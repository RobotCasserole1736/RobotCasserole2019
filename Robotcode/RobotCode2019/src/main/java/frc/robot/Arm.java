package frc.robot;

/*
 *******************************************************************************************
 * Copyright (C) 2019 FRC Team 1736 Robot Casserole - www.robotcasserole.org
 *******************************************************************************************
 *
 * This software is released under the MIT Licence - see the license.txt
 *  file in the root of this repo.
 *
 * Non-legally-binding statement from Team 1736:
 *  Thank you for taking the time to read through our software! We hope you
 *   find it educational and informative! 
 *  Please feel free to snag our software for your own use in whatever project
 *   you have going on right now! We'd love to be able to help out! Shoot us 
 *   any questions you may have, all our contact info should be on our website
 *   (listed above).
 *  If you happen to end up using our software to make money, that is wonderful!
 *   Robot Casserole is always looking for more sponsors, so we'd be very appreciative
 *   if you would consider donating to our club to help further STEM education.
 */

//import org.usfirst.frc.team1736.lib.Calibration.Calibration;
//import org.usfirst.frc.team1736.lib.Util.CrashTracker;

import frc.lib.Calibration.*;
import frc.lib.DataServer.Signal;
import frc.robot.IntakeControl.IntakePos;
//import edu.wpi.first.wpilibj.Spark;

import edu.wpi.first.wpilibj.AnalogPotentiometer;
import edu.wpi.first.wpilibj.Solenoid;
import edu.wpi.first.wpilibj.DigitalInput;

import com.revrobotics.CANSparkMax;
import com.revrobotics.ControlType;
import com.revrobotics.CANSparkMaxLowLevel.MotorType;
import com.revrobotics.CANEncoder;
import com.revrobotics.CANPIDController;


public class Arm {

    /////////Moving Things\\\\\\\\\
    Solenoid armBrake;
    CANSparkMax sadey;
    
    CANPIDController armPID;


    /////Know Where The Arm Is(From SparkMax)\\\\\
    CANEncoder armEncoder;
    double curArmAngle;
    /////The PID StartUps\\\\\
    double kP, kI, kD, kIz, kFF, kMaxOutput, kMinOutput, maxRPM;


    /////////Sensors\\\\\\\\\
    double voltageToDegreeMult;
    double zeroOffset;
    DigitalInput upperLimSwitch;
    DigitalInput lowLimSwitch;

    /////////Input Commands\\\\\\\\\\\
    ArmPos posIn;

    /////////State Varibles\\\\\\\\\\\\
    double   desAngle;
    double   prevManMoveCmd = 0;
    double   curManMoveCmd = 0;
    double   uncompensatedMotorCmd = 0;
    boolean  brakeIn = false;
    boolean  isZeroed = false;
    IntakePos curIntakePos;
    boolean  intakeExtend = false;

    //Arm State Heights\\
    Calibration topCargoHeightCal;
    Calibration midCargoHeightCal;
    Calibration lowCargoHeightCal;
    Calibration intakeCargoHeightCal;

    Calibration topHatchHeightCal;
    Calibration midHatchHeightCal;
    Calibration lowHatchHeightCal;
    Calibration intakeHatchHeightCal;

    Calibration intakeDangerZoneUpperHeight; //Lowest height allowable in Hatch Mode

    Calibration armAngleOffsetCal;
    Calibration gravOffsetHorz;
    Calibration rampRate;    

    /////////Limit Switches\\\\\\\\\\
    boolean topOfMotion;
    boolean bottomOfMotion; 

    Signal armMotorCurrentSig;
    Signal armMotorCmdSig;
    Signal armDesPosSig;
    Signal armActPosSig;

    /////////Physical Mechanism Constants\\\\\\\\\\
    final double MAX_MANUAL_DEG_PER_SEC = 25.0;
    final double ARM_GEAR_RATIO = 150.0/1.0;
    final double REV_ENCODER_TICKS_PER_REV = 42.0;


    
    /////////Initialization Code\\\\\\\\\\\
    private static Arm  singularInstance = null;

    public static synchronized Arm getInstance() {
        if ( singularInstance == null)
            singularInstance = new Arm();
        return singularInstance;
    }

