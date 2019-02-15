package frc.robot;

import com.ctre.phoenix.motorcontrol.can.WPI_TalonSRX;

import edu.wpi.first.wpilibj.Compressor;
import frc.lib.Util.DaBouncer;

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

public class SensorCheck {
 
    DaBouncer aboveDebouncer;
    DaBouncer belowDebouncer;

    DriverController driverController;
    Drivetrain drivetrain;
    Compressor compressor;
    PneumaticsControl pneumatic;

    boolean dtLeftEncoderFault;
    boolean dtRightEncoderFault;
    boolean dtRightTalon1Fault;
    boolean dtRightTalon2Fault;
    boolean dtLeftTalon1Fault;
    boolean dtLeftTalon2Fault;
    boolean compressorCutoffFault;
    boolean compressorPresenceFault;

    boolean faultDetected;

	// You will want to rename all instances of "EmptyClass" with your actual class name and "empty" with a variable name
	private static SensorCheck empty = null;

	public static synchronized SensorCheck getInstance() {
		if(empty == null)
			empty = new SensorCheck();
		return empty;
	}

	// This is the private constructor that will be called once by getInstance() and it should instantiate anything that will be required by the class
	private SensorCheck() {
        aboveDebouncer = new DaBouncer();
        belowDebouncer = new DaBouncer();

        driverController = DriverController.getInstance();
        drivetrain = Drivetrain.getInstance();
        pneumatic = PneumaticsControl.getInstance();
        compressor = pneumatic.getCompressor();
    }

    public void update() {
        //Drivetrain left encoder
        if((driverController.getDriverFwdRevCmd() > 0) && (aboveDebouncer.AboveDebounce(drivetrain.getLeftWheelSpeedRPM()) == true)) {
            dtLeftEncoderFault = false;
            faultDetected = false;
        }else if((driverController.getDriverFwdRevCmd() > 0) && (aboveDebouncer.AboveDebounce(drivetrain.getLeftWheelSpeedRPM()) == false)) {
            dtLeftEncoderFault = true;
            faultDetected = true;
        }else if((driverController.getDriverFwdRevCmd() < 0) && (belowDebouncer.BelowDebounce(drivetrain.getLeftWheelSpeedRPM()) == true)) {
            dtLeftEncoderFault = false;
            faultDetected = false;
        }else if((driverController.getDriverFwdRevCmd() < 0) && (belowDebouncer.BelowDebounce(drivetrain.getLeftWheelSpeedRPM()) == false)) {
            dtLeftEncoderFault = true;
            faultDetected = true;
        } 
        
        //Drivetrain right encoder
        if((driverController.getDriverFwdRevCmd() > 0) && (aboveDebouncer.AboveDebounce(drivetrain.getRightWheelSpeedRPM()) == true)) {
            dtRightEncoderFault = false; //change these to right
            faultDetected = false;
        }else if((driverController.getDriverFwdRevCmd() > 0) && (aboveDebouncer.AboveDebounce(drivetrain.getRightWheelSpeedRPM()) == false)) {
            dtRightEncoderFault = true;
            faultDetected = true;
        }else if((driverController.getDriverFwdRevCmd() < 0) && (belowDebouncer.BelowDebounce(drivetrain.getRightWheelSpeedRPM()) == true)) {
            dtRightEncoderFault = false;
            faultDetected = false;
        }else if((driverController.getDriverFwdRevCmd() < 0) && (belowDebouncer.BelowDebounce(drivetrain.getRightWheelSpeedRPM()) == false)) {
            dtRightEncoderFault = true;
            faultDetected = true;
        } 

        //Drivetrain motor currents
        if((driverController.getDriverFwdRevCmd() != 0) && (drivetrain.getLeftTalon1Current() != 0)) {
            dtLeftTalon1Fault = false;
            faultDetected = false;
        }else if((driverController.getDriverFwdRevCmd() != 0) && (drivetrain.getLeftTalon1Current() == 0)) {
            dtLeftTalon1Fault = true;
            faultDetected = true;
        }else if((driverController.getDriverFwdRevCmd() != 0) && (drivetrain.getLeftTalon2Current() != 0)) {
            dtLeftTalon2Fault = false;
            faultDetected = false;
        }else if((driverController.getDriverFwdRevCmd() != 0) && (drivetrain.getLeftTalon2Current() == 0)) {
            dtLeftTalon2Fault = true;
            faultDetected = true;
        }else if((driverController.getDriverFwdRevCmd() != 0) && (drivetrain.getRightTalon1Current() != 0)) {
            dtRightTalon1Fault = false;
            faultDetected = false;
        }else if((driverController.getDriverFwdRevCmd() != 0) && (drivetrain.getRightTalon1Current() == 0)) {
            dtRightTalon1Fault = true;
            faultDetected = true;
        }else if((driverController.getDriverFwdRevCmd() != 0) && (drivetrain.getRightTalon2Current() != 0)) {
            dtRightTalon2Fault = false;
            faultDetected = false;
        }else if((driverController.getDriverFwdRevCmd() != 0) && (drivetrain.getRightTalon2Current() == 0)) {
            dtRightTalon2Fault = true;
            faultDetected = true;
        }

        //Compressor cutoff switch
        if((compressor.getPressureSwitchValue() == false) && (pneumatic.getPressure() > 100)) {
            compressorCutoffFault = false;
            faultDetected = false;
        }else if((compressor.getPressureSwitchValue() == false) && (pneumatic.getPressure() < 100)) {
            compressorCutoffFault = true;
            faultDetected = true;
        }

        //Compressor plugged in?
        if(pneumatic.getPressure() > -5) {
            compressorPresenceFault = false;
            faultDetected = false;
        }else if(pneumatic.getPressure() <= -5) {
            compressorPresenceFault = true;
            faultDetected = true;
        }
    }

    public boolean isFaultDetected() {
        return faultDetected;
    }

    public String getFaultDescription() {
        String description = "No Fault";
        if(dtLeftEncoderFault == true) {
            description = "DT Left Encoder Fault";
        }else if(dtRightEncoderFault == true) {
            description = "DT Right Encoder Fault";
        }else if(dtLeftTalon1Fault == true) {
            description = "DT Left Motor 1 Electrical Fault";
        }else if(dtLeftTalon2Fault == true) {
            description = "DT Left Motor 2 Electrical Fault";
        }else if(dtRightTalon1Fault == true) {
            description = "DT Right Motor 1 Electrical Fault";
        }else if(dtRightTalon2Fault == true) {
            description = "DT Right Motor 2 Electrical Fault";
        }else if(compressorCutoffFault == true) {
            description = "Compressor Cutoff Switch Fault";
        }else if(compressorPresenceFault == true) {
            description = "Compressor Unplugged or Faulty";
        }else{
            description = "No Fault";
        } 
        return description; 
    }

}