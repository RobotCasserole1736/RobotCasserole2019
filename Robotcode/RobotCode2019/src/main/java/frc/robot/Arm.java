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

import frc.lib.Calibration.*;
import frc.lib.DataServer.Signal;

import edu.wpi.first.wpilibj.Solenoid;

import com.revrobotics.CANSparkMax;
import com.revrobotics.ControlType;
import com.revrobotics.CANPIDController.AccelStrategy;
import com.revrobotics.CANSparkMaxLowLevel.MotorType;
import com.revrobotics.CANEncoder;
import com.revrobotics.CANPIDController;

import com.revrobotics.CANDigitalInput;
import com.revrobotics.CANDigitalInput.LimitSwitchPolarity;


public class Arm {

    /////////Moving Things\\\\\\\\\
    Solenoid armBrake;
    CANSparkMax sadey;
    
    CANPIDController armPID;


    /////Know Where The Arm Is(From SparkMax)\\\\\
    CANEncoder armEncoder;
    double curArmAngle;
    CANDigitalInput upperLimitSwitch;
    CANDigitalInput lowerLimitSwitch;
    
    /////The PID StartUps\\\\\
    public double kIz, kFF;
    Calibration kP, kI, kD;

    /////////Input Commands\\\\\\\\\\\
    ArmPos posIn;

    /////////State Varibles\\\\\\\\\\\\
    double   desAngle;
    double   prevManMoveCmd = 0;
    double   curManMoveCmd = 0;
    double   uncompensatedMotorCmd = 0;
    boolean  brakeIn = false;
    boolean  isZeroed = false;

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

    Calibration gravOffsetHorz;

    Calibration bottomLimitSwitchDegreeCal;
    Calibration topLimitSwitchDegreeCal;

    Calibration kMaxOutputCal;
    Calibration kMinOutputCal;
    Calibration maxVelCal;
    Calibration minVelCal;
    Calibration maxAccCal;
    Calibration allowedErrCal;


    /////////Limit Switches\\\\\\\\\\
    boolean topOfMotion;
    boolean bottomOfMotion; 

    Signal armMotorCurrentSig;
    Signal armMotorCmdSig;
    Signal armDesPosSig;
    Signal armActPosSig;
    Signal armActVelSig;
    Signal armLowerLimitSig;
    Signal armUpperLimitSig;

    /////////Physical Mechanism Constants\\\\\\\\\\
    final double MAX_MANUAL_DEG_PER_SEC = 25.0;
    final double SPROCKET_GEAR_RATIO = 5.0/1.0;
    final double GEARBOX_GEAR_RATIO = 20.0/1.0;

    final double INVERT_FACTOR = -1.0;

    boolean runSimMode;

    
    /////////Initialization Code\\\\\\\\\\\
    private static Arm singularInstance = null;

    public static synchronized Arm getInstance() {
        if ( singularInstance == null)
            singularInstance = new Arm();
        return singularInstance;
    }

