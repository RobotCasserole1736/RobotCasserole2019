package frc.robot;

import frc.lib.AutoSequencer.AutoSequencer;
import frc.robot.Arm.ArmPos;
import frc.robot.IntakeControl.IntakePos;
import frc.robot.IntakeControl.IntakeSpd;
import frc.robot.OperatorController.ArmPosCmd;
import frc.robot.PEZControl.PEZPos;
import frc.robot.auto.Delay;
import frc.robot.auto.EjectBall;
import frc.robot.auto.MoveArmIntakePos;
import frc.robot.auto.MoveArmLowPos;
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

    public enum OpMode {
        Hatch(0), Cargo(1), TransitionToCargo(2), TransitionToHatch(3), None(-1);
        public final int value;

        private OpMode(int value) {
            this.value = value;
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
        gripper = PEZControl.getInstance();
        opCtrl = OperatorController.getInstance();
        intake = IntakeControl.getInstance();
        arm = Arm.getInstance();
    }

    public void update(){
        prevActualOpMode = actualOpMode;
        prevCmdOpMode = cmdOpMode;

        cmdOpMode = OperatorController.getInstance().getOpMode();

        if(prevCmdOpMode == OpMode.Hatch  && cmdOpMode == OpMode.Cargo){
            //Start transition from hatch mode to cargo mode.
            actualOpMode = OpMode.TransitionToCargo;
            seq = new AutoSequencer("ToCargo");
            //TODO: Combine the envents which can happen in parallel
            seq.addEvent(new EjectBall()); //Eject any ball that is presently in the intake
            seq.addEvent(new MoveGripper(PEZPos.Release)); //Drop any gamepiece we may currently have
            seq.addEvent(new MoveArmLowPos(OpMode.Cargo)); //Move arm toward intake position
            seq.addEvent(new MoveIntake(IntakePos.Extend)); //Extend the intake so it's out of the way of the arm
            seq.addEvent(new MoveGripper(PEZPos.CargoGrab)); //Move gripper to be cargo grab so it's out of the way of the frame
            seq.addEvent(new MoveArmIntakePos(OpMode.Cargo)); //Lower the arm into the frame
            seq.addEvent(new MoveGripper(PEZPos.Release)); //Return gripper to the neutral position in prep for recieving a ball.

            seq.update();

        } else if(prevCmdOpMode == OpMode.Cargo  && cmdOpMode == OpMode.Hatch){
            //Start transition from Cargo mode to Hatch mode.
            actualOpMode = OpMode.TransitionToHatch;
            seq = new AutoSequencer("ToHatch");
            //TODO: Combine the envents which can happen in parallel
            seq.addEvent(new EjectBall()); //Eject any ball that is presently in the intake
            seq.addEvent(new MoveGripper(PEZPos.Release)); //Drop any gamepiece we may have
            seq.addEvent(new Delay(0.2)); //Wait for gamepiece to actually drop.
            seq.addEvent(new MoveGripper(PEZPos.CargoGrab)); //Move grabber to cargo grab position
            seq.addEvent(new MoveArmLowPos(OpMode.Hatch)); //Move arm out of the way of the intake
            seq.addEvent(new MoveIntake(IntakePos.Retract)); //Pull the intake back within the robot
            seq.addEvent(new MoveArmIntakePos(OpMode.Hatch)); // Move the arm to the hatch intake position in prep for grabbing a hatch
            seq.addEvent(new MoveGripper(PEZPos.Release)); //Move the gripper to the neutral position in prep for grabbing a hatch

            seq.update();

        } else if(actualOpMode == OpMode.TransitionToCargo){
            //Handle actions to take while transitioning to Cargo mode
            seq.update();
            if(seq.isRunning() == false){
                actualOpMode = OpMode.Cargo;
            }

        } else if(actualOpMode == OpMode.TransitionToHatch){
            //Handle actions to take while transitioning to Hatch mode
            seq.update();
            if(seq.isRunning() == false){
                actualOpMode = OpMode.Hatch;
            }

        } else if(actualOpMode == OpMode.Cargo) {
            //Handle actions to take while in to Cargo mode

            //Map grab/release requests to gripper positions
            if(opCtrl.getGampieceGrabRequest()){
                gripperPosCmd = PEZPos.CargoGrab;
            } else if(opCtrl.getGampieceReleaseRequest()){
                gripperPosCmd = PEZPos.Release;
            } else {
                gripperPosCmd = PEZPos.None;
            }
            gripper.setPositionCmd(gripperPosCmd);

            //TODO Map relevant operator inputs to commands, or split this into muilple states

        } else if(actualOpMode == OpMode.Hatch) {
            //Handle actions to take while in to Hatch mode

            //Map grab/release requests to gripper positions
            if(opCtrl.getGampieceGrabRequest()){
                gripperPosCmd = PEZPos.HatchGrab;
            } else if(opCtrl.getGampieceReleaseRequest()){
                gripperPosCmd = PEZPos.Release;
            } else {
                gripperPosCmd = PEZPos.None;
            }
            gripper.setPositionCmd(gripperPosCmd);

            //Keep intake at inside position at all times.
            intake.setSpeedCmd(IntakeSpd.Stop);
            intake.setPositionCmd(IntakePos.Retract);

            //Allow all hatch-related arm position commands
            if(opCtrl.getArmPosReq() == ArmPosCmd.Intake){
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

            //Limit manual motion command if we're on the highway to the danger zone.
            if(!arm.isAboveDangerZone() && opCtrl.getArmManualPosCmd() < 0.0 ){
                arm.setManualMovementCmd(0.0);
            } else {
                arm.setManualMovementCmd(opCtrl.getArmManualPosCmd());
            }

        } else {
            //Do nothing until some operation mode is commanded.
        }

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






}