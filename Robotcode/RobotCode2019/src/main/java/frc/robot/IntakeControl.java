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
import edu.wpi.first.wpilibj.AnalogPotentiometer;
import edu.wpi.first.wpilibj.PIDSource;
import frc.lib.Calibration.Calibration;
import frc.lib.DataServer.Signal;

public class IntakeControl implements PIDSource{

    DigitalInput ballInIntake;

    Spark intakeMotor;
    PWMVictorSPX intakeLeftArmMotor;
    PWMVictorSPX intakeRightArmMotor;

    AnalogPotentiometer leftArmPot;
    AnalogPotentiometer rightArmPot;
    
    int loopCounter = 0;
    double currentLeftPosition = 0;
    double currentRightPosition = 0;

    Signal retractStateEstSig;
    Signal retractStateCmdSig;
    Signal motorSpeedCmdSig;
    Signal ballInIntakeSig;

    Calibration intakeSpeed;
    Calibration ejectSpeed;
    Calibration extendTime;
    Calibration retractAngle;
    Calibration extendAngle;
    Calibration groundAngle;
    Calibration angleRange;


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

        //Properly set calibrations
        retractAngle = new Calibration("Angle of Retracted State (deg)", 0);
        extendAngle = new Calibration("Angle of Extended State (deg)", 90);
        groundAngle = new Calibration("Angle of Ground State (deg)", 115);
        angleRange = new Calibration("Range of Potentiometer (deg)", 315);
        
        ballInIntake = new DigitalInput(RobotConstants.BALL_INTAKE_PORT);
        intakeMotor = new Spark(RobotConstants.INTAKE_MOTOR_PORT);
        //Change Constants
        intakeLeftArmMotor = new PWMVictorSPX(RobotConstants.INTAKE_MOTOR_PORT);
        //Change Constants
        intakeRightArmMotor = new PWMVictorSPX(RobotConstants.INTAKE_MOTOR_PORT);
        //Change Constants
        leftArmPot = new AnalogPotentiometer(RobotConstants.SOMETHING);
        //Change Constants
        rightArmPot = new AnalogPotentiometer(RobotConstants.SOMETHING);

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
        }else if(posIn == IntakePos.Ground){
            intakePosCmd = IntakePos.Ground;
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
        //change those values from 0
        if(currentLeftPosition==0 && currentRightPosition==0){
            return true;
        }else{
            return false;
        }
        //TODO: add logic to determine if the intake has reached the commanded position or not.
        
    }

    double convertVoltsToDeg(double voltage_in){
        //DEFINE THE STUFF IN HERE
        return (voltage_in - LowerLimitVoltage) * (angleRange.get() - LowerLimitDegrees) / (UpperLimitVoltage - LowerLimitVoltage) + LowerLimitDegrees;
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
            if(currentLeftPosition != retractAngle.get()){

            }
            if(currentRightPosition != retractAngle.get()){
                
            }

        }else if(intakePosCmd == IntakePos.Extend){
            if(currentLeftPosition != extendAngle.get()){

            }
            if(currentRightPosition != extendAngle.get()){
                
            }

        }else if(intakePosCmd == IntakePos.Ground){
            if(currentLeftPosition != groundAngle.get()){

            }
            if(currentRightPosition != groundAngle.get()){
                
            }
            //TODO - handle position on ground
        }else{ //if for some reason it is confused, pick up the intake arm bar thingy
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

        // Calculate current position
            currentLeftPosition = convertVoltstoDeg(leftArmPot.get());
            currentRightPosition = convertVoltstoDeg(rightArmPot.get());
        
        /* Update Telemetry */
        double sampleTimeMS = LoopTiming.getInstance().getLoopStartTimeSec() * 1000.0;
        //FIX THIS MILES
        retractStateEstSig.addSample(sampleTimeMS, currentPosition.toInt());
        retractStateCmdSig.addSample(sampleTimeMS, intakePosCmd.toInt());
        motorSpeedCmdSig.addSample(sampleTimeMS, intakeMotorCmd);
        ballInIntakeSig.addSample(sampleTimeMS, ballDetected);
    }

    public IntakePos getEstimatedPosition() {
        //POTENTIALLY UPDATE THINGS BASED OFF OF THIS AS IT NOW RETURNS TWO THINGS
        return currentLeftPosition;
        return currentRightPosition;
    }

    public boolean isBallDetected(){
        return ballDetected;
    }
}