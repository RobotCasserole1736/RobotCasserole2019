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

import edu.wpi.first.wpilibj.Timer;
import frc.lib.AutoSequencer.AutoEvent;
import frc.robot.JeVoisInterface;
import frc.robot.Utils;
import frc.robot.Drivetrain;

public class AutoSeqFinalAlign extends AutoEvent {

    JeVoisInterface camera;

    Drivetrain dt;

    final double ALIGNMENT_SPEED_FTPERSEC = 2;

    final double MAX_TIME_S = 3.0;
    double startTime = 0;

    double motorRotationCmd;
    double desiredAngle;

    double initial_angle = 0;

    public AutoSeqFinalAlign(){
        motorRotationCmd = 0;
        camera = JeVoisInterface.getInstance();
        dt = Drivetrain.getInstance();
    }

    public double getAlignmentAngle() {
        if(camera.isVisionOnline() || camera.isTgtVisible()){
            return dt.getGyroAngle() - camera.getTgtGeneralAngle();
        } else {
            return dt.getGyroAngle();
        }

    }

    @Override
    public void userStart() {
        desiredAngle = getAlignmentAngle();
        startTime = Timer.getFPGATimestamp();
    }

    @Override
    public void userUpdate() {
        double motorSpeed = Utils.FT_PER_SEC_TO_RPM(ALIGNMENT_SPEED_FTPERSEC);
        dt.setClosedLoopSpeedCmd(motorSpeed, motorSpeed, desiredAngle);
    }

    @Override
    public void userForceStop() {
        dt.setOpenLoopCmd(0,0);
    }

    @Override
    public boolean isTriggered() {
        return true;
    }

    @Override
    public boolean isDone() {
        boolean timedOut = (Timer.getFPGATimestamp() > (startTime + MAX_TIME_S) );

        // 1/12 is to say 1 inch instead of 1 foot.
        return (AutoSeqDistToTgtEst.getInstance().getEstDistanceFt() < 1.0/12.0) || timedOut;
    }

}