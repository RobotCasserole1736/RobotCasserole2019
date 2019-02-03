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
    AnalogPotentiometer armPot;
    int potChannel;
    double voltageToDegreeMult;
    double zeroOffset;
    DigitalInput upperLimSwitch;
    DigitalInput lowLimSwitch;

    /////////Input Commands\\\\\\\\\\\
    ArmPosReq posIn;

    /////////State Varibles\\\\\\\\\\\\
    double   desAngle;
    double   potUpVolt;
    double   potLowVolt;
    boolean  brakeActivated;
    double   pastManMoveCmd = 0;
    double   curManMoveCmd = 0;
    double   uncompensatedMotorCmd = 0;
    boolean  brakeIn = false;
    boolean  isZeroed = false;
    IntakePos curIntakePos;
    boolean  intakeExtend = false;

    //Arm State Heights\\
    
    double topRocket;
    double midRocket;
    double lowRocket;
    double intakeHeight;
    
    
    Calibration topRocketCal;
    Calibration midRocketCal;
    Calibration lowRocketCal;
    Calibration intakeHeightCal;
    Calibration armUpCal;
    Calibration armDownCal;
    Calibration armAngleOffsetCal;
    Calibration gravOffsetHorz;
    Calibration rampRate;    

    ///////////Pot State\\\\\\\\\\\\
    double UpperLimitDegrees = 270;
    double UpperLimitVoltage = 12;
    double LowerLimitDegrees = 0;
    double LowerLimitVoltage = 12;
    double armPotPos;


    /////////Limit Switches\\\\\\\\\\
    boolean topOfMotion;
    boolean bottomOfMotion; 

    Signal armMotorCurrentSig;
    Signal armMotorCmdSig;
    Signal armDesPosSig;
    Signal armActPosSig;

    final double MAX_MANUAL_DEG_PER_SEC = 10.0;
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
        /////Analog Inputs\\\\\\\\
        armPot = new AnalogPotentiometer(RobotConstants.ARM_POS_SENSOR_PORT, voltageToDegreeMult, zeroOffset);
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
        topRocketCal = new Calibration("Arm Top Level Pos (Deg)", 190);
        midRocketCal = new Calibration("Arm Mid Level Pos (Deg)", 50);
        lowRocketCal = new Calibration("Arm Bottom Level Pos (Deg)", 10);
        intakeHeightCal = new Calibration("Arm Intake Level Pos (Deg)", 0);
        armAngleOffsetCal = new Calibration("Arm Is Offset From Flat Ground(Deg)", 20);
        gravOffsetHorz = new Calibration("Arm Required Voltage at Horz(Volts)", 0.5);
        rampRate = new Calibration("Arm Spark Ramp Rate (Volts)", 0.3);
        sadey.setRampRate(rampRate.get());
    } 
    

    public enum ArmPosReq {
        Top(4), Middle(3), Lower(2), Intake(1), None(0);

        public final int value;

        private ArmPosReq(int value) {
            this.value = value;
        }
                
        public int toInt(){
            return this.value;
        }
    }

    /*No more Potentiometer
    double convertVoltsToDeg(double voltage_in) {
        return (voltage_in - LowerLimitVoltage) * 
                (UpperLimitDegrees - LowerLimitDegrees) / 
                (UpperLimitVoltage - LowerLimitVoltage) + LowerLimitDegrees;
    }*/
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

        curArmAngle = convertRotationsToDeg(armEncoder.getPosition());

        sadey.setRampRate(rampRate.get());
        double setAngle = curArmAngle;

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
            defArmPos();

            if(curManMoveCmd != 0) {
                setAngle = curArmAngle + curManMoveCmd*MAX_MANUAL_DEG_PER_SEC*0.02;
                desAngle = setAngle;
            }
            else {
                setAngle = desAngle;
            }

            double desRotation = convertDegToRotations(setAngle);
            double gravComp = gravComp();
            armPID.setReference(desRotation, ControlType.kPosition, 0, gravComp);

        } 

        double sampleTimeMS = LoopTiming.getInstance().getLoopStartTimeSec() * 1000.0;
        armMotorCmdSig.addSample(sampleTimeMS, sadey.getAppliedOutput());
        armMotorCurrentSig.addSample(sampleTimeMS, sadey.getOutputCurrent());
        armDesPosSig.addSample(sampleTimeMS, setAngle);
        armActPosSig.addSample(sampleTimeMS, curArmAngle);
    }

    public void setPositionCmd(ArmPosReq posIn) {
        this.posIn = posIn;
    }
    
    /////Movement Settings\\\\\
    public void defArmPos() {
        switch(posIn) {
            case Top:
            desAngle = topRocketCal.get();
            break;

            case Middle:
            desAngle = midRocketCal.get();
            break;

            case Lower:
            desAngle = lowRocketCal.get();
            break;

            case Intake:
            desAngle = intakeHeightCal.get();
            break;

            case None:
            // Don't change desiredArmAngle
            break;
            
        }
    }

    public void setManualMovementCmd(double mov_cmd_in) {
        
        if(mov_cmd_in>0.5 || mov_cmd_in<-0.5) {    
            curManMoveCmd = mov_cmd_in;
            brakeIn = false;
        
        }
        else if(mov_cmd_in < 0.5 && mov_cmd_in > -0.5) {
        
            curManMoveCmd = 0;
            brakeIn = true;

        }
        else {
            curManMoveCmd = 0;
            
        }
    }

    public void setSolBrake(boolean brakeIn) {
        if(brakeIn) {
            armBrake.set(true); 
        }
        else {
            armBrake.set(true);
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
        curIntakePos = IntakeControl.getInstance().getEstimatedPosition();   
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
        //TODO
    }
}