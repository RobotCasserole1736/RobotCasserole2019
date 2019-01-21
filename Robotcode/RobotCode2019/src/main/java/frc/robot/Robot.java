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
import frc.robot.IntakeControl.IntakePos;
import frc.robot.IntakeControl.IntakeSpd;
import frc.robot.LEDController.LEDPatterns;
import frc.robot.PEZControl.GamePiece;
import frc.robot.PEZControl.PEZPos;


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

    //Website Utilities
    CasseroleWebServer webserver;
    CalWrangler wrangler;

    //Processor metric utilities
    CasseroleRIOLoadMonitor loadMon;

    //Physical devices
    PowerDistributionPanel pdp;

    BuiltInAccelerometer onboardAccel;

    //Top level telemetry signals
    Signal rioDS_SAMPLoad;
    Signal rioCurr_DrawLoad;
    Signal rioBat_VolLoad;
    Signal onboardAccelX;
    Signal onboardAccelY;
    Signal onboardAccelZ;

    //Vision Tracking Camera
    JeVoisInterface jevois;

    Ultrasonic testSensor;

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
        LEDController.getInstance();
        PneumaticsControl.getInstance();
        jevois = new JeVoisInterface(false);
        Arm.getInstance();
        Drivetrain.getInstance();
        Climber.getInstance();
        IntakeControl.getInstance();
        PEZControl.getInstance();
        onboardAccel = new BuiltInAccelerometer();

        testSensor = new Ultrasonic(3, "Test");

        /* Init input from humans */
        OperatorController.getInstance();
        DriverController.getInstance();

        /* Init software utilities */
        loadMon= new CasseroleRIOLoadMonitor();
        LoopTiming.getInstance();
        poseCalc = new RobotPose();

        /* Init local telemetry signals */
        rioDS_SAMPLoad = new Signal("dataserver stored samples", "count"); 
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
        LEDController.getInstance().setPattern(LEDPatterns.Pattern1);
    }

    @Override
    public void autonomousInit() {
        CasseroleDataServer.getInstance().logger.startLoggingAuto();
        LEDController.getInstance().setPattern(LEDPatterns.Pattern2);
    }


    /**
     * This function is called periodically in both Auto("sandstorm") and Teleop
     */
    private void matchPeriodicCommon(){
        LoopTiming.getInstance().markLoopStart();

        /* Sample inputs from humans */
        DriverController.getInstance().update();
        OperatorController.getInstance().update();


        /* Map subsystem IO */

        //Operator Controller provides commands to Arm
        Arm.getInstance().setIntakeActualState(IntakeControl.getInstance().getEstimatedPosition());
        Arm.getInstance().setManualMovementCmd(OperatorController.getInstance().getArmManualPosCmd());
        Arm.getInstance().setPositionCmd(OperatorController.getInstance().getArmPosReq());
        Arm.getInstance().update();

        //Update Gripper Control
        if(OperatorController.getInstance().getBallPickupReq()){
            //Cargo Pickup is requested
            PEZControl.getInstance().setPositionCmd(PEZPos.CargoGrab);
        } else if(OperatorController.getInstance().getHatchPickupReq()){
            //Hatch Pickup Requested
            PEZControl.getInstance().setPositionCmd(PEZPos.HatchGrab);
        } else if(OperatorController.getInstance().getReleaseReq()) {
            //Release whatever we currently have in our gripper
            if(PEZControl.getInstance().getHeldGamePiece() == GamePiece.Cargo){
                PEZControl.getInstance().setPositionCmd(PEZPos.CargoRelease);
            } else if(PEZControl.getInstance().getHeldGamePiece() == GamePiece.Hatch){
                PEZControl.getInstance().setPositionCmd(PEZPos.HatchRelease);
            } else {
                PEZControl.getInstance().setPositionCmd(PEZPos.None);
            }
        } else {
            PEZControl.getInstance().setPositionCmd(PEZPos.None);
        }
        PEZControl.getInstance().update();


        //Arbitrate Intake commands from driver and operator and arm
        if(DriverController.getInstance().getIntakePosReq() == IntakePos.Extend || 
           OperatorController.getInstance().getIntakePosReq() == IntakePos.Extend || 
           Arm.getInstance().intakeExtendOverride() == true) {
            IntakeControl.getInstance().setPositionCmd(IntakePos.Extend);
        } else {
            IntakeControl.getInstance().setPositionCmd(IntakePos.Retract);
        }

        if(DriverController.getInstance().getIntakeSpdReq() == IntakeSpd.Eject ||
           OperatorController.getInstance().getIntakeSpdReq() == IntakeSpd.Eject ){
            IntakeControl.getInstance().setSpeedCmd(IntakeSpd.Eject);
        } else if(DriverController.getInstance().getIntakeSpdReq() == IntakeSpd.Intake ||
                  OperatorController.getInstance().getIntakeSpdReq() == IntakeSpd.Intake ){
            IntakeControl.getInstance().setSpeedCmd(IntakeSpd.Intake);
        } else {
            IntakeControl.getInstance().setSpeedCmd(IntakeSpd.Stop);
        }

        IntakeControl.getInstance().update();


        //Arbitrate driver & auto sequencer inputs to drivetrain
        if(DriverController.getInstance().getGyroAngleLockReq()){
            //Map driver inputs to drivetrain in gyro-lock mode
            Drivetrain.getInstance().setGyroLockCmd(DriverController.getInstance().getDriverFwdRevCmd());
        } else {
            // Map driver inputs to drivetrain open loop
            Drivetrain.getInstance().setOpenLoopCmd(DriverController.getInstance().getDriverFwdRevCmd(), DriverController.getInstance().getDriverRotateCmd());
        }

        Drivetrain.getInstance().update();

        poseCalc.setLeftMotorSpeed(Drivetrain.getInstance().getLeftWheelSpeedRPM());
        poseCalc.setRightMotorSpeed(Drivetrain.getInstance().getRightWheelSpeedRPM());
        poseCalc.update();


        /* Update Other subsytems */
        LEDController.getInstance().update();
        PneumaticsControl.getInstance().update();
        Climber.getInstance().update();
        telemetryUpdate();


        
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
        LEDController.getInstance().setPattern(LEDPatterns.Pattern4);
    }

    /**
     * This function is called periodically during disabled mode.
     */
    @Override
    public void disabledPeriodic() {
        LoopTiming.getInstance().markLoopStart();

        /* Sample inputs from humans to keep telemetry updated, but we won't actually use it. */
        DriverController.getInstance().update();
        OperatorController.getInstance().update();


        /* Map subsystem IO */

        //Initial Match State - Arm in Lower Position
        Arm.getInstance().setIntakeActualState(IntakeControl.getInstance().getEstimatedPosition());
        Arm.getInstance().setManualMovementCmd(0);
        Arm.getInstance().setPositionCmd(ArmPosReq.Lower);
        Arm.getInstance().update();

        //Update Gripper Control - pull position command from driver view interface.
        String gp_start = CasseroleDriverView.getAutoSelectorVal("Starting Gamepiece");
        if(gp_start.compareTo(GamePiece.Cargo.toString())==0){
            PEZControl.getInstance().setPositionCmd(PEZPos.CargoGrab);
        } else if(gp_start.compareTo(GamePiece.Hatch.toString())==0){
            PEZControl.getInstance().setPositionCmd(PEZPos.HatchGrab);
        } else {
            PEZControl.getInstance().setPositionCmd(PEZPos.HatchRelease);
        }
        PEZControl.getInstance().update();


        //Start intake within frame perimiter and in safe state
        IntakeControl.getInstance().setPositionCmd(IntakePos.Retract);
        IntakeControl.getInstance().setSpeedCmd(IntakeSpd.Stop);
        IntakeControl.getInstance().update();


        //Keep drivetrain stopped.
        Drivetrain.getInstance().setOpenLoopCmd(0,0);
        Drivetrain.getInstance().update();

        poseCalc.setLeftMotorSpeed(Drivetrain.getInstance().getLeftWheelSpeedRPM());
        poseCalc.setRightMotorSpeed(Drivetrain.getInstance().getRightWheelSpeedRPM());
        poseCalc.update();


        /* Update Other subsytems */
        LEDController.getInstance().update();
        PneumaticsControl.getInstance().update();
        Climber.getInstance().update();
        telemetryUpdate();


        
        LoopTiming.getInstance().markLoopEnd();
    }
    
