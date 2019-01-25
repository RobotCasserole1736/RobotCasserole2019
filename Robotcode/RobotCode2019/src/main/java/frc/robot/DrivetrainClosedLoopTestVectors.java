package frc.robot;
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

import frc.lib.Calibration.Calibration;

public class DrivetrainClosedLoopTestVectors {

    Calibration testSequence;

    Calibration testPeriodSec;
    Calibration testAmpRPM;


    boolean testActive;

    private static DrivetrainClosedLoopTestVectors dtCLTestVec = null;
    public static synchronized DrivetrainClosedLoopTestVectors getInstance() {
        if(dtCLTestVec == null)
            dtCLTestVec = new DrivetrainClosedLoopTestVectors();
        return dtCLTestVec;
    }

    private DrivetrainClosedLoopTestVectors(){
        testSequence  = new Calibration("Test Vector Drivetrain Test Type", 0, 0, 2); 
        testPeriodSec = new Calibration("Test Vector Drivetrain Amplititude RPM", 50, 0, 1000); 
        testAmpRPM    = new Calibration("Test Vector Drivetrain Period Sec", 0, 0, 15); 
        testActive = false;
    }

    public boolean isTestActive(){
        return testActive;
    }

    public void update(){
        double leftSpeedCmd = 0;
        double rightSpeedCmd = 0;

        if(testSequence.get() > 0){
            testActive = true;
        } else {
            testActive = false;
        }

        if(testActive){
            if(testSequence.get() == 1.0){
                //Sine-wave input
            } else if(testSequence.get() == 2.0){
                // Trapezoid speed profile
            } else if(testSequence.get() == 3.0){
                //PathPlan Curve
            }
        }

    }

}