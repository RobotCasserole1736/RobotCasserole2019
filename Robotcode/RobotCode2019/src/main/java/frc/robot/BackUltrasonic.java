package frc.robot;

import edu.wpi.first.wpilibj.AnalogInput;
import frc.lib.Calibration.Calibration;
import frc.lib.DataServer.Signal;
import frc.lib.SignalMath.AveragingFilter;

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

public class BackUltrasonic {
    AnalogInput analogIn;

    AveragingFilter filt;

    double distance_ft;
    boolean distanceAvailable;

    Calibration maxVoltage;
    Calibration minVoltage;

    Signal distanceFeetSig;
    Signal voltageSig;
    Signal distanceAvailableSig;

    // You will want to rename all instances of "BackUltrasonic" with your actual class name and "empty" with a variable name
    private static BackUltrasonic empty = null;

    public static synchronized BackUltrasonic getInstance() {
        if(empty == null)
            empty = new BackUltrasonic();
        return empty;
    }

    // This is the private constructor that will be called once by getInstance() and it should instantiate anything that will be required by the class
    BackUltrasonic() {
        analogIn = new AnalogInput(RobotConstants.ULTRASONIC_REAR_PORT); 

        filt = new AveragingFilter(3,0);

        maxVoltage = new Calibration("Back Ultrasonic Voltage Max (mV)", 180);
        minVoltage = new Calibration("Back Ultrasonic Voltage Min (mV)", 70);

        distanceFeetSig = new Signal("Back Ultrasonic Distance", "ft");
        voltageSig = new Signal("Back Ultrasonic Sensor Voltage", "mV");
        distanceAvailableSig = new Signal("Back Distance Available", "true/false");
    }

    public void update() {
        double voltage = 0;
        double distance_mm = 0;

        voltage = ((analogIn.getVoltage()) * 1000); //voltage in mV
        distance_mm = voltage; 
        distance_ft = filt.filter(distance_mm / 304.8);

        if((voltage > (maxVoltage.get())) || (voltage < (minVoltage.get()))){
            distanceAvailable = false;
        }else{
            distanceAvailable = true;
        }

        double sampleTimeMS = LoopTiming.getInstance().getLoopStartTimeSec() * 1000.0;
        distanceFeetSig.addSample(sampleTimeMS, distance_ft);
        voltageSig.addSample(sampleTimeMS, voltage);
        distanceAvailableSig.addSample(sampleTimeMS, distanceAvailable);

    }

    public double getdistance_ft() {
        return distance_ft;
    }

    public boolean isDistanceAvailable() {
        return distanceAvailable;
    }
}