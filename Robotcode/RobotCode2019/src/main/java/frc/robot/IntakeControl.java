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

    Signal solenoidArmState;

    Calibration intakeSpeed;
    Calibration ejectSpeed;

    DriverController dController;
    OperatorController opController;
    Arm arm;

    private static IntakeControl iControl = null;

    public static synchronized IntakeControl getInstance() {
        if(iControl == null)
            iControl = new IntakeControl();
        return iControl;
    }
    
    private IntakeControl(){
        intakeSpeed = new Calibration("Intake Speed", 0.25, 0, 1);
        ejectSpeed = new Calibration("Eject Speed", 0.25, 0, 1);
        ballInIntake = new DigitalInput(RobotConstants.BALL_INTAKE_PORT);
        intakeMotor = new Spark(RobotConstants.INTAKE_MOTOR_PORT);
        intakeArmBar = new Solenoid(RobotConstants.INTAKE_ARM_BAR_PORT);

        dController = DriverController.getInstance();
        opController = OperatorController.getInstance();
        arm = Arm.getInstance();
        solenoidArmState = new Signal("Intake State", "B");
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

    public void update(){
        if(MatchState.getInstance().GetPeriod() == MatchState.Period.OperatorControl ||
           MatchState.getInstance().GetPeriod() == MatchState.Period.Autonomous) {
            //Arbitrate Intake commands from driver and operator and arm
            if(dController.getIntakePosReq() == IntakePos.Extend || 
            opController.getIntakePosReq() == IntakePos.Extend || 
            arm.intakeExtendOverride() == true) {
                setPositionCmd(IntakePos.Extend);
            } else {
                setPositionCmd(IntakePos.Retract);
            }

            if(dController.getIntakeSpdReq() == IntakeSpd.Eject ||
            opController.getIntakeSpdReq() == IntakeSpd.Eject ){
                setSpeedCmd(IntakeSpd.Eject);
            } else if(dController.getIntakeSpdReq() == IntakeSpd.Intake ||
                    opController.getIntakeSpdReq() == IntakeSpd.Intake ){
                setSpeedCmd(IntakeSpd.Intake);
            } else {
                setSpeedCmd(IntakeSpd.Stop);
            }
        }
        else
        {
            //Start intake within frame perimiter and in safe state
            setPositionCmd(IntakePos.Retract);
            setSpeedCmd(IntakeSpd.Stop);
        }

        //Pneumatic arm bar thingy control
        if(intakePosCmd == IntakePos.Retract){
            intakeArmBar.set(false);
        }else if(intakePosCmd == IntakePos.Extend){
            intakeArmBar.set(true);
        }else{ //if for some reason it is confused, pick up the intake arm bar thingy
            intakeArmBar.set(false);
        }

        //Intake motor control
        if((ballInIntake.get()) && (intakeSpdCmd == IntakeSpd.Intake)){ //If we got a ball, don't Intake
            intakeMotor.set(0);
        }else if(!ballInIntake.get() && (intakeSpdCmd == IntakeSpd.Intake)){ //If we don't, Intake
            intakeMotor.set(intakeSpeed.get());
        }else if(intakeSpdCmd == IntakeSpd.Stop){ //Whether we have a ball or not, we can Stop and Eject
            intakeMotor.set(0);
        }else if(intakeSpdCmd == IntakeSpd.Eject){
            intakeMotor.set(-1 * (ejectSpeed.get()));
        }else{ //If for some reason it is confused, don't run the intake
            intakeMotor.set(0);
        }

        // Calcualte an estimate of current position
        if(intakeArmBar.get()){
            
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
        double sample_time_ms = LoopTiming.getInstance().getLoopStartTimeSec() * 1000.0;
        solenoidArmState.addSample(sample_time_ms, currentPosition.toInt());
    }

    public IntakePos getEstimatedPosition() {
        return currentPosition;
    }
}