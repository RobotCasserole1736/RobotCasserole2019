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
import frc.robot.Arm.ArmPosReq;
import frc.robot.LEDController.LEDPatterns;
import frc.robot.PEZControl.GamePiece;


/**
 * The VM is configured to automatically run this class, and to call the
 * functions corresponding to each mode, as described in the TimedRobot
 * documentation. If you change the name of this class or the package after
 * 
 * creating this project, you must also update the build.gradle file in the
 * project.
 */
public class Robot extends TimedRobot {

    RobotPose poseCalc;
    MatchState matchState;

    //Website Utilities
    CasseroleWebServer webserver;
    CalWrangler wrangler;
    CasseroleDataServer dataServer;

    //Processor metric utilities
    CasseroleRIOLoadMonitor loadMon;
    LoopTiming loopTiming;

    //Operator and Driver controllers
    DriverController driverController;
    OperatorController operatorController;

    //Physical devices
    PowerDistributionPanel pdp;
    Ultrasonic testSensor;
    BuiltInAccelerometer onboardAccel;
    LEDController ledController;
    PneumaticsControl pneumaticsControl;
    Arm arm;
    Drivetrain drivetrain;
    Climber climber;
    IntakeControl intakeControl;
    PEZControl pezControl;

    //Top level telemetry signals
    Signal rioDSSampLoad;
    Signal rioCurrDrawLoad;
    Signal rioBattVoltLoad;
    Signal onboardAccelX;
    Signal onboardAccelY;
    Signal onboardAccelZ;

    //Vision Tracking Camera
    JeVoisInterface jevois;

    /**
     * This function is run when the robot is first started up and should be
     * used for any initialization code.
     */
    @Override
    public void robotInit() {
        /* Init website utilties */
        webserver = new CasseroleWebServer();
        wrangler = new CalWrangler();

        /* Init Robot parts */
        pdp = new PowerDistributionPanel(RobotConstants.POWER_DISTRIBUTION_PANEL_CANID);
        ledController = LEDController.getInstance();
        pneumaticsControl = PneumaticsControl.getInstance();
        jevois = new JeVoisInterface(false);
        arm = Arm.getInstance();
        drivetrain = Drivetrain.getInstance();
        climber = Climber.getInstance();
        intakeControl = IntakeControl.getInstance();
        pezControl = PEZControl.getInstance();
        onboardAccel = new BuiltInAccelerometer();

        testSensor = new Ultrasonic(3, "Test");

        /* Init input from humans */
        operatorController = OperatorController.getInstance();
        driverController = DriverController.getInstance();

        /* Init software utilities */
        loadMon= new CasseroleRIOLoadMonitor();
        loopTiming = LoopTiming.getInstance();
        poseCalc = new RobotPose();
        matchState = MatchState.getInstance();

        /* Init local telemetry signals */
        rioDSSampLoad = new Signal("dataserver stored samples", "count"); 
        rioCurrDrawLoad = new Signal("overall current draw", "A");
        rioBattVoltLoad = new Signal("battery voltage", "V");
        onboardAccelX = new Signal("Onboard Accelerometer X Value", "g");
        onboardAccelY = new Signal("Onboard Accelerometer Y Value", "g");
        onboardAccelZ = new Signal("Onboard Accelerometer Z Value", "g");
        
        /* Website setup */
        initDriverView();

        /* Fire up webserver & telemetry dataserver */
        webserver.startServer();
        dataServer = CasseroleDataServer.getInstance();
        dataServer.startServer();
    }

/////////////////////////////////////////////////////////////////////////////////////
// Match-active Init & Periodic Functions
/////////////////////////////////////////////////////////////////////////////////////
    @Override
    public void teleopInit() {
        dataServer.logger.startLoggingTeleop();
        ledController.setPattern(LEDPatterns.Pattern1);
        matchState.SetPeriod(MatchState.Period.OperatorControl);
    }

    @Override
    public void autonomousInit() {
        dataServer.logger.startLoggingAuto();
        ledController.setPattern(LEDPatterns.Pattern2);
        matchState.SetPeriod(MatchState.Period.Autonomous);
    }


