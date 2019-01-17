/*----------------------------------------------------------------------------*/
/* Copyright (c) 2017-2018 FIRST. All Rights Reserved.                                                */
/* Open Source Software - may be modified and shared by FRC teams. The code     */
/* must be accompanied by the FIRST BSD license file in the root directory of */
/* the project.                                                                                                                             */
/*----------------------------------------------------------------------------*/

package frc.robot;

import edu.wpi.first.wpilibj.BuiltInAccelerometer;

/*
 *******************************************************************************************
 * Copyright (C) 2019 FRC Team 1736 Robot Casserole - www.robotcasserole.org
 *******************************************************************************************
 *
 * This software is released under the MIT Licence - see the license.txt
 *    file in the root of this repo.
 *
 * Non-legally-binding statement from Team 1736:
 *    Thank you for taking the time to read through our software! We hope you
 *     find it educational and informative! 
 *    Please feel free to snag our software for your own use in whatever project
 *     you have going on right now! We'd love to be able to help out! Shoot us 
 *     any questions you may have, all our contact info should be on our website
 *     (listed above).
 *    If you happen to end up using our software to make money, that is wonderful!
 *     Robot Casserole is always looking for more sponsors, so we'd be very appreciative
 *     if you would consider donating to our club to help further STEM education.
 */

import edu.wpi.first.wpilibj.PowerDistributionPanel;
import edu.wpi.first.wpilibj.TimedRobot;
import frc.lib.Calibration.CalWrangler;
import frc.lib.DataServer.CasseroleDataServer;
import frc.lib.DataServer.Signal;
import frc.lib.LoadMon.CasseroleRIOLoadMonitor;
import frc.lib.WebServer.CasseroleDriverView;
import frc.lib.WebServer.CasseroleWebServer;


/**
 * The VM is configured to automatically run this class, and to call the
 * functions corresponding to each mode, as described in the TimedRobot
 * documentation. If you change the name of this class or the package after
 * 
 * creating this project, you must also update the build.gradle file in the
 * project.
 */
public class Robot extends TimedRobot {


    RobotPose RobotPose;

    //Website Utilities
    CasseroleWebServer webserver;
    CalWrangler wrangler;

    //Processor metric utilities
    CasseroleRIOLoadMonitor loadMon;

    //Physical devices
    PowerDistributionPanel pdp;

    BuiltInAccelerometer onboardAccel;

    //Top level telemetry signals
    Signal rioCPULoad;
    Signal rioMemLoad;
    Signal rioDS_SAMPLoad;
    Signal rioCurr_DrawLoad;
    Signal rioBat_VolLoad;
    Signal onboardAccelX;
    Signal onboardAccelY;
    Signal onboardAccelZ;

    /**
     * @param rioCPULoad the rioCPULoad to set
     */
    public void setRioCPULoad(Signal rioCPULoad) {
        this.rioCPULoad = rioCPULoad;
    }
    
    /**
     * This function is run when the robot is first started up and should be
     * used for any initialization code.
     */
    @Override
    public void robotInit() {
        /* Init website utilties */
        webserver = new CasseroleWebServer();
        wrangler = new CalWrangler();

        RobotPose = new RobotPose();

        onboardAccel = new BuiltInAccelerometer();

        RobotPose.robotPose();
        /* Init Robot parts */
        pdp = new PowerDistributionPanel(RobotConstants.POWER_DISTRIBUTION_PANEL_CANID);
        LEDController.getInstance();
        PneumaticsControl.getInstance();
        //Arm.getInstance();
        Drivetrain.getInstance();
        Climber.getInstance();
        //Intake
        IntakeControl.getInstance();

        /* Init input from humans */
        OperatorController.getInstance();
        DriverController.getInstance();

        /* Init software utilities */
        loadMon= new CasseroleRIOLoadMonitor();
        LoopTiming.getInstance();

        /* Init local telemetry signals */
        rioCPULoad = new Signal("roboRIO CPU Load", "Pct");
        rioMemLoad = new Signal("roboRIO Memory Load", "Pct"); 
        rioDS_SAMPLoad = new Signal("dataserver stored samples", "Pct"); 
        rioCurr_DrawLoad = new Signal("overall current draw", "A");
        rioBat_VolLoad = new Signal("battery voltage", "V");
        onboardAccelX = new Signal("Onboard Accelerometer X Value", "g");
        onboardAccelY = new Signal("Onboard Accelerometer Y Value", "g");
        onboardAccelZ = new Signal("Onboard Accelerometer Z Value", "g");
        
        /* Website setup */
        initDriverView();

        /* Fire up webserver & telemetry dataserver */
        webserver.startServer();
        CasseroleDataServer.getInstance().startServer();
    }

/////////////////////////////////////////////////////////////////////////////////////
// Match-active Init & Periodic Functions
/////////////////////////////////////////////////////////////////////////////////////
    @Override
    public void teleopInit() {
        CasseroleDataServer.getInstance().logger.startLoggingTeleop();
    }