    private Arm() {
        runSimMode = RioSimMode.getInstance().isSimMode();

        /////Movers\\\\\
        armBrake = new Solenoid(RobotConstants.ARM_MECH_BRAKE_SOL_PORT);
        sadey = new CANSparkMax(RobotConstants.ARM_MOTOR_PORT, MotorType.kBrushless);
        sadey.restoreFactoryDefaults();//ensure we start from the same config every time.

        //Force a max current limit of 60A to try to help prevent burnout and brownout and such.
        sadey.setSmartCurrentLimit(60);

        //Configure Encoder
        armEncoder = sadey.getEncoder();
        armEncoder.setPositionConversionFactor((1/GEARBOX_GEAR_RATIO)*(1/SPROCKET_GEAR_RATIO)*360); //Set up so the distance measurement is in degrees
        armEncoder.setVelocityConversionFactor((1/GEARBOX_GEAR_RATIO)*(1/SPROCKET_GEAR_RATIO)*360*60); //Set up so the velocity measurement is in deg/sec
        //Configure s
        armPID = sadey.getPIDController();

        //Limit switches should be wired to be normally-closed - this way if they come unplugged, 
        // we go to the safe-state of "no motion"
        //Assume upper limit of travel is in the forward direction of motion
        //Assume lower limit of travel is in the reverse direction of motion
        upperLimitSwitch = sadey.getForwardLimitSwitch(LimitSwitchPolarity.kNormallyClosed); 
        lowerLimitSwitch = sadey.getReverseLimitSwitch(LimitSwitchPolarity.kNormallyClosed); 
        //Ensure limit switches remain enabled. This should bedefault but I don't want to take any chances.
        upperLimitSwitch.enableLimitSwitch(true);
        lowerLimitSwitch.enableLimitSwitch(true);


        //Mechanically reversed direction is "forward" in our code
        //This doesn't seem to work as I'd expect in closed loop so we'll invert when going to/from the motor.
        sadey.setInverted(false);

        armMotorCmdSig = new Signal("Arm Motor Command", "cmd");
        armMotorCurrentSig = new Signal("Arm Motor Current", "A");
        armDesPosSig = new Signal("Arm Desired Position", "deg");
        armActPosSig = new Signal("Arm Actual Position", "deg");
        armActVelSig = new Signal("Arm Actual Velocity", "deg per sec");
        armLowerLimitSig = new Signal("Arm Lower Position Limit Switch", "bool");
        armUpperLimitSig = new Signal("Arm Upper Position Limit Switch", "bool");


        /////Calibration Things\\\\\
        topCargoHeightCal    = new Calibration("Arm Cargo Level Pos Top (Deg)", 180);
        midCargoHeightCal    = new Calibration("Arm Cargo Level Pos Mid (Deg)", 50);
        lowCargoHeightCal    = new Calibration("Arm Cargo Level Pos Bottom (Deg)", 10);
        intakeCargoHeightCal = new Calibration("Arm Cargo Level Pos Intake (Deg)", 0);

        topHatchHeightCal    = new Calibration("Arm Hatch Level Pos Top (Deg)", 190);
        midHatchHeightCal    = new Calibration("Arm Hatch Level Pos Mid (Deg)", 45);
        lowHatchHeightCal    = new Calibration("Arm Hatch Level Pos Bottom (Deg)", 5);
        intakeHatchHeightCal = new Calibration("Arm Hatch Level Pos Intake (Deg)", 5);

        intakeDangerZoneUpperHeight = new Calibration("Arm Intake Danger Zone Upper Pos (Deg)", 3);
        
        gravOffsetHorz    = new Calibration("Arm Required Voltage at Horz(Volts)", 0.5);
        bottomLimitSwitchDegreeCal = new Calibration("Arm Limit Switch Angle Bottom (deg)", -30);
        topLimitSwitchDegreeCal    = new Calibration("Arm Limit Switch Angle Top (deg)", 110);

        //Calibration for the Arm Trapezoidal\\
        kMaxOutputCal = new Calibration("Arm SmartMotion Max Power", 1);
        kMinOutputCal = new Calibration("Arm SmartMotion Min Power",-1);
        maxVelCal     = new Calibration("Arm SmartMotion Max Velocity For The Arm (Deg per sec)", 20);
        minVelCal     = new Calibration("Arm SmartMotion Min Velocity For The Arm (Deg per sec)", -20);
        maxAccCal     = new Calibration("Arm SmartMotion Max Acceleration on Arm (Deg per sec sqrd)", 10);
        allowedErrCal = new Calibration("Arm SmartMotion Allowable Err (deg)", 0.5);

        kP  = new Calibration("Arm PID Control kP", 0.3); 
        kI  = new Calibration("Arm PID Control kI", 0);
        kD  = new Calibration("Arm PID Control kD", 0); 
        kIz = 0; 
        kFF = 0; 

        updateCalValues();
    } 

