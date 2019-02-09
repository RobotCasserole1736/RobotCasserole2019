package frc.robot;

import java.net.NetworkInterface;
import java.net.SocketException;

import frc.lib.Calibration.Calibration;

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

 public class RioSimMode{

    //Singelton stuff
    private static RioSimMode inst = null;
    public static synchronized RioSimMode getInstance() {
        if (inst == null){
            inst = new RioSimMode();
        }
        return inst;
    }

    boolean isSimMode;

    Calibration forceDriveTrainSim;
    final String ROBOTMAC = "00-80-2F-17-F5-E5"; /* The MAC address of the robot RoboRIO */
    private static String macStr = "MACnotInitialized";


    private RioSimMode(){

        forceDriveTrainSim  = new Calibration("Force Simulated Drivetrain (>0.0001 forces simulation)", 1.0000); //Default to sim, unless we're on the robot

        setMACAddr();

        isSimMode = System.getProperty("os.name").contains("Windows") || (macStr != ROBOTMAC && forceDriveTrainSim.get() > 0.0001);
    }

    public boolean isSimMode(){
        return isSimMode;
    }

    private void setMACAddr(){

        // Get MAC Address
        try {
            //Enumeration<NetworkInterface> netInterfaces = NetworkInterface.getNetworkInterfaces();
            NetworkInterface neti = NetworkInterface.getByName("eth0");
            byte[] mac = neti.getHardwareAddress();

            if(mac == null){ //happens on windows sometimes
                throw new SocketException();
            }

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

     
 }