    @Override
    public void autonomousInit() {
        CasseroleDataServer.getInstance().logger.startLoggingAuto();
    }


    /**
     * This function is called periodically in both Auto("sandstorm") and Teleop
     */
    private void matchPeriodicCommon(){
        LoopTiming.getInstance().markLoopStart();

        /* Sample inputs from humans */
        DriverController.getInstance().update();
        OperatorController.getInstance().update();

        IntakeControl.getInstance().setPositionCmd(DriverController.getInstance().getIntakePosReq());
        IntakeControl.getInstance().setSpeedCmd(DriverController.getInstance().getIntakeSpdReq());
        IntakeControl.getInstance().update();

        /* Map subsystem IO */
        if(DriverController.getInstance().getGyroAngleLockReq()){
            //Map driver inputs to drivetrain in gyro-lock mode
            Drivetrain.getInstance().setGyroLockCmd(DriverController.getInstance().getDriverFwdRevCmd());
        } else {
            // Map driver inputs to drivetrain open loop
            Drivetrain.getInstance().setOpenLoopCmd(DriverController.getInstance().getDriverFwdRevCmd(), DriverController.getInstance().getDriverRotateCmd());
        }

        /* Update subsytems */
        LEDController.getInstance().update();
        Drivetrain.getInstance().update();
        PneumaticsControl.getInstance().update();
        //Arm.getInstance().update();
        Climber.getInstance().update();
        telemetryUpdate();
        RobotPose.update();
        
        LoopTiming.getInstance().markLoopEnd();

    }

    /**
     * This function is called periodically during operator control only.
     */
    @Override
    public void teleopPeriodic() {
        matchPeriodicCommon();
    }

    /**
     * This function is called periodically during sandstorm only.
     */
    @Override
    public void autonomousPeriodic() {
        matchPeriodicCommon();
    }


/////////////////////////////////////////////////////////////////////////////////////
// Disabled Init & Periodic Functions
/////////////////////////////////////////////////////////////////////////////////////
    /**
     * This function is called once right before the start of disabled mode.
     */
    @Override
    public void disabledInit() {
        CasseroleDataServer.getInstance().logger.stopLogging();
    }

    /**
     * This function is called periodically during disabled mode.
     */
    @Override
    public void disabledPeriodic() {
        LoopTiming.getInstance().markLoopStart();

        /* Read from humans to keep telemetry up to date */
        DriverController.getInstance().update();
        OperatorController.getInstance().update();
        
        /*Set commands to safe stopped state */
        Drivetrain.getInstance().setOpenLoopCmd(0,0);

        /* Update subsystems */
        LEDController.getInstance().update();
        Drivetrain.getInstance().update();
        PneumaticsControl.getInstance().update();
        //Arm.getInstance().update();
        Climber.getInstance().update();
        RobotPose.update();

        telemetryUpdate();
        LoopTiming.getInstance().markLoopEnd();
    }
    
//////////////////////////////////////////////////////////////////////////
// Utilties
//////////////////////////////////////////////////////////////////////////
    private void telemetryUpdate(){
        double sample_time_ms = LoopTiming.getInstance().getLoopStartTime_sec()*1000.0;

        /* Update main loop signals */
        rioCPULoad.addSample(sample_time_ms,loadMon.getCPULoadPct());
        rioMemLoad.addSample(sample_time_ms,loadMon.getMemLoadPct());
        rioDS_SAMPLoad.addSample(sample_time_ms,CasseroleDataServer.getInstance().getTotalStoredSamples());
        rioCurr_DrawLoad.addSample(sample_time_ms,pdp.getTotalCurrent());
        rioBat_VolLoad.addSample(sample_time_ms,pdp.getVoltage());  
        onboardAccelX.addSample(sample_time_ms, onboardAccel.getX());
        onboardAccelY.addSample(sample_time_ms, onboardAccel.getY());
        onboardAccelZ.addSample(sample_time_ms, onboardAccel.getZ());
    
        CasseroleDriverView.setDialValue("Main System Pressure", PneumaticsControl.getInstance().getPressure());
        CasseroleDriverView.setBoolean("Gyro Offline", !Drivetrain.getInstance().isGyroOnline());
    }
        
    /**
     * This function sets up the driver view website
     */
    private void initDriverView(){
        String[] gpOptions =    {"Cargo (ball)", "Hatch Panel", "Nothing"};
        CasseroleDriverView.newAutoSelector("Starting Gamepiece", gpOptions);
        CasseroleDriverView.newDial("Main System Pressure", 0, 140, 10, 80, 125);
        CasseroleDriverView.newWebcam("cam1", RobotConstants.CAM_1_STREAM_URL, 0, 0, 0);
        CasseroleDriverView.newWebcam("cam2", RobotConstants.CAM_2_STREAM_URL, 0, 0, 0);
        CasseroleDriverView.newBoolean("Gyro Offline", "red");
    }
}
