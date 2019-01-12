package frc.robot;

import edu.wpi.first.wpilibj.Compressor;
import edu.wpi.first.wpilibj.AnalogInput;

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

public class PneumaticsControl {
	
	Compressor compressor;
	AnalogInput pressureSensor;

	private static PneumaticsControl pneumatics = null;
    
	public static synchronized PneumaticsControl getInstance() {
		if(pneumatics == null)
		 pneumatics = new PneumaticsControl();
		return pneumatics;
	}

	private PneumaticsControl() {
		compressor = new Compressor();
		pressureSensor = new AnalogInput(RobotConstants.ANALOG_PRESSURE_SENSOR_PORT);
	}
	
	// start method for the compressor
	public void Start(){
		compressor.start();
	}

	// stop method for the compressor
	public void Stop(){
		compressor.stop();
	}
	
	public double GetPressure(){
		double voltage = pressureSensor.getVoltage();
		double pressure = ((voltage/5.0)-0.1)*(150/0.8);
		return pressure;
	}

}