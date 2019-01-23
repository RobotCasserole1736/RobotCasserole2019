package frc.robot;

import com.ctre.phoenix.motorcontrol.InvertType;

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
 *    find it educational and informative! 
 *  Please feel free to snag our software for your own use in whatever project
 *    you have going on right now! We'd love to be able to help out! Shoot us 
 *    any questions you may have, all our contact info should be on our website
 *    (listed above).
 *  If you happen to end up using our software to make money, that is wonderful!
 *    Robot Casserole is always looking for more sponsors, so we'd be very appreciative
 *    if you would consider donating to our club to help further STEM education.
 */


public class DrivetrainSim implements DrivetrainInterface {

    public DrivetrainSim() {

    }

    public void setOpenLoopCmd(double forwardReverseCmd, double rotaionCmd) {
        //TODO
    }

    public boolean isGyroOnline(){
        return true;
    }

    public void setGyroLockCmd(double forwardReverseCmd) {
        //TODO
    }

    public void update() {
        //TODO
    }

    public double getLeftWheelSpeedRPM() {
        return 0; //TODO
    }

    public double getRightWheelSpeedRPM() {
        return 0; //TODO
    }

    public void updateGains(boolean force) {
    }
}