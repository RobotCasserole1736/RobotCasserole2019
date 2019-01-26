package frc.robot.auto;

import frc.lib.AutoSequencer.AutoEvent;
import frc.lib.AutoSequencer.AutoSequencer;
import frc.robot.OperatorController;
import frc.robot.Arm.ArmPosReq;
import frc.robot.PEZControl.PEZPos;

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
    boolean previousAutoMoveState = false;

    private double visionAngle = 0;

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

    public ArmPosReq getArmPosCmd() {
        return ArmPosReq.None;
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

    //Commands called from other parts of the code need to be inputed into the parentheses, I think
    public void update(){
        boolean currentAutoMoveState = OperatorController.getInstance().getAutoMove();
        
        if(currentAutoMoveState && !previousAutoMoveState){
            //Initialize autoMove sequence
            double xTargetOffset = 0;
            double yTargetOffset = 0;
            double targetPositionAngle = 0;

            AutoEvent parent = new AutoSeqPathPlan(xTargetOffset, yTargetOffset, targetPositionAngle);
            AutoSequencer.addEvent(parent);
            //parent = new AutoSeqDistToTgtEst();

            if(OperatorController.getInstance().getLowLevelPlace()){
                parent.addChildEvent(new MoveArmLowPos());
            }

            else if(OperatorController.getInstance().getMidLevelPlace()){
                parent.addChildEvent(new MoveArmMidPos());
            }

            else if(OperatorController.getInstance().getTopLevelPlace()){
                parent.addChildEvent(new MoveArmTopPos());
            }

            else {
                System.out.println("Error invalid Autostate.");
            }

            AutoSequencer.addEvent(parent);
        }

        if(!currentAutoMoveState && previousAutoMoveState){
            //Cancel sequence
            AutoSequencer.stop();
        }

        previousAutoMoveState = currentAutoMoveState;
    }
}