//////////////////////////////////////////////////////////////////////////
// Utilties
//////////////////////////////////////////////////////////////////////////
    private void telemetryUpdate(){
        double sample_time_ms = LoopTiming.getInstance().getLoopStartTime_sec()*1000.0;

        /* Update main loop signals */
        rioDS_SAMPLoad.addSample(sample_time_ms,CasseroleDataServer.getInstance().getTotalStoredSamples());
        rioCurr_DrawLoad.addSample(sample_time_ms,pdp.getTotalCurrent());
        rioBat_VolLoad.addSample(sample_time_ms,pdp.getVoltage());  
        onboardAccelX.addSample(sample_time_ms, onboardAccel.getX());
        onboardAccelY.addSample(sample_time_ms, onboardAccel.getY());
        onboardAccelZ.addSample(sample_time_ms, onboardAccel.getZ());
    
        CasseroleDriverView.setDialValue("Main System Pressure", PneumaticsControl.getInstance().getPressure());
        CasseroleDriverView.setDialValue("Speed", poseCalc.getRobotVelocity_ftpersec());
        CasseroleDriverView.setDialValue("Arm Angle", Arm.getInstance().getActualArmHeight());
        CasseroleDriverView.setBoolean("Gyro Offline", !Drivetrain.getInstance().isGyroOnline());
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
