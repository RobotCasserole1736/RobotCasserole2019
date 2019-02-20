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

import edu.wpi.first.wpilibj.DigitalInput;
import edu.wpi.first.wpilibj.Timer;

import com.ctre.phoenix.motorcontrol.ControlMode;
import com.ctre.phoenix.motorcontrol.can.VictorSPX;
import com.ctre.phoenix.motorcontrol.can.WPI_TalonSRX;
import frc.lib.Calibration.Calibration;
import frc.lib.DataServer.Signal;

public class IntakeControl{

    boolean runSimMode;

    DigitalInput ballInIntake;
    DigitalInput backIntakeSwitch;
    DigitalInput frontIntakeSwitch;

    

    WPI_TalonSRX intakeMotor;
    VictorSPX intakeLeftArmMotor;
    VictorSPX intakeRightArmMotor;

    double intakeMotorCmd = 0;
    double intakeRollerCmd = 0;

    Calibration intakeSpeed;
    Calibration ejectSpeed;
    Calibration groundToExtendTime;
    Calibration retractedToExtendTime;

    Calibration positionOverride;

    boolean isOnTarget;

    Signal intakeCmdStateSig;
    Signal motorSpeedCmdSig;
    Signal ballInIntakeSig;
    Signal isOnTargetSig; 
    Signal intakeAtBackLimit;
    Signal intakeAtFrontLimit;
    Signal rollerSpeedCmdSig;

    boolean ballDetected = false;
    boolean backLimitSwitchPressed = false;
    boolean frontLimitSwitchPressed = false;

    double transitionEndTime = 0;

    //Start with intake retracted and stopped
    private IntakePos intakePosDes = IntakePos.Retract;
    private IntakePos intakePosAct = IntakePos.Retract;
    private IntakePos prevIntakePosDes = IntakePos.Retract;
    private IntakeSpd intakeSpdCmd = IntakeSpd.Stop;

    /* singelton stuff */
    private static IntakeControl iControl = null;
    public static synchronized IntakeControl getInstance() {
        if(iControl == null)
            iControl = new IntakeControl();
        return iControl;
    }
    
    private IntakeControl(){
        runSimMode = RioSimMode.getInstance().isSimMode();

        intakeSpeed = new Calibration("Intake Intake Speed motor cmd", 0.5, 0, 1);
        ejectSpeed = new Calibration("Intake Eject Speed motor cmd", 0.5, 0, 1);
        groundToExtendTime = new Calibration("Intake Ground to Extend Time sec", 0.4, 0, 10);
        retractedToExtendTime = new Calibration("Intake Retracted to Extend Time Sec", 0.85, 0, 10);
        
        positionOverride = new Calibration("Intake Position Override Enable", 0, 0, 1);
        
        ballInIntake = new DigitalInput(RobotConstants.BALL_INTAKE_PORT);
        backIntakeSwitch = new DigitalInput(RobotConstants.INTAKE_BACK_LIMIT_SWITCH_PORT);
        frontIntakeSwitch = new DigitalInput(RobotConstants.INTAKE_FRONT_LIMIT_SWITCH_PORT);

        intakeMotor = new WPI_TalonSRX(RobotConstants.INTAKE_MOTOR_CANID);
        intakeLeftArmMotor  = new VictorSPX(RobotConstants.INTAKE_MOTOR_LEFT_CANID);
        intakeRightArmMotor = new VictorSPX(RobotConstants.INTAKE_MOTOR_RIGHT_CANID);

        intakeCmdStateSig = new Signal("Intake Commanded Position", "Intake Pos Enum");
        motorSpeedCmdSig = new Signal("Intake Arms Motor Commands", "cmd");
        rollerSpeedCmdSig = new Signal("Intake Roller Motor Command", "cmd");
        ballInIntakeSig = new Signal("Intake Ball Present", "bool");
        isOnTargetSig  = new Signal("Intake Arm Position On Target", "bool");
        intakeAtBackLimit  = new Signal("Intake Arms at Back Limit", "bool");
        intakeAtFrontLimit  = new Signal("Intake Arms at Front Limit", "bool");

    }

    //Intake positions that can be requested
    public enum IntakePos {
        Extend(0), Retract(1), Ground(2), InTransit(3);

        public final int value;

        private IntakePos(int value) {
            this.value = value;
        }
                
        public int toInt(){
            return this.value;
        }
    }
    
    
    //Intake positions that can be requested
    public enum IntakeSpd {
        Stop(0), Intake(1), Eject(2);

        public final int value;

        private IntakeSpd(int value) {
            this.value = value;
        }
        
        public int toInt(){
            return this.value;
        }
    }

    public void manualIntakeOverride(){
        int dpadPos = OperatorController.getInstance().xb.getPOV();

        if(dpadPos == 0){
            intakePosDes = IntakePos.Ground;
        } else if(dpadPos == 90 || dpadPos == 270){
            intakePosDes = IntakePos.Extend;
        } else if(dpadPos == 180){
            intakePosDes = IntakePos.Retract;
        }
    }

    public void setPositionCmd(IntakePos posIn){

        intakePosDes = posIn;

    }

    public void stopIntakeMotion(){
        intakeMotorCmd = 0;
        intakeRollerCmd = 0;
        intakeLeftArmMotor.set(ControlMode.PercentOutput, 0);
        intakeRightArmMotor.set(ControlMode.PercentOutput, 0);
    }

