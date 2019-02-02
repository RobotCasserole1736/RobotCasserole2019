 package frc.robot;
 
 import java.net.NetworkInterface;
 import java.util.Enumeration;
 import java.net.SocketException;
 
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
 *    (listed above). ,m,mm8l
 *  If you happen to end up using our software to make money, that is wonderful!
 *    Robot Casserole is always looking for more sponsors, so we'd be very appreciative
 *    if you would consider donating to our club to help further STEM education.
 */

 /**
  * Sim/Real drivetrain switchyard. This exists entirely because I currently cannot get CTRE_PhoenixCCI.dll to load
  *  in WPILib simulation. Per their docs, it's not technically supported yet, which kinda stinks. In the mean time,
  *  The drivetrain is abstracted out and this class is the singleton that selects whether we use the real or simulated
  *  implelmentations (ie with or without CTRE libraries). The simulated implementation has a built-in plant model.
  *
  * NOTE: Any new method added to either drivetrain implementation needs to be added to the other implementation, as well as to
  *       this switchyard.
  */

import frc.lib.Calibration.Calibration;

public class Drivetrain implements DrivetrainInterface {

    private static DrivetrainInterface dTrainIF = null;
    private static Drivetrain dTrain = null;

    Calibration forceDriveTrainSim;
    final String ROBOTMAC = "00-80-2F-17-F5-E5"; /* The MAC address of the robot RoboRIO (test board now)*/
    private static String macStr = "MACnotInitialized";
    
    public static synchronized Drivetrain getInstance() {
        if (dTrain == null){
            dTrain = new Drivetrain();
        }
        return dTrain;
    }

    private Drivetrain(){

        forceDriveTrainSim  = new Calibration("Force Simulated Drivetrain (>0.0001 forces simulation)", 0.0000);
    
        if(System.getProperty("os.name").contains("Windows") || (macStr != ROBOTMAC && forceDriveTrainSim.get() > 0.0001)){
            dTrainIF = new DrivetrainSim(); //TODO make this work on linux laptops
        } else {
            dTrainIF = new DrivetrainReal();
        }
    }

    public void setOpenLoopCmd(double forwardReverseCmd, double rotaionCmd){
        dTrainIF.setOpenLoopCmd(forwardReverseCmd, rotaionCmd);
    }

    public void setGyroLockCmd(double forwardReverseCmd){
        dTrainIF.setGyroLockCmd(forwardReverseCmd);
    }

    public boolean isGyroOnline(){
        return dTrainIF.isGyroOnline();
    }

    public void update(){
        dTrainIF.update();
    }

    public void updateGains(boolean force){
        dTrainIF.updateGains(force);
    }

    public double getLeftWheelSpeedRPM(){
        return dTrainIF.getLeftWheelSpeedRPM();
    }

    public double getRightWheelSpeedRPM(){
        return dTrainIF.getRightWheelSpeedRPM();
    }

    public void setMACAddr(){

        // Get MAC Address
        try {
            //Enumeration<NetworkInterface> netInterfaces = NetworkInterface.getNetworkInterfaces();
            NetworkInterface neti = NetworkInterface.getByName("eth0");
            byte[] mac = neti.getHardwareAddress();

            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < mac.length; i++) {
                sb.append(String.format("%02X%s", mac[i], (i < mac.length - 1) ? "-" : ""));        
            }
            macStr = sb.toString();

        } catch (SocketException e){

            macStr = "SocketException";

        }
    }

    public String getMACAddr(){
        return macStr;
    }

    @Override
    public void setClosedLoopSpeedCmd(double leftCmdRPM, double rightCmdRPM) {
        dTrainIF.setClosedLoopSpeedCmd(leftCmdRPM, rightCmdRPM);
    }

    @Override
    public void setClosedLoopSpeedCmd(double leftCmdRPM, double rightCmdRPM, double headingCmdDeg) {
        dTrainIF.setClosedLoopSpeedCmd(leftCmdRPM, rightCmdRPM, headingCmdDeg);
    }
}