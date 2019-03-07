package frc.robot;

import frc.lib.AutoSequencer.AutoEvent;
import frc.lib.AutoSequencer.AutoSequencer;
import frc.lib.DataServer.Signal;
import frc.lib.Util.CrashTracker;
import frc.robot.Arm.ArmPos;
import frc.robot.IntakeControl.IntakePos;
import frc.robot.IntakeControl.IntakeSpd;
import frc.robot.OperatorController.ArmPosCmd;
import frc.robot.PEZControl.PEZPos;
import frc.robot.auto.Delay;
import frc.robot.auto.EjectBall;
import frc.robot.auto.MoveArmIntakePos;
import frc.robot.auto.MoveArmMidPos;
import frc.robot.auto.MoveGripper;
import frc.robot.auto.MoveIntake;

/*
 *******************************************************************************************
 * Copyright (C) 2019 FRC Team 1736 Robot Casserole - www.robotcasserole.org
 *******************************************************************************************
 *
 * This software is released under the MIT Licence - see the license.txt
 *    file in the root of this repo.
 *
 * Non-legally-binding statement from Team 1736:
 *    Thank you for taking the time to read through our software! We hope you
 *     find it educational and informative! 
 *    Please feel free to snag our software for your own use in whatever project
 *     you have going on right now! We'd love to be able to help out! Shoot us 
 *     any questions you may have, all our contact info should be on our website
 *     (listed above).
 *    If you happen to end up using our software to make money, that is wonderful!
 *     Robot Casserole is always looking for more sponsors, so we'd be very appreciative
 *     if you would consider donating to our club to help further STEM education.
 */

public class Superstructure {

    OpMode actualOpMode;
    OpMode prevActualOpMode;

    OpMode cmdOpMode;
    OpMode prevCmdOpMode;

    PEZPos gripperPosCmd;
    PEZControl gripper;

    IntakeControl intake;

    OperatorController opCtrl;

    Arm arm;

    AutoSequencer seq;

    Signal cmdModeSig;
    Signal actModeSig;
    Signal seqStepSig;

    public enum OpMode {
        Hatch(0), CargoIntake(1), CargoCarry(2), TransitionToCargoIntake(3), TransitoinToCargoCarry(4), TransitionToHatch(5), None(-1);
        public final int value;

        private OpMode(int value) {
            this.value = value;
        }

        public int toInt(){
            return this.value;
        }
    }


    /* Singleton stuff */
    private static Superstructure inst = null;
    public static synchronized Superstructure getInstance() {
        if(inst == null)
            inst = new Superstructure();
        return inst;
    }

    private Superstructure(){
        actualOpMode = OpMode.None;
        prevActualOpMode = OpMode.None;
        cmdOpMode = OpMode.None;
        prevCmdOpMode = OpMode.None;
        gripper = PEZControl.getInstance();
        opCtrl = OperatorController.getInstance();
        intake = IntakeControl.getInstance();
        arm = Arm.getInstance();

        seq = new AutoSequencer("Superstructure");

        cmdModeSig = new Signal("Superstructure Commanded Mode", "OpMode");
        actModeSig = new Signal("Superstructure Actual Mode", "OpMode");
        seqStepSig = new Signal("Superstructure Sequencer Step", "idx");
    }

