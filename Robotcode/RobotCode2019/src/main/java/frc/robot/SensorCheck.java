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
 
    DaBouncer dtLeftEncoderFaultDbnc;
    DaBouncer dtRightEncoderFaultDbnc;
    DaBouncer dtRightTalon1FaultDbnc;
    DaBouncer dtRightTalon2FaultDbnc;
    DaBouncer dtLeftTalon1FaultDbnc;
    DaBouncer dtLeftTalon2FaultDbnc;
    DaBouncer compressorCutoffFaultDbnc;
    DaBouncer compressorPresenceFaultDbnc;

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

	private static SensorCheck inst = null;
	public static synchronized SensorCheck getInstance() {
		if(inst == null)
			inst = new SensorCheck();
		return inst;
	}

	// This is the private constructor that will be called once by getInstance() and it should instantiate anything that will be required by the class
	private SensorCheck() {
        dtLeftEncoderFaultDbnc = new DaBouncer(0, 10);
        dtRightEncoderFaultDbnc = new DaBouncer(0, 10);
        dtRightTalon1FaultDbnc = new DaBouncer(0, 10);
        dtRightTalon2FaultDbnc = new DaBouncer(0, 10);
        dtLeftTalon1FaultDbnc = new DaBouncer(0, 10);
        dtLeftTalon2FaultDbnc = new DaBouncer(0, 10);
        compressorCutoffFaultDbnc = new DaBouncer(0, 10);
        compressorPresenceFaultDbnc = new DaBouncer(0, 10);

        driverController = DriverController.getInstance();
        drivetrain = Drivetrain.getInstance();
        pneumatic = PneumaticsControl.getInstance();
        compressor = pneumatic.getCompressor();
    }

    public void update() {
        //Drivetrain left encoder
        if((driverController.getDriverFwdRevCmd() > 0) && (driverController.getDriverRotateCmd() == 0) && (drivetrain.getLeftWheelSpeedRPM()) <= 0.0) {
            // Driver command is forward, but left speed is not forward.
            dtLeftEncoderFault = dtLeftEncoderFaultDbnc.AboveDebounceBoo(true);
        } else if((driverController.getDriverFwdRevCmd() < 0) && (driverController.getDriverRotateCmd() == 0) && (drivetrain.getLeftWheelSpeedRPM()) >= 0.0)  {
            // Driver command is reverse, but right speed is not reverse.
            dtLeftEncoderFault = dtLeftEncoderFaultDbnc.AboveDebounceBoo(true);
        } else {
            dtLeftEncoderFault = dtLeftEncoderFaultDbnc.AboveDebounceBoo(false);
        }
        
        //Drivetrain right encoder
        if((driverController.getDriverFwdRevCmd() > 0) && (driverController.getDriverRotateCmd() == 0) && (drivetrain.getRightWheelSpeedRPM()) <= 0.0) {
            // Driver command is forward, but Right speed is not forward.
            dtRightEncoderFault = dtRightEncoderFaultDbnc.AboveDebounceBoo(true);
        } else if((driverController.getDriverFwdRevCmd() < 0) && (driverController.getDriverRotateCmd() == 0) && (drivetrain.getRightWheelSpeedRPM()) >= 0.0)  {
            // Driver command is reverse, but right speed is not reverse.
            dtRightEncoderFault = dtRightEncoderFaultDbnc.AboveDebounceBoo(true);
        } else {
            dtRightEncoderFault = dtRightEncoderFaultDbnc.AboveDebounceBoo(false);
        }

        //Drivetrain motor currents
        if((driverController.getDriverFwdRevCmd() != 0) || (driverController.getDriverRotateCmd() != 0)){
            if(drivetrain.getLeftTalon1Current() != 0){
                dtLeftTalon1Fault = dtLeftTalon1FaultDbnc.AboveDebounceBoo(true);
            } else {
                dtLeftTalon1Fault = dtLeftTalon1FaultDbnc.AboveDebounceBoo(false);
            }

            if(drivetrain.getLeftTalon2Current() != 0){
                dtLeftTalon2Fault = dtLeftTalon2FaultDbnc.AboveDebounceBoo(true);
            } else {
                dtLeftTalon2Fault = dtLeftTalon2FaultDbnc.AboveDebounceBoo(false);
            }

            if(drivetrain.getRightTalon1Current() != 0){
                dtRightTalon1Fault = dtRightTalon1FaultDbnc.AboveDebounceBoo(true);
            } else {
                dtRightTalon1Fault = dtRightTalon1FaultDbnc.AboveDebounceBoo(false);
            }

            if(drivetrain.getRightTalon2Current() != 0){
                dtRightTalon2Fault = dtRightTalon2FaultDbnc.AboveDebounceBoo(true);
            } else {
                dtRightTalon2Fault = dtRightTalon2FaultDbnc.AboveDebounceBoo(false);
            }
        } else {
            dtLeftTalon1Fault  = dtLeftTalon1FaultDbnc.AboveDebounceBoo(false);
            dtLeftTalon2Fault  = dtLeftTalon2FaultDbnc.AboveDebounceBoo(false);
            dtRightTalon1Fault = dtRightTalon1FaultDbnc.AboveDebounceBoo(false);
            dtRightTalon2Fault = dtRightTalon2FaultDbnc.AboveDebounceBoo(false);
        }


        //Compressor cutoff switch & pressure sensor cross-check
        if((compressor.getPressureSwitchValue() == false) && (pneumatic.getPressure() < 100)) {
            compressorCutoffFault = compressorCutoffFaultDbnc.AboveDebounceBoo(true);
        }else{
            compressorCutoffFault = compressorCutoffFaultDbnc.AboveDebounceBoo(false);
        }

        //Pressure sensor not-plugged-in check.
        if(pneumatic.getPressure() <= -5.0) {
            //if we think we have a vaccuum, well, we don't....
            compressorPresenceFault = compressorPresenceFaultDbnc.AboveDebounceBoo(true);
        } else {
            compressorPresenceFault = compressorPresenceFaultDbnc.AboveDebounceBoo(false);
        }

        faultDetected = dtLeftEncoderFault | dtRightEncoderFault | dtLeftTalon1Fault | dtLeftTalon2Fault | dtRightTalon1Fault | compressorCutoffFault | compressorPresenceFault;
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
            description = "Pressure Sensor Low Pressure Fault";
        }else if(compressorPresenceFault == true) {
            description = "Pressure Sensor Unplugged Fault";
        }else{
            description = "No Fault";
        } 
        return description; 
    }

}