    /**
     * This function is called periodically in both Auto("sandstorm") and Teleop
     */
    private void matchPeriodicCommon(){
        loopTiming.markLoopStart();

        /* Sample inputs from humans */
        driverController.update();
        operatorController.update();


        /* Map subsystem IO */

        //Operator Controller provides commands to Arm
        arm.setIntakeActualState(intakeControl.getEstimatedPosition());
        arm.setManualMovementCmd(operatorController.getArmManualPosCmd());
        arm.setPositionCmd(operatorController.getArmPosReq());
        arm.update();

        pezControl.update();

        intakeControl.update();


        //Arbitrate driver & auto sequencer inputs to drivetrain
        if(driverController.getGyroAngleLockReq()){
            //Map driver inputs to drivetrain in gyro-lock mode
            drivetrain.setGyroLockCmd(driverController.getDriverFwdRevCmd());
        } else {
            // Map driver inputs to drivetrain open loop
            drivetrain.setOpenLoopCmd(driverController.getDriverFwdRevCmd(), driverController.getDriverRotateCmd());
        }

        drivetrain.update();

        poseCalc.setLeftMotorSpeed(drivetrain.getLeftWheelSpeedRPM());
        poseCalc.setRightMotorSpeed(drivetrain.getRightWheelSpeedRPM());
        poseCalc.update();

        /* Update Other subsytems */
        ledController.update();
        pneumaticsControl.update();
        climber.update();
        telemetryUpdate();

        loopTiming.markLoopEnd();
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
        dataServer.logger.stopLogging();
        ledController.setPattern(LEDPatterns.Pattern4);
        matchState.SetPeriod(MatchState.Period.Disabled);
    }

    /**
     * This function is called periodically during disabled mode.
     */
    @Override
    public void disabledPeriodic() {
        loopTiming.markLoopStart();

        /* Sample inputs from humans to keep telemetry updated, but we won't actually use it. */
        driverController.update();
        operatorController.update();


        /* Map subsystem IO */

        //Initial Match State - Arm in Lower Position
        arm.setIntakeActualState(intakeControl.getEstimatedPosition());
        arm.setManualMovementCmd(0);
        arm.setPositionCmd(ArmPosReq.Lower);
        arm.update();

        pezControl.update();

        intakeControl.update();


        //Keep drivetrain stopped.
        drivetrain.setOpenLoopCmd(0,0);
        drivetrain.update();

        poseCalc.setLeftMotorSpeed(drivetrain.getLeftWheelSpeedRPM());
        poseCalc.setRightMotorSpeed(drivetrain.getRightWheelSpeedRPM());
        poseCalc.update();


        /* Update Other subsytems */
        ledController.update();
        pneumaticsControl.update();
        climber.update();
        telemetryUpdate();

        loopTiming.markLoopEnd();
    }
    
//////////////////////////////////////////////////////////////////////////
// Utilties
//////////////////////////////////////////////////////////////////////////
    private void telemetryUpdate(){
        double sampleTimeMs = loopTiming.getLoopStartTimeSec()*1000.0;

        /* Update main loop signals */
        rioDSSampLoad.addSample(sampleTimeMs, dataServer.getTotalStoredSamples());
        rioCurrDrawLoad.addSample(sampleTimeMs, pdp.getTotalCurrent());
        rioBattVoltLoad.addSample(sampleTimeMs, pdp.getVoltage());  
        onboardAccelX.addSample(sampleTimeMs, onboardAccel.getX());
        onboardAccelY.addSample(sampleTimeMs, onboardAccel.getY());
        onboardAccelZ.addSample(sampleTimeMs, onboardAccel.getZ());
    
        CasseroleDriverView.setDialValue("Main System Pressure", pneumaticsControl.getPressure());
        CasseroleDriverView.setDialValue("Speed", poseCalc.getRobotVelocity_ftpersec());
        CasseroleDriverView.setDialValue("Arm Angle", arm.getActualArmHeight());
        CasseroleDriverView.setBoolean("Gyro Offline", !drivetrain.isGyroOnline());
        CasseroleDriverView.setBoolean("Vision Camera Offline", !jevois.isVisionOnline());
        CasseroleDriverView.setBoolean("Vision Target Available", jevois.isTgtVisible());
    }
        
    /**
     * This function sets up the driver view website
     */
    private void initDriverView(){
        String[] gpOptions =    {GamePiece.Cargo.toString(), GamePiece.Hatch.toString(), GamePiece.Nothing.toString()};
        CasseroleDriverView.newAutoSelector("Starting Gamepiece", gpOptions);
        CasseroleDriverView.newDial("Main System Pressure", 0, 140, 10, 80, 125);
        CasseroleDriverView.newWebcam("cam1", RobotConstants.CAM_1_STREAM_URL, 0, 0, 0);
        CasseroleDriverView.newWebcam("cam2", RobotConstants.CAM_2_STREAM_URL, 0, 0, 0);
        CasseroleDriverView.newDial("Speed", 0, 20, 2,  1, 15);
        CasseroleDriverView.newDial("Arm Angle", -45, 225, 15,  -45, 100);
        CasseroleDriverView.newBoolean("Gyro Offline", "red");
        CasseroleDriverView.newBoolean("Vision Camera Offline", "red");
        CasseroleDriverView.newBoolean("Vision Target Available", "green");
    }
}
