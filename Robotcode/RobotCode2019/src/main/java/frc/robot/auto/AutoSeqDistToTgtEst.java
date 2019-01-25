package frc.robot.auto;
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

import frc.lib.AutoSequencer.AutoEvent;

public class AutoSeqDistToTgtEst extends AutoEvent {

    double distanceEst_ft = 0;

    public AutoSeqDistToTgtEst(){
        //TODO, if any init is needed.
    }

    /**
     * Set the current robot linear velocity toward or away from the target
     */
    public void setRobotLinearVelocity(double linearVel_ftpsec){
        //TODO
    }

    /**
     * Call this when the current distance is known for sure (ie, line sensor first detects the line). It will force the current estimate to this number.
     */
    public void setDistance(double distance_ft){
        //Todo
    }

    public void setVisionDistanceEstimate(double distance_ft){
        //Todo
    }

    public void setUltrasonicDistanceEstimate(double distance_ft){
        //todo
    }

    public double getEstDistanceFt(){
        return distanceEst_ft; 
    }

    public void update(){
        //TODO 

    }

    @Override
    public void userStart() {

    }

    @Override
    public void userUpdate() {

    }

    @Override
    public void userForceStop() {

    }

    @Override
    public boolean isTriggered() {
        return false;
    }

    @Override
    public boolean isDone() {
        return false;
    }

}