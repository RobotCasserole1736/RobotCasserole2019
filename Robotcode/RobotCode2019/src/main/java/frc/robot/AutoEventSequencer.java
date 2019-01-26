package frc.robot;

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

public class AutoEventSequencer {
    //State Variables
    public Robert curState;
    private Robert nextState;

    private double visionAngle = 0;


    private static final Robert initState = Robert.Idle;

    // name and "empty" with a variable name
    private static AutoEventSequencer empty = null;

    public static synchronized AutoEventSequencer getInstance() {
        if (empty == null)
            empty = new AutoEventSequencer();
        return empty;
    }

    // This is the private constructor that will be called once by getInstance() and
    // it should instantiate anything that will be required by the class

    // Name for AutoEventSequencer enum is Robert. Welcome to change if need be.
    public enum Robert {
        Idle(0), Path_Plan_Approach_Vector(1), Move_Along_Approach_Vector(2), Place_Gamepiece_Low_Level(3), 
        Place_Gamepiece_Mid_Level(4), Place_Gamepiece_Top_Level(5), Release_Gamepiece_Low_Level_and_Backup(6),
        Release_Gamepiece_Mid_Level_and_Backup(7), Release_Gamepiece_High_Level_and_Backup(8);

        public final int value;

        private Robert(int value){
            this.value = value;
        }
    }

    //needs something inside of the parentheses, probably
    private AutoEventSequencer() {
        curState = initState;
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
        //if no state transition is specified, 
        //	we should stay in the same state.
        //this assignment will be overwritten if any
        // 	of the transition conditions are true.
        nextState = curState;
        if(OperatorController.getInstance().getAutoMove()){
            nextState = Robert.Path_Plan_Approach_Vector;
        }

        else if(nextState == Robert.Path_Plan_Approach_Vector){
            nextState = Robert.Move_Along_Approach_Vector;
        }

        else {
            nextState = initState;
        }

        if(OperatorController.getInstance().getLowLevelPlace() && nextState == Robert.Move_Along_Approach_Vector){
            nextState = Robert.Place_Gamepiece_Low_Level;
        }

        else if(nextState == Robert.Place_Gamepiece_Low_Level){
            nextState = Robert.Release_Gamepiece_Low_Level_and_Backup;
        }

        if(OperatorController.getInstance().getMidLevelPlace() && nextState == Robert.Move_Along_Approach_Vector){
            nextState = Robert.Place_Gamepiece_Mid_Level;
        }

        else if(nextState == Robert.Place_Gamepiece_Mid_Level){
            nextState = Robert.Release_Gamepiece_Mid_Level_and_Backup;
        }

        if(OperatorController.getInstance().getTopLevelPlace() && nextState == Robert.Move_Along_Approach_Vector){
            nextState = Robert.Place_Gamepiece_Top_Level;
        }

        else if(nextState == Robert.Place_Gamepiece_Top_Level){
            nextState = Robert.Release_Gamepiece_High_Level_and_Backup;
        }
        
        //make the next-state the current state
		curState = nextState;
    }
}