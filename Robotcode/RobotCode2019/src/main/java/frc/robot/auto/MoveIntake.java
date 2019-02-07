package frc.robot.auto;

import frc.lib.AutoSequencer.AutoEvent;
import frc.robot.IntakeControl;
import frc.robot.IntakeControl.IntakePos;
import frc.robot.IntakeControl.IntakeSpd;

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

public class MoveIntake extends AutoEvent {

    IntakePos desPos;

    public MoveIntake(IntakePos desPos_in) {
        desPos = desPos_in;
    }

    @Override
    public void userStart() {
        IntakeControl.getInstance().setPositionCmd(desPos);
        IntakeControl.getInstance().setSpeedCmd(IntakeSpd.Stop);
    }

    @Override
    public void userUpdate() {

    }

    @Override
    public void userForceStop() {
        IntakeControl.getInstance().forceStop();
    }

    @Override
    public boolean isTriggered() {
        return true; //always run right away
    }

    @Override
    public boolean isDone() {
        return IntakeControl.getInstance().isAtDesPos();
    }
}