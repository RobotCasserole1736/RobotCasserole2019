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
import frc.robot.LineFollower;
import frc.robot.Drivetrain;

public class TopPlaceFinalAlign extends AutoEvent {

    private LineFollower lineFollower;

    double motorRotationCmd;

    public TopPlaceFinalAlign(){
        motorRotationCmd = 0;
    }

    public void getLineFollowerAngle() {
        motorRotationCmd = lineFollower.getRotationCmd();
    }

    @Override
    public void userStart() {

    }

    @Override
    public void userUpdate() {
        getLineFollowerAngle();
        Drivetrain.getInstance().setOpenLoopCmd(-0.2,motorRotationCmd);
    }

    @Override
    public void userForceStop() {

    }

    @Override
    public boolean isTriggered() {
        return true;
    }

    @Override
    public boolean isDone() {
        // 1/12 is to say 1 inch instead of 1 foot.
        return AutoSeqDistToTgtEst.getInstance().getEstDistanceFt() < 1/12;
    }

}