    private Arm() {
        /////Movers\\\\\
        armBrake = new Solenoid(RobotConstants.ARM_MECH_BRAKE_SOL_PORT);
        sadey = new CANSparkMax(RobotConstants.ARM_MOTOR_PORT, MotorType.kBrushless);
        sadey.setSmartCurrentLimit(60);
        armEncoder = sadey.getEncoder();
        armPID = sadey.getPIDController();

        //Mechanically reversed direction is forward
        sadey.setInverted(true);

        armMotorCmdSig = new Signal("Arm Motor Command", "cmd");
        armMotorCurrentSig = new Signal("Arm Motor Current", "A");
        armDesPosSig = new Signal("Arm Desired Position", "deg");
        armActPosSig = new Signal("Arm Actual Position", "deg");
        
        /////PID Values\\\\\
        kP = 0.1; 
        kI = 0;
        kD = 0; 
        kIz = 0; 
        kFF = 0; 
        kMaxOutput = 1; 
        kMinOutput = -1;
        maxRPM = 9000;
        /////Set PID\\\\\
        armPID.setP(kP);
        armPID.setI(kI);
        armPID.setD(kD);
        armPID.setIZone(kIz);
        armPID.setFF(kFF);
        armPID.setOutputRange(kMinOutput, kMaxOutput);

        /////Digital Inputs\\\\\\\
        upperLimSwitch = new DigitalInput(RobotConstants.ARM_UPPER_LIMIT_SWITCH_PORT);
        lowLimSwitch = new DigitalInput(RobotConstants.ARM_LOWER_LIMIT_SWITCH_PORT);
        
        /////Calibration Things\\\\\
        topCargoHeightCal = new Calibration("Arm Top Cargo Level Pos (Deg)", 180);
        midCargoHeightCal = new Calibration("Arm Mid Cargo Level Pos (Deg)", 50);
        lowCargoHeightCal = new Calibration("Arm Bottom Cargo Level Pos (Deg)", 10);
        intakeCargoHeightCal = new Calibration("Arm Intake Cargo Level Pos (Deg)", 0);

        topHatchHeightCal = new Calibration("Arm Top Hatch Level Pos (Deg)", 190);
        midHatchHeightCal = new Calibration("Arm Mid Hatch Level Pos (Deg)", 45);
        lowHatchHeightCal = new Calibration("Arm Bottom Hatch Level Pos (Deg)", 5);
        intakeHatchHeightCal = new Calibration("Arm Intake Hatch Level Pos (Deg)", 5);

        intakeDangerZoneUpperHeight = new Calibration("Arm Intake Danger Zone Upper Pos (Deg)", 3);
        
        armAngleOffsetCal = new Calibration("Arm Is Offset From Flat Ground(Deg)", 20);
        gravOffsetHorz = new Calibration("Arm Required Voltage at Horz(Volts)", 0.5);
        rampRate = new Calibration("Arm Spark Ramp Rate (Volts)", 0.3);
        sadey.setRampRate(rampRate.get());
    } 
    

    public enum ArmPos {
        TopCargo(8), 
        TopHatch(7), 
        MiddleCargo(6), 
        MiddleHatch(5), 
        LowerCargo(4), 
        LowerHatch(3), 
        IntakeCargo(2), 
        IntakeHatch(1), 
        None(0);

        public final int value;

        private ArmPos(int value) {
            this.value = value;
        }
                
        public int toInt(){
            return this.value;
        }
    }

    double convertRotationsToDeg(double rotations_in) {
        return (rotations_in / REV_ENCODER_TICKS_PER_REV * ARM_GEAR_RATIO);
    }

    double convertDegToRotations(double degrees_in) {
        return(degrees_in *REV_ENCODER_TICKS_PER_REV / ARM_GEAR_RATIO);
    }

