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

import edu.wpi.first.wpilibj.Compressor;
import frc.lib.DataServer.Signal;
import edu.wpi.first.wpilibj.AnalogInput;

public class PneumaticsControl {
    
    Compressor compressor;
    AnalogInput pressureSensor;

    Signal pressSig;
    Signal pressSwVallSig;
    Signal compCurrent;

    double curPressure_psi;

    /* Singelton Stuff */
    private static PneumaticsControl pneumatics = null;
    public static synchronized PneumaticsControl getInstance() {
        if(pneumatics == null) pneumatics = new PneumaticsControl();
        return pneumatics;
    }

    private PneumaticsControl() {
        compressor = new Compressor();
        pressureSensor = new AnalogInput(RobotConstants.ANALOG_PRESSURE_SENSOR_PORT);
        pressSig = new Signal("Pneumatics Main System Pressure", "psi");
        pressSwVallSig = new Signal("Pneumatics Cutoff Switch State", "bool");
        compCurrent = new Signal("Pneumatics Compressor Current", "A");
    }

    public void update(){

        double voltage = pressureSensor.getVoltage();
        curPressure_psi = ((voltage/5.0)-0.1)*(150/0.8);

        double sample_time_ms = LoopTiming.getInstance().getLoopStartTime_sec()*1000.0;
        pressSig.addSample(sample_time_ms,curPressure_psi);
        pressSwVallSig.addSample(sample_time_ms,compressor.getPressureSwitchValue());
        compCurrent.addSample(sample_time_ms,compressor.getCompressorCurrent());
    }
    
    // start method for the compressor
    public void start(){
        compressor.start();
    }

    // stop method for the compressor
    public void stop(){
        compressor.stop();
    }
    
    public double getPressure(){
        return curPressure_psi;
    }

}