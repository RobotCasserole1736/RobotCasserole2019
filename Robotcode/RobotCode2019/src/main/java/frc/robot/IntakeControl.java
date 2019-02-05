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
import edu.wpi.first.wpilibj.Solenoid;
import edu.wpi.first.wpilibj.Spark;
import edu.wpi.first.wpilibj.PWMVictorSPX;
import frc.lib.Calibration.Calibration;
import frc.lib.DataServer.Signal;

public class IntakeControl {

    DigitalInput ballInIntake;

    Spark intakeMotor;

    Solenoid intakeArmBar;
    int loopCounter = 0;
    IntakePos currentPosition = IntakePos.Retract;

    Signal retractStateEstSig;
    Signal retractStateCmdSig;
    Signal motorSpeedCmdSig;
    Signal ballInIntakeSig;

    Calibration intakeSpeed;
    Calibration ejectSpeed;
    Calibration extendTime;

    DriverController dController;
    OperatorController opController;
    Arm arm;

    boolean ballDetected = false;

    private static IntakeControl iControl = null;

    public static synchronized IntakeControl getInstance() {
        if(iControl == null)
            iControl = new IntakeControl();
        return iControl;
    }
    
    private IntakeControl(){
        intakeSpeed = new Calibration("Intake Intake Speed (motor cmd)", 0.25, 0, 1);
        ejectSpeed = new Calibration("Intake Eject Speed (motor cmd)", 0.25, 0, 1);
        extendTime = new Calibration("Intake Est Extend Time (sec)", 0.500, 0, 5);
        ballInIntake = new DigitalInput(RobotConstants.BALL_INTAKE_PORT);
        intakeMotor = new Spark(RobotConstants.INTAKE_MOTOR_PORT);
        intakeArmBar = new Solenoid(RobotConstants.INTAKE_ARM_BAR_PORT);

        dController = DriverController.getInstance();
        opController = OperatorController.getInstance();
        arm = Arm.getInstance();
        retractStateEstSig = new Signal("Intake Estimated Position", "Intake Pos Enum");
        retractStateCmdSig = new Signal("Intake Commanded Position", "Intake Pos Enum");
        motorSpeedCmdSig = new Signal("Intake Motor Command", "cmd");
        ballInIntakeSig = new Signal("Intake Ball Present", "bool");
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

    //Start with intake retracted and stopped
    private IntakePos intakePosCmd = IntakePos.Retract;
    private IntakeSpd intakeSpdCmd = IntakeSpd.Stop;

    public void setPositionCmd(IntakePos posIn){
        if(posIn == IntakePos.Retract){
            intakePosCmd = IntakePos.Retract;
        }else if(posIn == IntakePos.Extend){
            intakePosCmd = IntakePos.Extend;
        }
    }

    public void setSpeedCmd(IntakeSpd speedIn){
        if(speedIn == IntakeSpd.Stop){
            intakeSpdCmd = IntakeSpd.Stop;
        }else if(speedIn == IntakeSpd.Intake){
            intakeSpdCmd = IntakeSpd.Intake;
        }else if(speedIn == IntakeSpd.Eject){
            intakeSpdCmd = IntakeSpd.Eject;
        }

    }

    public void forceStop(){
        //TODO - Implement a safety method to stop motors when called
    }

    public boolean isAtDesPos(){
        //TODO: add logic to determine if the intake has reached the commanded position or not.
        return true;
    }


    public void update(){
        double intakeMotorCmd = 0;

        if(MatchState.getInstance().GetPeriod() != MatchState.Period.OperatorControl &&
           MatchState.getInstance().GetPeriod() != MatchState.Period.Autonomous) {
            //Start intake within frame perimiter and in safe state
            setPositionCmd(IntakePos.Retract);
            setSpeedCmd(IntakeSpd.Stop);
        }

        //Pneumatic arm bar thingy control
        if(intakePosCmd == IntakePos.Retract){
            intakeArmBar.set(false);
        }else if(intakePosCmd == IntakePos.Extend){
            intakeArmBar.set(true);
        }else if(intakePosCmd == IntakePos.Ground){
            //TODO - handle position on ground
        }else{ //if for some reason it is confused, pick up the intake arm bar thingy
            intakeArmBar.set(false);
        }

        ballDetected = !ballInIntake.get(); //Sensor outputs high for no ball, low for ball 

        //Intake motor control
        if((ballDetected) && (intakeSpdCmd == IntakeSpd.Intake)){ //If we got a ball, don't Intake
            intakeMotorCmd = 0;
        }else if((!ballDetected) && (intakeSpdCmd == IntakeSpd.Intake)){ //If we don't, Intake
            intakeMotorCmd = intakeSpeed.get();
        }else if(intakeSpdCmd == IntakeSpd.Stop){ //Whether we have a ball or not, we can Stop and Eject
            intakeMotorCmd = 0;
        }else if(intakeSpdCmd == IntakeSpd.Eject){
            intakeMotorCmd = -1 * ejectSpeed.get();
        }else{ //If for some reason it is confused, don't run the intake
            intakeMotorCmd = 0;
        }

        intakeMotor.set(intakeMotorCmd);

        // Calcualte an estimate of current position
        if(intakePosCmd == IntakePos.Extend){
            
            if(loopCounter == 0){
                currentPosition = IntakePos.Extend; 
            }
            else {
                loopCounter--;
            }
        }

        if(intakePosCmd == IntakePos.Retract){
            loopCounter = (int)Math.floor(extendTime.get()/0.02);
            currentPosition = IntakePos.Retract;
        }
        
        /* Update Telemetry */
        double sampleTimeMS = LoopTiming.getInstance().getLoopStartTimeSec() * 1000.0;
        retractStateEstSig.addSample(sampleTimeMS, currentPosition.toInt());
        retractStateCmdSig.addSample(sampleTimeMS, intakePosCmd.toInt());
        motorSpeedCmdSig.addSample(sampleTimeMS, intakeMotorCmd);
        ballInIntakeSig.addSample(sampleTimeMS, ballDetected);
    }

    public IntakePos getEstimatedPosition() {
        return currentPosition;
    }

    public boolean isBallDetected(){
        return ballDetected;
    }
}