    public void sampleSensors() {
       topOfMotion = upperLimSwitch.get();
       topOfMotion = lowLimSwitch.get();
    }

    
    /////Use Sensor Data in Calculations\\\\\
    public void update() {

        curArmAngle = convertRotationsToDeg(-1.0*armEncoder.getPosition());

        sadey.setRampRate(rampRate.get());

        //TEMP - pretend we are always zeroed until the limit switches are installed.
        isZeroed =true;

        if(!isZeroed) {
            armPID.setReference(-0.01, ControlType.kVoltage);
            if(bottomOfMotion) {
                isZeroed = true;
                armPID.setReference(0.0, ControlType.kVoltage);
            }
        } 
        else {
            //Update the position based on what the driver requested
            
            if(curManMoveCmd != 0) {
                armPID.setReference(curManMoveCmd*6.0, ControlType.kVoltage);
                desAngle = curArmAngle;
            }
            else {
                defArmPos();
                double desRotation = convertDegToRotations(desAngle);
                double gravComp = gravComp();
                armPID.setReference(desRotation, ControlType.kPosition, 0, gravComp);
            }

        } 

        double sampleTimeMS = LoopTiming.getInstance().getLoopStartTimeSec() * 1000.0;
        armMotorCmdSig.addSample(sampleTimeMS, sadey.getAppliedOutput());
        armMotorCurrentSig.addSample(sampleTimeMS, sadey.getOutputCurrent());
        armDesPosSig.addSample(sampleTimeMS, desAngle);
        armActPosSig.addSample(sampleTimeMS, curArmAngle);

        prevManMoveCmd = curManMoveCmd;
    }

    public void setPositionCmd(ArmPos posIn) {
        this.posIn = posIn;
    }
    
    /////Movement Settings\\\\\
    public void defArmPos() {
        switch(posIn) {
            case TopCargo:
                desAngle = topCargoHeightCal.get();
            break;

            case MiddleCargo:
                desAngle = midCargoHeightCal.get();
            break;

            case LowerCargo:
                desAngle = lowCargoHeightCal.get();
            break;

            case IntakeCargo:
                desAngle = intakeCargoHeightCal.get();
            break;

            case TopHatch:
                desAngle = topHatchHeightCal.get();
            break;

            case MiddleHatch:
                desAngle = midHatchHeightCal.get();
            break;

            case LowerHatch:
                desAngle = lowHatchHeightCal.get();
            break;

            case IntakeHatch:
                desAngle = intakeHatchHeightCal.get();
            break;

            case None:
                // Don't change desiredArmAngle
            break;
            
        }
    }

    public void setManualMovementCmd(double mov_cmd_in) {
        
        if(mov_cmd_in>0.5 || mov_cmd_in<-0.5) {        
            brakeIn = false;    
        }
        else if(mov_cmd_in < 0.5 && mov_cmd_in > -0.5) {
            brakeIn = true;
        }
        else {

        }
        curManMoveCmd = mov_cmd_in;
    }

    public void setSolBrake(boolean brakeIn) {
        if(brakeIn) {
            armBrake.set(true); 
        }
        else {
            armBrake.set(false);
        }
    }
        

    public boolean getTopOfMotion() {
        return topOfMotion;
    }

    public boolean getBottomOfMotion() {
        return bottomOfMotion;
    }         

    public double getActualArmHeight() {
        return curArmAngle;
    }

    public boolean isAboveDangerZone(){
        return  (curArmAngle > intakeDangerZoneUpperHeight.get());
    }

    public double getDesiredArmHeight() {
        return desAngle;
    }

    public double getuncompensatedMotorCmd() {
        return uncompensatedMotorCmd;
    
    }
    public boolean atDesiredHeight() {
        return(Math.abs(desAngle - curArmAngle)<2.0); //hardcode to 2 degree desired height
    }

    /**
     * Set the current state of the intake. If Retracted, and the arm is 
     * inside the Danger Zone, motion should be stopped.
     * 
     */
    public void setIntakeActualState(IntakePos state_in) {
        curIntakePos = IntakeControl.getInstance().getPositionCmd();   
    }

    /**
     * 
     * @return True if the arm is forcing the intake to extend, false otherwise.
     */
    public boolean intakeExtendOverride() {
        //TODO
            if(curIntakePos == IntakePos.Retract && curArmAngle < 50) {
               intakeExtend = true;
            }
        return intakeExtend;
    }
    
    public double gravComp() {
        double compArmAngle = curArmAngle - armAngleOffsetCal.get();
        double cosAngle = Math.cos(compArmAngle);
        double compVolt = cosAngle * gravOffsetHorz.get();
        return(compVolt);
    }

    public void forceArmStop() {
        setPositionCmd(ArmPos.None);
        setManualMovementCmd(0.0);
        desAngle = curArmAngle;
    }
}