    public IntakePos getPositionCmd(){
        return intakePosDes;
    }

    public void setSpeedCmd(IntakeSpd speedIn){
        intakeSpdCmd=speedIn;
    }

    public boolean isAtDesPos(){
        if(runSimMode){
            return true;
        } else {
            return isOnTarget;
        }
    }

    // Reads the back limit switch and attempts to set up initail states 
    // Returns true if we're at the retracted state, false otherwise.
    public boolean setAndCheckInitialState(){
        sampleSensors();
        if(backLimitSwitchPressed){
            intakePosAct = IntakePos.Retract;
            intakePosDes = IntakePos.Retract;
            prevIntakePosDes = IntakePos.Retract;
            return true;
        } else {
            return false;
        }
    }


    public void sampleSensors(){
        if(!runSimMode){
            ballDetected = !ballInIntake.get(); //Sensor outputs high for no ball, low for ball 
            backLimitSwitchPressed = backIntakeSwitch.get();
            frontLimitSwitchPressed = frontIntakeSwitch.get();
        }
    }

    public boolean getBackLimitSwitchPressed(){
        return backLimitSwitchPressed;
    }

    public boolean getFrontLimitSwitchPressed(){
        return frontLimitSwitchPressed;
    }

    public void update(){

        sampleSensors();

        if(runSimMode){
            intakePosAct = intakePosDes;
            ballDetected = false;

        } else {

            if(intakePosDes==IntakePos.Extend){
                if(prevIntakePosDes != IntakePos.Extend){
                    //When going to extend, we need to run the motor till the timer has expired
                    if(intakePosAct == IntakePos.Ground){
                        transitionEndTime = Timer.getFPGATimestamp() + groundToExtendTime.get();
                        intakeMotorCmd = -1.0;
                    } else if(intakePosAct == IntakePos.Retract){
                        transitionEndTime = Timer.getFPGATimestamp() + retractedToExtendTime.get();
                        intakeMotorCmd = 1.0;
                    }
                } else {
                    if(Timer.getFPGATimestamp() > transitionEndTime){
                        intakePosAct = IntakePos.Extend;
                        intakeMotorCmd = 0;
                    } else {
                        intakePosAct = IntakePos.InTransit;
                    }
                }
            }else if(intakePosDes==IntakePos.Ground){
                //When going to the ground, just command the motor forward.
                if(frontLimitSwitchPressed){
                    intakeMotorCmd = 0.0;
                    intakePosAct = IntakePos.Ground;
                } else {
                    intakeMotorCmd = 1.0;
                    intakePosAct = IntakePos.InTransit;
                }
            }else if(intakePosDes==IntakePos.Retract){
                //When going to retract, just command the motor backward.
                if(backLimitSwitchPressed){
                    intakeMotorCmd = 0.0;
                    intakePosAct = IntakePos.Retract;
                }else {
                    intakeMotorCmd = -1.0;
                    intakePosAct = IntakePos.InTransit;
                }
            }else{
                //Don't change the setpoint otherwise.
            }

            //Overarching saftey cutouts
            if(frontLimitSwitchPressed){
                if(intakeMotorCmd > 0.0){
                    intakeMotorCmd = 0.0;
                }
            } else if(backLimitSwitchPressed){
                if(intakeMotorCmd < 0.0){
                    intakeMotorCmd = 0.0;
                }
            }

            intakeLeftArmMotor.set(ControlMode.PercentOutput, intakeMotorCmd);
            intakeRightArmMotor.set(ControlMode.PercentOutput, intakeMotorCmd);


            isOnTarget = (intakePosAct == intakePosDes);

            //Intake motor control
            if((ballDetected) && (intakeSpdCmd == IntakeSpd.Intake)){ //If we got a ball, don't Intake
                intakePosDes = IntakePos.Ground;
                intakeRollerCmd = 0;
            }else if((!ballDetected) && (intakeSpdCmd == IntakeSpd.Intake)){ //If we don't, Intake
                intakeRollerCmd = intakeSpeed.get();
            }else if(intakeSpdCmd == IntakeSpd.Stop){ //Whether we have a ball or not, we can Stop and Eject
                intakeRollerCmd = 0;
            }else if(intakeSpdCmd == IntakeSpd.Eject){
                intakeRollerCmd = -1 * ejectSpeed.get();
            }else{ //If for some reason it is confused, don't run the intake
                intakeRollerCmd = 0;
            }
    
            intakeMotor.set(intakeRollerCmd); //motor is no longer

            prevIntakePosDes = intakePosDes;
    

            updateTelemetry();
        }


    }

    public void updateTelemetry(){
        /* Update Telemetry */
        double sampleTimeMS = LoopTiming.getInstance().getLoopStartTimeSec() * 1000.0;
        motorSpeedCmdSig.addSample(sampleTimeMS, intakeMotorCmd);
        rollerSpeedCmdSig.addSample(sampleTimeMS, intakeRollerCmd);
        intakeCmdStateSig.addSample(sampleTimeMS, intakePosDes.toInt());
        ballInIntakeSig.addSample(sampleTimeMS, ballDetected);
        isOnTargetSig.addSample(sampleTimeMS, isOnTarget);  
        intakeAtBackLimit.addSample(sampleTimeMS, backLimitSwitchPressed);  
        intakeAtFrontLimit.addSample(sampleTimeMS, frontLimitSwitchPressed);  
    }

    public boolean isBallDetected(){
        return ballDetected;
    }

}