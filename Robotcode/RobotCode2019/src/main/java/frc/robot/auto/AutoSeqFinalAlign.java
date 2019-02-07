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

import edu.wpi.first.wpilibj.PIDController;
import edu.wpi.first.wpilibj.PIDOutput;
import edu.wpi.first.wpilibj.PIDSource;
import edu.wpi.first.wpilibj.PIDSourceType;
import frc.lib.AutoSequencer.AutoEvent;
import frc.robot.JeVoisInterface;
import frc.robot.Drivetrain;

public class AutoSeqFinalAlign extends AutoEvent {

    private JeVoisInterface camera;

    //most likely will need to change the name. It is just a placeholder.
    PIDController motorMove;

    double motorRotationCmd;
    double desiredAngle;

    int angleOffset;

    public AutoSeqFinalAlign(){
        //TODO, if any init is needed.
    }

    public double getJeVoisAngle() {
		return angleOffset - camera.getTgtAngle();
    }

    public void pidWrite(double output) {
        motorRotationCmd = output;
    }

    public void setPIDSourceType(PIDSourceType pidSource) {
        
    }

    public PIDSourceType getPIDSourceType() {
        return PIDSourceType.kDisplacement;
    }

    public double pidGet() {
        return getJeVoisAngle();
    }

    @Override
    public void userStart() {

    }

    @Override
    public void userUpdate() {
        Drivetrain.getInstance().setOpenLoopCmd(0.2,motorRotationCmd);
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
        return false;
    }

}