    public void updateCalValues(){
        /////Set PID\\\\\
        armPID.setP(kP.get());
        armPID.setI(kI.get());
        armPID.setD(kD.get());
        armPID.setIZone(kIz);
        armPID.setFF(kFF);
        armPID.setOutputRange(kMinOutputCal.get(), kMaxOutputCal.get());
        
        //What is the Slot For - it's for ummmm slotty things. slotty mc slot face.
        int smartMotionSlot = 0;
        
        armPID.setSmartMotionMaxVelocity(maxVelCal.get(), smartMotionSlot);
        armPID.setSmartMotionMinOutputVelocity(minVelCal.get(), smartMotionSlot);
        armPID.setSmartMotionMaxAccel(maxAccCal.get(), smartMotionSlot);
        armPID.setSmartMotionAllowedClosedLoopError(allowedErrCal.get(), smartMotionSlot);
        armPID.setSmartMotionAccelStrategy(AccelStrategy.kTrapezoidal, smartMotionSlot);
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

    public void sampleSensors() {
       topOfMotion = upperLimitSwitch.get();
       bottomOfMotion = lowerLimitSwitch.get();
       if (topOfMotion){
           armEncoder.setPosition(convertArmDegToMotorRot(topLimitSwitchDegreeCal.get()));
       } 
       if (bottomOfMotion){
           armEncoder.setPosition(convertArmDegToMotorRot(bottomLimitSwitchDegreeCal.get()));
       }
    }

    public double convertArmDegToMotorRot(double in){
        return INVERT_FACTOR*in/((1/GEARBOX_GEAR_RATIO)*(1/SPROCKET_GEAR_RATIO)*360);
    }

    
    /////Use Sensor Data in Calculations\\\\\
    public void update() {

        
        if(runSimMode){
            //Run a simulated arm
            if(curManMoveCmd != 0) {
                curArmAngle += 50*MAX_MANUAL_DEG_PER_SEC*curManMoveCmd;
                desAngle = curArmAngle;
            } else {
                double err = (desAngle - curArmAngle);
                if(Math.abs(err) > 50*RobotConstants.MAIN_LOOP_SAMPLE_RATE_S){
                    err = Math.copySign(50*RobotConstants.MAIN_LOOP_SAMPLE_RATE_S, err);
                }
                curArmAngle += err;
            }

        } else {
            //Control the actual arm
            sampleSensors();
            curArmAngle = INVERT_FACTOR*armEncoder.getPosition();
            
            //Update the position based on what the driver requested
            if(curManMoveCmd != 0) {
                armPID.setReference(INVERT_FACTOR*curManMoveCmd*6.0, ControlType.kVoltage);
                desAngle = curArmAngle;
            }
            else {
                defArmPos();
                double desRotation = desAngle;
                double gravComp = gravComp();
                armPID.setReference(INVERT_FACTOR*desRotation, ControlType.kPosition, 0, gravComp); //restore gravity TODOS
                //armPID.setReference(INVERT_FACTOR*desRotation, ControlType.kSmartMotion, 0, gravComp);

            }

        }



        double sampleTimeMS = LoopTiming.getInstance().getLoopStartTimeSec() * 1000.0;
        armMotorCmdSig.addSample(sampleTimeMS, sadey.getAppliedOutput());
        armMotorCurrentSig.addSample(sampleTimeMS, sadey.getOutputCurrent());
        armDesPosSig.addSample(sampleTimeMS, desAngle);
        armActPosSig.addSample(sampleTimeMS, curArmAngle);
        armActVelSig.addSample(sampleTimeMS, INVERT_FACTOR*armEncoder.getVelocity());
        armLowerLimitSig.addSample(sampleTimeMS, bottomOfMotion);
        armUpperLimitSig.addSample(sampleTimeMS, topOfMotion);

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
    
    public double gravComp() {
        double cosAngle = Math.cos(Math.toRadians(curArmAngle));
        double compVolt = cosAngle * gravOffsetHorz.get();
        return(compVolt);
    }

    public void forceArmStop() {
        setPositionCmd(ArmPos.None);
        setManualMovementCmd(0.0);
        desAngle = curArmAngle;
    }
}