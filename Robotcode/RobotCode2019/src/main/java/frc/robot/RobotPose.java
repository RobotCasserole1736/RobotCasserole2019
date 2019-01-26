package frc.robot;

import frc.lib.DataServer.Signal;

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

public class RobotPose {

    public double leftVelosity_RPM;
    public double rightVelosity_RPM;
    public final double Math_PI = 3.14;
    public final double FIELD_LENGTH_FT = 54;
    public final double FIELD_HALF_WIDTH_FT = 13.47;
    public double poseX = 0;
    public double poseY = 0;
    public double poseThaddeus = 90;
    public double velosityX = 0;
    public double velosityY = 0;

    Signal DesX;
    Signal DesY;
    Signal DesT;
    Signal ActX;
    Signal ActY;
    Signal ActT;

    public RobotPose() {
        DesX = new Signal("botDesPoseX", "in");
        DesY = new Signal("botDesPoseY", "in");
        DesT = new Signal("botDesPoseT", "deg");
        ActX = new Signal("botActPoseX", "in");
        ActY = new Signal("botActPoseY", "in");
        ActT = new Signal("botActPoseT", "deg");
    }

    public void setLeftMotorSpeed(double speed) {
        leftVelosity_RPM = speed;
    }

    public void setRightMotorSpeed(double speed){
        rightVelosity_RPM = speed;
    }
    
    public double getRobotVelocity_ftpersec(){
        return velosityX;
    }
    
    public void update() {
        double leftVelosity_FPS = leftVelosity_RPM * (2*Math_PI*RobotConstants.WHEEL_RADIUS_FT / 60);
        double rightVelosity_FPS = rightVelosity_RPM * (2*Math_PI*RobotConstants.WHEEL_RADIUS_FT / 60);
        double robotAngle_DPS = ((rightVelosity_FPS-leftVelosity_FPS)/(2*RobotConstants.ROBOT_RADIUS_FT));
        double X_dot = (rightVelosity_FPS+leftVelosity_FPS)/2; 
        
        Math.toRadians(poseThaddeus);

        Math.toDegrees((rightVelosity_FPS-leftVelosity_FPS)/(2*RobotConstants.ROBOT_RADIUS_FT));

        velosityX = 0.02 * (X_dot*Math.cos(poseThaddeus));
        velosityY = 0.02 * (X_dot*Math.sin(poseThaddeus));
        
        if(poseY < 0) { 
            velosityY = 0;
            velosityX = 0;
            
        }
        if(poseY > FIELD_LENGTH_FT){
            velosityY = 0;
            velosityX = 0;
            
        }
        if(poseX < -FIELD_HALF_WIDTH_FT){
            velosityX = 0;
            velosityY = 0;
        }
        if(poseX > FIELD_HALF_WIDTH_FT){
            velosityX = 0;
            velosityY = 0;
        }
        
        poseX += velosityX;
        poseY += velosityY;
        poseThaddeus += 0.02 * robotAngle_DPS;
        //CasseroleRobotPoseView.setRobotPose(poseX, poseY, poseTheta - 90);

        double sample_time_ms = LoopTiming.getInstance().getLoopStartTimeSec()*1000.0;
        DesX.addSample(sample_time_ms,0);
        DesY.addSample(sample_time_ms,0);
        DesT.addSample(sample_time_ms,0);
        ActX.addSample(sample_time_ms,poseX);
        ActY.addSample(sample_time_ms,poseY);
        ActT.addSample(sample_time_ms,poseThaddeus);

        }
    
    public void reset() {
        poseX = 0;
        poseY = 0;
        poseThaddeus = 90;
        leftVelosity_RPM = 0;
        rightVelosity_RPM = 0;
    }
    
}
