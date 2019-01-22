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

import edu.wpi.first.wpilibj.DigitalInput;

public class LineFollower {

    public double forwardReverseCmd;
    public double rotationCmd;
    public boolean sensor1;
    public boolean sensor2;
    public boolean sensor3;
    public boolean sensor4;
    public boolean sensor5;
    public boolean sensor1Prev;
    public boolean sensor2Prev;
    public boolean sensor3Prev;
    public boolean sensor4Prev;
    public boolean sensor5Prev;
    public double sensor1PosIn = 4;
    public double sensor2PosIn = 2;
    public double sensor3PosIn = 0;
    public double sensor4PosIn = -2;
    public double sensor5PosIn = -4;
    
    DigitalInput digitalInput1;
    DigitalInput digitalInput2;
    DigitalInput digitalInput3;
    DigitalInput digitalInput4;
    DigitalInput digitalInput5;

    private LineFollower(){
        digitalInput1 = new DigitalInput(RobotConstants.LINE_FOLLOWING_SENSOR_1_PORT);
        digitalInput2 = new DigitalInput(RobotConstants.LINE_FOLLOWING_SENSOR_2_PORT);
        digitalInput3 = new DigitalInput(RobotConstants.LINE_FOLLOWING_SENSOR_3_PORT);
        digitalInput4 = new DigitalInput(RobotConstants.LINE_FOLLOWING_SENSOR_4_PORT);
        digitalInput5 = new DigitalInput(RobotConstants.LINE_FOLLOWING_SENSOR_5_PORT);
    }

    public double getForwardCmd() {
        return forwardReverseCmd;
    }

    public double getRotationCmd(){
        return rotationCmd;
    }

    public void update() {
        sensor1 = digitalInput1.get();
        sensor2 = digitalInput2.get();
        sensor3 = digitalInput3.get();
        sensor4 = digitalInput4.get();
        sensor5 = digitalInput5.get();
        if(sensor1 == true && sensor1Prev == false){
            forwardReverseCmd = 0;
            rotationCmd = 1;
        } else if(sensor1 == false && sensor1Prev == false){
            forwardReverseCmd = 0;
            rotationCmd = 0;
        }

        if(sensor2 == true && sensor2Prev == false) {
            forwardReverseCmd = 0;
            rotationCmd = 50;
        } else if(sensor2 == false && sensor2Prev == false){
            forwardReverseCmd = 0;
            rotationCmd = 0;
        }

        if(sensor3 == true && sensor3Prev == false){
            forwardReverseCmd = 1;
            rotationCmd = 0;
        } else if(sensor3 == false && sensor3Prev == false){
            forwardReverseCmd = 0;
            rotationCmd = 0;
        }

        if(sensor4 == true && sensor4Prev == false){
            forwardReverseCmd = 0;
            rotationCmd = -50;
        } else if(sensor4 == false && sensor5Prev == false){
            forwardReverseCmd = 0;
            rotationCmd = 0;
        }

        if(sensor5 == true && sensor5Prev == false){
            forwardReverseCmd = 0;
            rotationCmd = -1;
        } else if(sensor5 == false && sensor5Prev == false){
            forwardReverseCmd = 0;
            rotationCmd = 0;
        }
        
        sensor1Prev = sensor1;
        sensor2Prev = sensor2;
        sensor3Prev = sensor3;
        sensor4Prev = sensor4;
        sensor5Prev = sensor5;
    }
    
}