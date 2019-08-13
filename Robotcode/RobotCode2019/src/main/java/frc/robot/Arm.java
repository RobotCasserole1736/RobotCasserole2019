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
import frc.lib.Util.DaBouncer;
import edu.wpi.first.wpilibj.Solenoid;

import com.revrobotics.CANSparkMax;
import com.revrobotics.ControlType;
import com.revrobotics.CANPIDController.AccelStrategy;
import com.revrobotics.CANSparkMax.IdleMode;
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
    public double kIz;
    Calibration kP, kI, kD, kFF;

    /////////Input Commands\\\\\\\\\\\
    ArmPos posIn;

    /////////State Varibles\\\\\\\\\\\\
    double   desAngle;
    double   prevManMoveCmd = 0;
    double   curManMoveCmd = 0;
    double   uncompensatedMotorCmd = 0;
    boolean  brakeIn = false;
    boolean  isZeroed = false;
    boolean hatchMode=false;
    boolean cargoMode=false;

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

    DaBouncer atDesAngleDbnc;
    boolean atDesAngle;
    final double MIN_ALLOWABLE_ANGLE_ERR_DEG = 2.0;
    final int AT_DES_ANGLE_DBNC_LOOPS = 7;

    double desRotationPrev = -10000;
    boolean forceUpdate = true;


    /////////Limit Switches\\\\\\\\\\
    boolean topOfMotion = false;
    boolean bottomOfMotion = false; 
    boolean topOfMotionPrev = false;
    boolean bottomOfMotionPrev = false; 

    Signal armMotorCurrentSig;
    Signal armMotorCmdSig;
    Signal armDesPosSig;
    Signal armActPosSig;
    Signal armActVelSig;
    Signal armDesVelSig;
    Signal armLowerLimitSig;
    Signal armUpperLimitSig;
    Signal atDesAngleSig;
    Signal armGearSkipCounterSig;

    /////////Physical Mechanism Constants\\\\\\\\\\
    final double MAX_MANUAL_DEG_PER_SEC = 25.0;
    final double SPROCKET_GEAR_RATIO = 54/12;//Was 5:1 //*17.0/12.0 we removed it so heres the value
    final double GEARBOX_GEAR_RATIO = 30.0/1.0;

    final double INVERT_FACTOR = -1.0;

    boolean runSimMode;

    int gearSkipCounter = 0;
    
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
        //armBrake = new Solenoid(RobotConstants.ARM_MECH_BRAKE_SOL_PORT);
        sadey = new CANSparkMax(RobotConstants.ARM_MOTOR_CANID, MotorType.kBrushless);
        sadey.restoreFactoryDefaults();//ensure we start from the same config every time.

        //Force a max current limit of 60A to try to help prevent burnout and brownout and such.
        sadey.setSmartCurrentLimit(60);

        //Configure Encoder
        armEncoder = sadey.getEncoder();
        armEncoder.setPositionConversionFactor((1/GEARBOX_GEAR_RATIO)*(1/SPROCKET_GEAR_RATIO)*360); //Set up so the distance measurement is in degrees
        armEncoder.setVelocityConversionFactor((1/GEARBOX_GEAR_RATIO)*(1/SPROCKET_GEAR_RATIO)*360/60); //Set up so the velocity measurement is in deg/sec
        //Configure s
        armPID = sadey.getPIDController();

        //Limit switches should be wired to be normally-closed - this way if they come unplugged, 
        // we go to the safe-state of "no motion"
        //Assume upper limit of travel is in the forward direction of motion
        //Assume lower limit of travel is in the reverse direction of motion
        lowerLimitSwitch = sadey.getForwardLimitSwitch(LimitSwitchPolarity.kNormallyClosed); 
        upperLimitSwitch = sadey.getReverseLimitSwitch(LimitSwitchPolarity.kNormallyClosed); 
        //Ensure limit switches remain enabled. This should bedefault but I don't want to take any chances.
        upperLimitSwitch.enableLimitSwitch(true);
        lowerLimitSwitch.enableLimitSwitch(true);


        //Mechanically reversed direction is "forward" in our code
        //This doesn't seem to work as I'd expect in closed loop so we'll invert when going to/from the motor.
        sadey.setInverted(false);

        atDesAngle = false;
        atDesAngleDbnc = new DaBouncer(MIN_ALLOWABLE_ANGLE_ERR_DEG, AT_DES_ANGLE_DBNC_LOOPS);

        armMotorCmdSig = new Signal("Arm Motor Command", "cmd");
        armMotorCurrentSig = new Signal("Arm Motor Current", "A");
        armDesPosSig = new Signal("Arm Desired Position", "Deg");
        armActPosSig = new Signal("Arm Actual Position", "Deg");
        armActVelSig = new Signal("Arm Actual Velocity", "deg per sec");
        armDesVelSig = new Signal("Arm Desired Velocity", "deg per sec");
        armLowerLimitSig = new Signal("Arm Lower Position Limit Switch", "bool");
        armUpperLimitSig = new Signal("Arm Upper Position Limit Switch", "bool");
        atDesAngleSig = new Signal("Arm At Desired Angle", "bool");
        armGearSkipCounterSig = new Signal("Arm Gear Skip Count", "count");


        /////Calibration Things\\\\\
        topCargoHeightCal    = new Calibration("Arm Cargo Level Pos Top Deg", 140);
        midCargoHeightCal    = new Calibration("Arm Cargo Level Pos Mid Deg", 35);
        lowCargoHeightCal    = new Calibration("Arm Cargo Level Pos Bottom Deg", -7);
        intakeCargoHeightCal = new Calibration("Arm Cargo Level Pos Intake Deg", -43);

        topHatchHeightCal    = new Calibration("Arm Hatch Level Pos Top Deg", 65);
        midHatchHeightCal    = new Calibration("Arm Hatch Level Pos Mid Deg", 23);
        lowHatchHeightCal    = new Calibration("Arm Hatch Level Pos Bottom Deg", -28);
        intakeHatchHeightCal = new Calibration("Arm Hatch Level Pos Intake Deg", 16);

        intakeDangerZoneUpperHeight = new Calibration("Arm Intake Danger Zone Upper Pos Deg", -45);
        
        gravOffsetHorz    = new Calibration("Arm Required Voltage at Horz V", 0.5);
        bottomLimitSwitchDegreeCal = new Calibration("Arm Limit Switch Angle Bottom Deg", -45);
        topLimitSwitchDegreeCal    = new Calibration("Arm Limit Switch Angle Top Deg", 165.0);

        //Calibration for the Arm Trapezoidal\\
        kMaxOutputCal = new Calibration("Arm SmartMotion Max Power", 1);
        kMinOutputCal = new Calibration("Arm SmartMotion Min Power",-1);
        maxVelCal     = new Calibration("Arm SmartMotion Max Velocity For The Arm DegPerSec", 170);
        minVelCal     = new Calibration("Arm SmartMotion Min Velocity For The Arm DegPerSec", -170);
        maxAccCal     = new Calibration("Arm SmartMotion Max Acceleration on Arm DegPerSecSqrd", 340);
        allowedErrCal = new Calibration("Arm SmartMotion Allowable Err Deg", 0.5);

        kP  = new Calibration("Arm Velocity PID Control kP", 2.0E-5); 
        kI  = new Calibration("Arm Velocity PID Control kI", 1.0E-7);
        kD  = new Calibration("Arm Velocity PID Control kD", 0); 
        kFF  = new Calibration("Arm Velocity PID Control kFF", 0.00015); 
        kIz = 0; 
        updateCalValues(true);
    } 

    public void updateCalValues(boolean forceUpdate){
        /////Set PID\\\\\
        if(haveCalsChanged() || forceUpdate)
        {
            //We don't want to update these every single loop because it significantly impacts performance
            armPID.setP(kP.get());
            armPID.setI(kI.get());
            armPID.setD(kD.get());
            armPID.setIZone(kIz);
            armPID.setFF(kFF.get());
            armPID.setOutputRange(kMinOutputCal.get(), kMaxOutputCal.get());
            
            //What is the Slot For - it's for ummmm slotty things. slotty mc slot face.
            int smartMotionSlot = 0;
            
            armPID.setSmartMotionMaxVelocity(maxVelCal.get(), smartMotionSlot);
            armPID.setSmartMotionMinOutputVelocity(minVelCal.get(), smartMotionSlot);
            armPID.setSmartMotionMaxAccel(maxAccCal.get(), smartMotionSlot);
            armPID.setSmartMotionAllowedClosedLoopError(allowedErrCal.get(), smartMotionSlot);
            armPID.setSmartMotionAccelStrategy(AccelStrategy.kTrapezoidal, smartMotionSlot);

            acknowledgeCalsChanged();
        }
    }

    private boolean haveCalsChanged() {
        return kP.isChanged() || kI.isChanged() || kD.isChanged() || kFF.isChanged() || kMinOutputCal.isChanged() ||
        kMaxOutputCal.isChanged() || maxVelCal.isChanged() || minVelCal.isChanged() ||
        maxAccCal.isChanged() || allowedErrCal.isChanged();
    }

    private void acknowledgeCalsChanged() {
        kP.acknowledgeValUpdate();
        kI.acknowledgeValUpdate();
        kD.acknowledgeValUpdate();
        kFF.acknowledgeValUpdate();
        kMinOutputCal.acknowledgeValUpdate();
        kMaxOutputCal.acknowledgeValUpdate();
        maxVelCal.acknowledgeValUpdate();
        minVelCal.acknowledgeValUpdate();
        maxAccCal.acknowledgeValUpdate();
        allowedErrCal.acknowledgeValUpdate();
    }
    

    public enum ArmPos {
        Disabled(9),
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

    public enum OpMode {
        Cargo(3),
        Hatch(2), 
        None(0);

        public final int value;

        private OpMode(int value) {
            this.value = value;
        }
                
        public int toInt(){
            return this.value;
        }
    }

    public void setMatchStartPosition(){
        setMatchStartPosition(-53.0);//fudge
    }

    public void setMatchStartPosition(double initPos_deg){
        armEncoder.setPosition(convertArmDegToMotorRot(initPos_deg)); 
        forceUpdate = true;
    }

    public void sampleSensors() {
       topOfMotionPrev = topOfMotion;
       bottomOfMotionPrev = bottomOfMotion;
       topOfMotion = upperLimitSwitch.get();
       bottomOfMotion = lowerLimitSwitch.get();


       curArmAngle = INVERT_FACTOR*armEncoder.getPosition();

    //    if (topOfMotion == true && topOfMotionPrev == false){
    //        double expectedAngle = topLimitSwitchDegreeCal.get();
    //        if(Math.abs(expectedAngle - curArmAngle) > 5.0){
    //             //We just hit the top limit switch, and the error was bigger than one tooth at the bottom gear - we probably slipped a tooth.
    //             //Reset encoder position at top
    //             armEncoder.setPosition(convertArmDegToMotorRot(topLimitSwitchDegreeCal.get())); 
    //             forceUpdate = true;
    //             gearSkipCounter++;
    //        }
    //    } 

       if (bottomOfMotion == true && bottomOfMotionPrev == false){
           armEncoder.setPosition(convertArmDegToMotorRot(bottomLimitSwitchDegreeCal.get()));
           forceUpdate = true;
       }

    }

    public double convertArmDegToMotorRot(double in){
        return INVERT_FACTOR*in/((1/GEARBOX_GEAR_RATIO)*(1/SPROCKET_GEAR_RATIO)*360);
    }

    
    /////Use Sensor Data in Calculations\\\\\
    public void update() {

        double testDesVel = 0;

        
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
            
            //Update the position based on what the driver requested
            // double gravComp = gravComp();
            // armPID.setReference(INVERT_FACTOR*curManMoveCmd*6.0 + INVERT_FACTOR*gravComp, ControlType.kVoltage);
            // desAngle = curArmAngle;

             if(curManMoveCmd != 0 || posIn == ArmPos.Disabled) {
                 armPID.setReference(INVERT_FACTOR*curManMoveCmd*6.0, ControlType.kVoltage);
                 desAngle = curArmAngle;
             } else {
                double desRotation = desAngle;
                
                double gravComp = gravComp(); //Turns out, controls wise I guess we don't need this

                //testDesVel = desRotation; //TEMP - test only
                //armPID.setReference(INVERT_FACTOR*testDesVel, ControlType.kVelocity, 0, 0); //TEMP - test only
                armPID.setReference(INVERT_FACTOR*desRotation, ControlType.kPosition, 0, gravComp); // AKA not-smart motion

                //we've gotten reports that this is an expensive funciton call to make, so don't make it unless you need to?
                 if(desRotation != desRotationPrev || forceUpdate || true){ //Turns out we can't do this without new firmware
                     armPID.setReference(INVERT_FACTOR*desRotation, ControlType.kSmartMotion, 0, 0);
                     forceUpdate = false;
                 }
                
                 desRotationPrev = desRotation;
             }

        }

        atDesAngle = atDesAngleDbnc.BelowDebounce(Math.abs(desAngle - curArmAngle));

        double sampleTimeMS = LoopTiming.getInstance().getLoopStartTimeSec() * 1000.0;
        armMotorCmdSig.addSample(sampleTimeMS, sadey.getAppliedOutput());
        armMotorCurrentSig.addSample(sampleTimeMS, sadey.getOutputCurrent());
        armDesPosSig.addSample(sampleTimeMS, desAngle);
        armActPosSig.addSample(sampleTimeMS, curArmAngle);
        armActVelSig.addSample(sampleTimeMS, INVERT_FACTOR*armEncoder.getVelocity());
        armDesVelSig.addSample(sampleTimeMS, testDesVel);
        armLowerLimitSig.addSample(sampleTimeMS, bottomOfMotion);
        armUpperLimitSig.addSample(sampleTimeMS, topOfMotion);
        atDesAngleSig.addSample(sampleTimeMS, atDesAngle);
        armGearSkipCounterSig.addSample(sampleTimeMS, gearSkipCounter);

        prevManMoveCmd = curManMoveCmd;
    }

    public void setPositionCmd(ArmPos posIn) {
        this.posIn = posIn;
        defArmPos();
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

            case Disabled:
                curManMoveCmd = 0;
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
            //armBrake.set(true); 
        }
        else {
            //armBrake.set(false);
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
        return(atDesAngle); 
    }

    
    
    public double gravComp() {
        double cosAngle = Math.cos(Math.toRadians(curArmAngle));
        double compVolt = cosAngle * gravOffsetHorz.get();
        return(compVolt);
    }

    public void forceArmStop() {
        setPositionCmd(ArmPos.Disabled);
        setManualMovementCmd(0.0);
        desAngle = intakeCargoHeightCal.get();
        curManMoveCmd = 0;
        armPID.setReference(0, ControlType.kVoltage);
    }

    public void setCoastMode()
    {
        sadey.setIdleMode(IdleMode.kCoast);
    }

    public void setBrakeMode()
    {
        sadey.setIdleMode(IdleMode.kBrake);
    }
    
}