package frc.robot;

import edu.wpi.first.wpilibj.DigitalOutput;

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


 public class VisionLEDRingControl{

    //Singelton stuff
    private static VisionLEDRingControl inst = null;
    public static synchronized VisionLEDRingControl getInstance() {
        if (inst == null){
            inst = new VisionLEDRingControl();
        }
        return inst;
    }

    DigitalOutput ringRelayOutput;

    private VisionLEDRingControl(){
        ringRelayOutput = new DigitalOutput(RobotConstants.VISON_LED_RING_PORT);
    }

    public void setLEDRingState(boolean enabled){
        ringRelayOutput.set(enabled); //electrically inverted
    }

}