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
import frc.lib.Calibration.Calibration;
import frc.lib.DataServer.Signal;

public class IntakeControl {

    DigitalInput ballInIntake;

    Spark intakeMotor;

    Solenoid intakeArmBar;
    Integer loopCounter = 10;
    IntakePos currentPosition = IntakePos.Retract;

    Signal retractStateSig;
    Signal motorSpeedCmdSig;
    Signal ballInIntakeSig;

    Calibration intakeSpeed;
    Calibration ejectSpeed;

    // You will want to rename all instances of "EmptyClass" with your actual class name and "empty" with a variable name
    private static IntakeControl empty = null;

    public static synchronized IntakeControl getInstance() {
        if(empty == null)
            empty = new IntakeControl();
        return empty;
    }
    
    private IntakeControl(){

        intakeSpeed = new Calibration("Intake Speed", 0.25, 0, 1);
        ejectSpeed = new Calibration("Eject Speed", 0.25, 0, 1);
        ballInIntake = new DigitalInput(RobotConstants.BALL_INTAKE_PORT);
        intakeMotor = new Spark(RobotConstants.INTAKE_MOTOR_PORT);
        intakeArmBar = new Solenoid(RobotConstants.INTAKE_ARM_BAR_PORT);

        retractStateSig = new Signal("Intake Extension State", "Extension");
        motorSpeedCmdSig = new Signal("Intake Motor Command", "cmd");
        ballInIntakeSig = new Signal("Intake Ball Present", "bool");
    }

    //Intake positions that can be requested
    public enum IntakePos {
        Extend(0), Retract(1);

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

    public void setPositionCmd(IntakePos pos_in){
        if(pos_in == IntakePos.Retract){
            intakePosCmd = IntakePos.Retract;
        }else if(pos_in == IntakePos.Extend){
            intakePosCmd = IntakePos.Extend;
        }
    }

    public void setSpeedCmd(IntakeSpd spd_in){
        if(spd_in == IntakeSpd.Stop){
            intakeSpdCmd = IntakeSpd.Stop;
        }else if(spd_in == IntakeSpd.Intake){
            intakeSpdCmd = IntakeSpd.Intake;
        }else if(spd_in == IntakeSpd.Eject){
            intakeSpdCmd = IntakeSpd.Eject;
        }

    }

    public void update(){
        double intakeMotorCmd = 0;
        boolean ballDetected = false;

        //Pneumatic arm bar thingy control
        if(intakePosCmd == IntakePos.Retract){
            intakeArmBar.set(false);
        }else if(intakePosCmd == IntakePos.Extend){
            intakeArmBar.set(true);
        }else{ //if for some reason it is confused, pick up the intake arm bar thingy
            intakeArmBar.set(false);
        }

        ballDetected = ballInIntake.get();

        //Intake motor control
        if((ballDetected == true) && (intakeSpdCmd == IntakeSpd.Intake)){ //If we got a ball, don't Intake
            intakeMotorCmd = 0;
        }else if((ballDetected == false) && (intakeSpdCmd == IntakeSpd.Intake)){ //If we don't, Intake
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
        if(intakeArmBar.get() == true){
            
            if(loopCounter == 0){
                currentPosition = IntakePos.Extend; 
            }
            else {
                loopCounter = loopCounter - 1;
            }
        }

        if(intakeArmBar.get() == false){
            loopCounter = 10;
            currentPosition = IntakePos.Retract;
        }
        

        /* Update Telemetry */
        double sample_time_ms = LoopTiming.getInstance().getLoopStartTime_sec() * 1000.0;
        retractStateSig.addSample(sample_time_ms, currentPosition.toInt());
        motorSpeedCmdSig.addSample(sample_time_ms, intakeMotorCmd);
        ballInIntakeSig.addSample(sample_time_ms, ballDetected);
    }

    public IntakePos getEstimatedPosition() {
        return currentPosition;
    }
}