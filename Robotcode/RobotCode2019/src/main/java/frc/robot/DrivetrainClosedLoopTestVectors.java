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

import edu.wpi.first.wpilibj.Timer;
import frc.lib.Calibration.Calibration;
import frc.lib.PathPlanner.FalconPathPlanner;

public class DrivetrainClosedLoopTestVectors {

    Calibration testSequence;

    Calibration testPeriodSec;
    Calibration testAmpRPM;

    double leftSpeedCmd = 0;
    double rightSpeedCmd = 0;
    double headingCmd = 0;

    double speedCmd = 0;
    boolean triangleWaveUp = true;

    //In units of feet
    double[][] testWaypoints = {
                                {0,0},
                                {0,5},
                                {2,7},
                                {4,9},
                                {4,12}
                               };
    FalconPathPlanner path;
    int pathPlannerIdx = 0;
    
    double testSeq = 0;
    double prevTestSeq = 0;


    boolean testActive;

    private static DrivetrainClosedLoopTestVectors dtCLTestVec = null;
    public static synchronized DrivetrainClosedLoopTestVectors getInstance() {
        if(dtCLTestVec == null)
            dtCLTestVec = new DrivetrainClosedLoopTestVectors();
        return dtCLTestVec;
    }

    private DrivetrainClosedLoopTestVectors(){
        testSequence  = new Calibration("Test Vector Drivetrain Test Type", 0, 0, 3); 
        testAmpRPM = new Calibration("Test Vector Drivetrain Amplititude RPM", 50, 0, 1000); 
        testPeriodSec = new Calibration("Test Vector Drivetrain Period Sec", 5.0, 0.5, 15); 
        testActive = false;
        speedCmd = 0;
    }

    public boolean isTestActive(){
        return testActive;
    }

    public void update(){

        prevTestSeq = testSeq;
        testSeq = testSequence.get();

        if(prevTestSeq != testSeq){
            //Reset
            speedCmd = 0;
            triangleWaveUp = true;
            pathPlannerIdx = 0;
            path = new FalconPathPlanner(testWaypoints);
            path.setPathBeta(0.2);
            path.setPathAlpha(0.5);
            path.setVelocityAlpha(0.001);
            path.setVelocityBeta(0.9);
            path.calculate(testPeriodSec.get(), RobotConstants.MAIN_LOOP_SAMPLE_RATE_S, RobotConstants.ROBOT_TRACK_WIDTH_FT);
        }

        if(testSeq > 0){
            testActive = true;
        } else {
            testActive = false;
        }

        if(testActive){
            if(testSeq == 1.0){
                //Sine wave
                speedCmd = testAmpRPM.get() * Math.sin(2*Math.PI*Timer.getFPGATimestamp()/testPeriodSec.get());
                leftSpeedCmd = speedCmd;
                rightSpeedCmd = speedCmd;
                headingCmd = 0;
            } else if(testSeq == 2.0){
                // Triangle speed profile
                if(speedCmd > testAmpRPM.get()){
                    triangleWaveUp = false;
                } else if(speedCmd < -1.0*testAmpRPM.get()) {
                    triangleWaveUp = true;
                }

                if(triangleWaveUp){
                    speedCmd += testAmpRPM.get() * (RobotConstants.MAIN_LOOP_SAMPLE_RATE_S/testPeriodSec.get()*4);
                } else {
                    speedCmd -= testAmpRPM.get() * (RobotConstants.MAIN_LOOP_SAMPLE_RATE_S/testPeriodSec.get()*4);
                }

                leftSpeedCmd = speedCmd;
                rightSpeedCmd = speedCmd;
                headingCmd = 0;

            } else if(testSeq == 3.0){

                if(pathPlannerIdx < path.smoothLeftVelocity.length){
                    leftSpeedCmd  = path.smoothLeftVelocity[pathPlannerIdx][1];
                    rightSpeedCmd = path.smoothRightVelocity[pathPlannerIdx][1];
                    headingCmd = path.heading[pathPlannerIdx][1];
                } else {
                    leftSpeedCmd  = 0;
                    rightSpeedCmd = 0;
                    headingCmd = path.heading[path.heading.length-1][1];
                }
                pathPlannerIdx++;
            }

            Drivetrain.getInstance().setClosedLoopSpeedCmd(Utils.FT_PER_SEC_TO_RPM(leftSpeedCmd), Utils.FT_PER_SEC_TO_RPM(rightSpeedCmd), headingCmd);
        }

    }

}