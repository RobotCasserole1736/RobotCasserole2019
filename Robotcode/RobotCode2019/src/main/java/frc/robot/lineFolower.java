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

public class lineFolower {

    public double forwardReverseCmd;
    public double rotationCmd;
    public boolean Sensor1;
    public boolean Sensor2;
    public boolean Sensor3;
    public boolean Sensor4;
    public boolean Sensor5;
    public boolean Sensor1_Prev;
    public boolean Sensor2_Prev;
    public boolean Sensor3_Prev;
    public boolean Sensor4_Prev;
    public boolean Sensor5_Prev;
    public double Sensor1Pos_In = 4;
    public double Sensor2Pos_In = 2;
    public double Sensor3Pos_In = 0;
    public double Sensor4Pos_In = -2;
    public double Sensor5Pos_In = -4;
    
    DigitalInput digitalInput1;
    DigitalInput digitalInput2;
    DigitalInput digitalInput3;
    DigitalInput digitalInput4;
    DigitalInput digitalInput5;

        private lineFolower(){
            digitalInput1 = new DigitalInput(RobotConstants.LINE_FOLLOWING_SENSOR_1);
            digitalInput2 = new DigitalInput(RobotConstants.LINE_FOLLOWING_SENSOR_2);
            digitalInput3 = new DigitalInput(RobotConstants.LINE_FOLLOWING_SENSOR_3);
            digitalInput4 = new DigitalInput(RobotConstants.LINE_FOLLOWING_SENSOR_4);
            digitalInput5 = new DigitalInput(RobotConstants.LINE_FOLLOWING_SENSOR_5);
        }
        public double getForwardCmd() {
            return forwardReverseCmd;
        }
        public double getRotationCmd(){
            return rotationCmd;
        }

        public void update() {
            Sensor1 = digitalInput1.get();
            Sensor2 = digitalInput2.get();
            Sensor3 = digitalInput3.get();
            Sensor4 = digitalInput4.get();
            Sensor5 = digitalInput5.get();
            if(Sensor1 == true && Sensor1_Prev == false){
                forwardReverseCmd = 0;
                rotationCmd = 1;
            } else if(Sensor1 == false && Sensor1_Prev == false){
                forwardReverseCmd = 0;
                rotationCmd = 0;
            }
            if(Sensor2 == true && Sensor2_Prev == false) {
                forwardReverseCmd = 0;
                rotationCmd = 50;
            } else if(Sensor2 == false && Sensor2_Prev == false){
                forwardReverseCmd = 0;
                rotationCmd = 0;
            }
            if(Sensor3 == true && Sensor3_Prev == false){
                forwardReverseCmd = 1;
                rotationCmd = 0;
            } else if(Sensor3 == false && Sensor3_Prev == false){
                forwardReverseCmd = 0;
                rotationCmd = 0;
            }
            if(Sensor4 == true && Sensor4_Prev == false){
                forwardReverseCmd = 0;
                rotationCmd = -50;
            } else if(Sensor4 == false && Sensor5_Prev == false){
                forwardReverseCmd = 0;
                rotationCmd = 0;
            }
            if(Sensor5 == true && Sensor5_Prev == false){
                forwardReverseCmd = 0;
                rotationCmd = -1;
            } else if(Sensor5 == false && Sensor5_Prev == false){
                forwardReverseCmd = 0;
                rotationCmd = 0;
            }
            Sensor1_Prev = Sensor1;
            Sensor2_Prev = Sensor2;
            Sensor3_Prev = Sensor3;
            Sensor4_Prev = Sensor4;
            Sensor5_Prev = Sensor5;
        }
        
    }