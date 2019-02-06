package frc.robot.auto;

import frc.lib.AutoSequencer.AutoEvent;
import frc.lib.AutoSequencer.AutoSequencer;
import frc.robot.JeVoisInterface;
import frc.robot.OperatorController;
import frc.robot.Superstructure;
import frc.robot.Arm.ArmPos;
import frc.robot.PEZControl.PEZPos;
import frc.robot.Superstructure.OpMode;

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

public class Autonomous {
    //State Variables
    boolean prevAutoMoveRequested = false;
    boolean isForward;

    private double visionAngle = 0;

    AutoSequencer seq;

    AutoSeqDistToTgtEst distEst;


    // name and "empty" with a variable name
    private static Autonomous empty = null;

    public static synchronized Autonomous getInstance() {
        if (empty == null)
            empty = new Autonomous();
        return empty;
    }

    // This is the private constructor that will be called once by getInstance() and
    // it should instantiate anything that will be required by the class

    // Name for Autonomous enum is AutoState. Welcome to change if need be.
    public enum AutoState {
        Idle(0), Path_Plan_Approach_Vector(1), Move_Along_Approach_Vector(2), Place_Gamepiece_Low_Level(3), 
        Place_Gamepiece_Mid_Level(4), Place_Gamepiece_Top_Level(5), Backup(6);

        public final int value;

        private AutoState(int value){
            this.value = value;
        }
    }

    //needs something inside of the parentheses, probably
    private Autonomous() {
        distEst = AutoSeqDistToTgtEst.getInstance();
    }

    public void setVisionAngleToTgt(double angle_in) {

    }

    public void setVisionTargetTranslation(double x_in, double y_in) {

    }

    public void setVisionTargetSkewAngle(double angle_deg_in) {

    }

    //The type double was placed to remove the error. Replacement may be neccessary.
    public void setAutoAlignCommand(double cmd_in) {

    }

    public void setFrontDistanceMeas(double distance_ft, boolean dist_avail) {

    }

    public void setRearDistanceMeas(double distance_ft, boolean dist_avail) {

    }

    public void setDrivetrainMeasDist(double right_distance_ft, double left_distance_ft) {

    }

    public ArmPos getArmPosCmd() {
        return ArmPos.None;
    }

    public PEZPos getGripperPosCmd(){
        return PEZPos.None;
    }

    //The return false statements are just to get rid of errors. Replacement neccessary.
    public boolean getIntakeExtendLockout(){
        return false;
    }

    public boolean getAutoSequencerActive(){
        return false;
    }

    public boolean getLeftMotorSpeedCmd_RPM(){
        return false;
    }

    public boolean getRightMotorSpeedCmd_RPM(){
        return false;
    }

    public boolean getHeadingCmd_deg(){
        return false;
    }

    double xTargetOffset = 0;
    double yTargetOffset = 0;
    double targetPositionAngle = 0;


    //Commands called from other parts of the code need to be inputed into the parentheses, I think
    public void update(){
        boolean autoMoveRequested = OperatorController.getInstance().getAutoMove();

        OpMode curOpMode = Superstructure.getInstance().getActualOpMode();
        boolean opModeAllowsAuto = (curOpMode == OpMode.CargoCarry || curOpMode == OpMode.Hatch);

        boolean jeVoisHasNewTarget = false; 
        
        if(autoMoveRequested && !prevAutoMoveRequested && opModeAllowsAuto){
            //Initialize autoMove sequence on rising edge of auto move request from operator, and only if operational mode is not in transistion.
            xTargetOffset = 0;
            yTargetOffset = 0;
            targetPositionAngle = 0;

            JeVoisInterface.getInstance().latchTarget();
        }

        //TODO - populate the "jevois has target" based on reading the "latch" counter, and comparing at least a few readings to ensure the target visuilization is stable
        jeVoisHasNewTarget = true; // A silly and bad algorithm. Change me please.

        //TODO - add timeout here to indicate to driver if no new target is found soon

        if(autoMoveRequested && jeVoisHasNewTarget){

            if(OperatorController.getInstance().getAutoAlignHighReq()){
                isForward = false;
            } else {
                isForward = true;
            }


            xTargetOffset = JeVoisInterface.getInstance().getTgtPositionX();
            yTargetOffset = JeVoisInterface.getInstance().getTgtPositionY();
            targetPositionAngle = JeVoisInterface.getInstance().getTgtAngle();
            
            seq = new AutoSequencer("AutoAlign");

            AutoEvent parent = new AutoSeqPathPlan(xTargetOffset, yTargetOffset, targetPositionAngle); //TODO - handle the case where the path planner can't create a path based on current angle constraints with "getPathAvailable()"
            seq.addEvent(parent);


            parent = new AutoSeqFinalAlign();

            if(OperatorController.getInstance().getAutoAlignLowReq()){
                parent.addChildEvent(new MoveArmLowPos(curOpMode));
            } else if(OperatorController.getInstance().getAutoAlignMidReq()){
                parent.addChildEvent(new MoveArmMidPos(curOpMode));
            } else if(OperatorController.getInstance().getAutoAlignHighReq()){
                parent.addChildEvent(new MoveArmTopPos(curOpMode));
            } else {
                System.out.println("Error invalid Autostate.");
            }

            seq.addEvent(parent);

            if(isForward){
                parent.addChildEvent(new BackupHigh());
            } else {
                parent.addChildEvent(new Backup());
            }
        }

        if(seq != null){
            seq.update();
            if(!autoMoveRequested && seq.isRunning()){
                //Cancel sequence
                seq.stop();
            }
        }

        prevAutoMoveRequested = autoMoveRequested;
    }
}