    public void update(){
        prevActualOpMode = actualOpMode;
        prevCmdOpMode = cmdOpMode;


        ArmPosCmd opArmPosCmd = OperatorController.getInstance().getArmPosReq();

        //Determine new Op Mode
        if(actualOpMode == OpMode.Hatch){
            if(opArmPosCmd == ArmPosCmd.IntakeCargo){
                cmdOpMode = OpMode.CargoIntake;
            } else {
                //No other operator inputs can change OpMode
            }
        } else if(actualOpMode == OpMode.CargoIntake){
            if(opArmPosCmd == ArmPosCmd.Lower || opArmPosCmd == ArmPosCmd.Middle || opArmPosCmd == ArmPosCmd.Top){
                cmdOpMode = OpMode.CargoCarry;
            } else if(opArmPosCmd == ArmPosCmd.IntakeHatch) {
                cmdOpMode = OpMode.Hatch;
            } else {
                //No other operator inputs can change state
            }
        } else if(actualOpMode == OpMode.CargoCarry) {
            if(opArmPosCmd == ArmPosCmd.IntakeCargo){
                cmdOpMode = OpMode.CargoIntake;
            } else if (opArmPosCmd == ArmPosCmd.IntakeHatch){
                cmdOpMode = OpMode.Hatch;
            }
        } else {
            //Ignore operator commands in other op modes.
        }


        //Process Transitions between modes
        if(prevCmdOpMode == OpMode.Hatch){
            //Transitions out of OpMode
            if(cmdOpMode == OpMode.CargoIntake){
                //Start transition from hatch mode to cargo mode.
                actualOpMode = OpMode.TransitionToCargoIntake;
                seq.clearAllEvents();
                AutoEvent parent = new EjectBall(); //Eject any ball that is presently in the intake
                parent.addChildEvent(new MoveGripper(PEZPos.HatchRelease)); //Drop any gamepiece we may currently have
                parent.addChildEvent(new MoveArmMidPos(OpMode.CargoCarry)); //Move arm toward intake position
                seq.addEvent(parent); 
                seq.addEvent(new MoveIntake(IntakePos.Ground)); //Extend the intake so it's out of the way of the arm
                seq.addEvent(new MoveGripper(PEZPos.CargoGrab)); //Move gripper to be cargo grab so it's out of the way of the frame
                seq.addEvent(new MoveArmIntakePos(OpMode.CargoIntake)); //Lower the arm into the frame
                seq.addEvent(new MoveGripper(PEZPos.CargoRelease)); //Return gripper to the neutral position in prep for recieving a ball.
                seq.addEvent(new MoveIntake(IntakePos.Extend)); //Pull the intake to the proper position for intake
                seq.start();
            } else if(cmdOpMode == OpMode.CargoCarry){
                //Invalid, this transition should not occurr
                CrashTracker.logAndPrint("[Superstructure] Error: Superstructure - invalid transition from Hatch to CargoCarry");
                cmdOpMode = OpMode.Hatch;
            }

        } else if(prevCmdOpMode == OpMode.CargoCarry){
            //Transitions out of CargoCarry
            if(cmdOpMode == OpMode.Hatch){
                //Start transition from Cargo Intake mode to Hatch mode.
                actualOpMode = OpMode.TransitionToHatch;
                seq.clearAllEvents();
                seq.addEvent(new EjectBall()); //Eject any ball that is presently in the intake
                seq.addEvent(new MoveGripper(PEZPos.HatchRelease)); //Drop any gamepiece we may have
                seq.addEvent(new Delay(0.2)); //Wait for gamepiece to actually drop.
                seq.addEvent(new MoveArmMidPos(OpMode.CargoCarry)); //move the arm up and out of the way of the intake
                seq.addEvent(new MoveIntake(IntakePos.Retract)); //pull the intake back in
                seq.addEvent(new MoveArmIntakePos(OpMode.Hatch)); // Move the arm to the hatch intake position in prep for grabbing a hatch
                seq.start();
            } else if(cmdOpMode == OpMode.CargoIntake){
                //Start transition from Cargo Carry to Cargo Intake
                actualOpMode = OpMode.TransitionToCargoIntake;
                seq.clearAllEvents();
                seq.addEvent(new EjectBall()); //Eject any ball that is presently in the intake
                seq.addEvent(new MoveGripper(PEZPos.CargoRelease)); //Drop any gamepiece we may have
                seq.addEvent(new Delay(0.2)); //Wait for gamepiece to actually drop.
                seq.addEvent(new MoveGripper(PEZPos.CargoGrab)); //Move grabber to cargo grab position
                seq.addEvent(new MoveArmMidPos(OpMode.CargoCarry)); //Move arm out of the way of the intake
                seq.addEvent(new MoveIntake(IntakePos.Ground)); //put the intake out of the way
                seq.addEvent(new MoveArmIntakePos(OpMode.CargoIntake)); // Move the arm to the hatch intake position in prep for grabbing a hatch
                seq.addEvent(new MoveGripper(PEZPos.CargoRelease)); //Move the gripper to the neutral position in prep for grabbing a hatch
                seq.addEvent(new MoveIntake(IntakePos.Extend)); //Move the intake in prep for picking up stuff
                seq.start();
            }
        } else if(prevCmdOpMode == OpMode.CargoIntake){
            //Handle transitions out of CargoIntake mode
            if(cmdOpMode == OpMode.CargoCarry){
                //Start transition from Cargo Intake mode to Cargo Carry.
                actualOpMode = OpMode.TransitoinToCargoCarry;
                seq.clearAllEvents();
                seq.addEvent(new MoveGripper(PEZPos.CargoGrab)); //Ensure we're grabbing the ball
                seq.addEvent(new MoveIntake(IntakePos.Ground)); //Put the intake all the way out, out of the way of the the arm and ball as they come up
                seq.addEvent(new MoveArmMidPos(OpMode.CargoCarry)); //Move arm to the lower position by default.
                seq.addEvent(new MoveIntake(IntakePos.Retract)); //Ensure the intake is all the way back in the robot
                seq.start();
            } else if(cmdOpMode == OpMode.Hatch) {
                //Start transition from Cargo Intake mode to Cargo Carry.
                actualOpMode = OpMode.TransitionToHatch;
                seq.clearAllEvents();
                seq.addEvent(new MoveGripper(PEZPos.CargoGrab)); //Ensure we're grabbing to stay out of the way of the frame as we raise up
                seq.addEvent(new MoveIntake(IntakePos.Ground)); //Put the intake all the way out, out of the way of the the arm and ball as they come up
                seq.addEvent(new MoveArmMidPos(OpMode.Hatch)); //Move arm to the lower position by default.
                AutoEvent parent = new MoveIntake(IntakePos.Retract);
                parent.addChildEvent(new MoveGripper(PEZPos.HatchRelease));//Drop any gamepiece we may have
                seq.addEvent(parent);
                seq.addEvent(new MoveArmIntakePos(OpMode.Hatch)); //Move arm to the lower position by default.
                seq.start();
            }

        }
        

        //Process during-state updates
        if(actualOpMode == OpMode.TransitionToCargoIntake){
            //Handle actions to take while transitioning to Cargo mode
            seq.update();
            if(seq.isRunning() == false){
                actualOpMode = OpMode.CargoIntake;
            }

        } else if(actualOpMode == OpMode.TransitionToHatch){
            //Handle actions to take while transitioning to Hatch mode
            seq.update();
            if(seq.isRunning() == false){
                actualOpMode = OpMode.Hatch;
            }

        } else if(actualOpMode == OpMode.TransitoinToCargoCarry){
            //Handle actions to take while transitioning to Hatch mode
            seq.update();
            if(seq.isRunning() == false){
                actualOpMode = OpMode.CargoCarry;
            }

        } else if(actualOpMode == OpMode.CargoIntake) {
            //Handle actions to take while in to Cargo Intake mode

            //Map grab/release requests to gripper positions
            //Additionally, autmoatically go to grab when we detect a ball
            if(opCtrl.getGampieceReleaseRequest() || opCtrl.getIntakeSpdReq() == IntakeSpd.Eject){
                gripperPosCmd = PEZPos.CargoRelease;
                intake.setPositionCmd(IntakePos.Extend);
            } else if(opCtrl.getGampieceGrabRequest() || intake.isBallDetected()){
                gripperPosCmd = PEZPos.CargoGrab;
                intake.setPositionCmd(IntakePos.Ground);
            } else {
                gripperPosCmd = PEZPos.None;
            }
            gripper.setPositionCmd(gripperPosCmd);

            //Operator can command intake speed, but position is fixed
            intake.setSpeedCmd(opCtrl.getIntakeSpdReq());

            //Ignore all arm commands


        } else if(actualOpMode == OpMode.CargoCarry) {
            //Handle actions to take while in to Cargo Intake mode

            //Map grab/release requests to gripper positions
            if(opCtrl.getGampieceGrabRequest()){
                gripperPosCmd = PEZPos.CargoGrab;
            } else if(opCtrl.getGampieceReleaseRequest()){
                gripperPosCmd = PEZPos.CargoRelease;
            } else {
                gripperPosCmd = PEZPos.None;
            }
            gripper.setPositionCmd(gripperPosCmd);

            //Operator can command intake speed, but position is fixed
            intake.setSpeedCmd(opCtrl.getIntakeSpdReq());
            intake.setPositionCmd(IntakePos.Retract);

            //Allow all hatch-related arm position commands
            if(opCtrl.getArmPosReq() == ArmPosCmd.Lower){
                arm.setPositionCmd(ArmPos.LowerCargo);
            } else if(opCtrl.getArmPosReq() == ArmPosCmd.Middle){
                arm.setPositionCmd(ArmPos.MiddleCargo);
            } else if(opCtrl.getArmPosReq() == ArmPosCmd.Top){
                arm.setPositionCmd(ArmPos.TopCargo);
            } else {
                arm.setPositionCmd(ArmPos.None);
            }

            //Limit manual motion command if we're on the highway to the danger zone.
            if(!arm.isAboveDangerZone() && opCtrl.getArmManualPosCmd() < 0.0 ){
                arm.setManualMovementCmd(0.0);
            } else {
                arm.setManualMovementCmd(opCtrl.getArmManualPosCmd());
            }


        } else if(actualOpMode == OpMode.Hatch) {
            //Handle actions to take while in to Hatch mode

            //Map grab/release requests to gripper positions
            if(opCtrl.getGampieceGrabRequest()){
                gripperPosCmd = PEZPos.HatchGrab;
            } else if(opCtrl.getGampieceReleaseRequest()){
                gripperPosCmd = PEZPos.HatchRelease;
            } else {
                gripperPosCmd = PEZPos.None;
            }


            //Keep intake at inside position at all times.
            intake.setSpeedCmd(IntakeSpd.Stop);
            intake.setPositionCmd(IntakePos.Retract);

            //Allow all hatch-related arm position commands
            if(opCtrl.getArmPosReq() == ArmPosCmd.IntakeHatch){
                arm.setPositionCmd(ArmPos.IntakeHatch);
            } else if(opCtrl.getArmPosReq() == ArmPosCmd.Lower){
                arm.setPositionCmd(ArmPos.LowerHatch);
            } else if(opCtrl.getArmPosReq() == ArmPosCmd.Middle){
                arm.setPositionCmd(ArmPos.MiddleHatch);
            } else if(opCtrl.getArmPosReq() == ArmPosCmd.Top){
                arm.setPositionCmd(ArmPos.TopHatch);
            } else {
                arm.setPositionCmd(ArmPos.None);
            }
            
            gripper.setPositionCmd(gripperPosCmd);

            //Limit manual motion command if we're on the highway to the danger zone.
            if(!arm.isAboveDangerZone() && opCtrl.getArmManualPosCmd() < 0.0 ){
                arm.setManualMovementCmd(0.0);
            } else {
                arm.setManualMovementCmd(opCtrl.getArmManualPosCmd());
            }

        } else {
            //Do nothing until some operation mode is commanded.
        }

        /* Update Telemetry */
        double sample_time_ms = LoopTiming.getInstance().getLoopStartTimeSec()*1000.0;
        cmdModeSig.addSample(sample_time_ms, cmdOpMode.toInt());
        actModeSig.addSample(sample_time_ms, actualOpMode.toInt());
        seqStepSig.addSample(sample_time_ms, seq.getEventIndex());

    }

    public void setInitialOpMode(OpMode initMode){
        actualOpMode = initMode;
        prevActualOpMode = initMode;
        cmdOpMode = initMode;
        prevCmdOpMode = initMode;
    }

    public OpMode getActualOpMode(){
        return actualOpMode;
    }


    public PEZPos getGripperPosCmd(){
        return gripperPosCmd;
    }

    public String getOpModeString(){
        return actualOpMode.toString();
    }





}