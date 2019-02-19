/*----------------------------------------------------------------------------*/
/* Copyright (c) 2017-2018 FIRST. All Rights Reserved.                        */
/* Open Source Software - may be modified and shared by FRC teams. The code   */
/* must be accompanied by the FIRST BSD license file in the root directory of */
/* the project.                                                               */
/*----------------------------------------------------------------------------*/

package frc.robot;

import edu.wpi.first.wpilibj.BuiltInAccelerometer;
import edu.wpi.first.wpilibj.DriverStation;

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
import edu.wpi.first.wpilibj.RobotController;
import edu.wpi.first.wpilibj.TimedRobot;
import edu.wpi.first.wpilibj.GenericHID.Hand;
import frc.lib.Calibration.CalWrangler;
import frc.lib.DataServer.CasseroleDataServer;
import frc.lib.DataServer.Signal;
import frc.lib.LoadMon.CasseroleRIOLoadMonitor;
import frc.lib.WebServer.CasseroleDriverView;
import frc.lib.WebServer.CasseroleWebServer;
import frc.lib.Util.CrashTracker;
import frc.robot.Arm.ArmPos;
import frc.robot.IntakeControl.IntakePos;
import frc.robot.LEDController.LEDPatterns;
import frc.robot.PEZControl.PEZPos;
import frc.robot.Superstructure.OpMode;
import frc.robot.auto.AutoSeqDistToTgtEst;
import frc.robot.auto.Autonomous;



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
    BuiltInAccelerometer onboardAccel;
    LEDController ledController;
    PneumaticsControl pneumaticsControl;
    Arm arm;
    Drivetrain drivetrain;
    Climber climber;
    IntakeControl intakeControl;
    PEZControl pezControl;
    FrontUltrasonic frontUltrasonic;
    BackUltrasonic backUltrasonic;
    LineFollower linefollow;
    Superstructure superstructure;

    //Top level telemetry signals
    Signal rioDSSampLoadSig;
    Signal rioDSLogQueueLenSig;
    Signal rioCurrDrawLoadSig;
    Signal rioBattVoltLoadSig;
    Signal dtFwdRevAccelSig;
    Signal dtLeftRightAccelSig;
    Signal dtUpDownAccelSig;
    Signal intakeLeftCurrentSig;
    Signal intakeRightCurrentSig;
    Signal climberReleaseMotorCurrentSig;
    Signal rioIsBrownoutSig;
    Signal matchTimeSig;
    Signal rioCANBusUsagePctSig;

    //Vision Tracking Camera
    JeVoisInterface jevois;
    VisionLEDRingControl eyeOfVeganSauron;

    //Auto Aignment Routines
    Autonomous autonomous;

    //Sensor Cross-Checking
    SensorCheck sensorCheck; 

    //Safety check for robot state
    boolean wasAutoRun = false;

    public Robot() {
        super(RobotConstants.MAIN_LOOP_SAMPLE_RATE_S);
        CrashTracker.logRobotConstruction();
    }

    /**
     * This function is run when the robot is first started up and should be
     * used for any initialization code.
     */
    @Override
    public void robotInit() {
        try{
            CrashTracker.logRobotInit();

            Thread.currentThread().setPriority(10);

            RioSimMode.getInstance();

            /* Init website utilties */
            webserver = new CasseroleWebServer();
            wrangler = new CalWrangler();
            dataServer = CasseroleDataServer.getInstance();

            /* Init Robot parts */
            pdp = new PowerDistributionPanel(RobotConstants.POWER_DISTRIBUTION_PANEL_CANID);
            ledController = LEDController.getInstance();
            pneumaticsControl = PneumaticsControl.getInstance();
            jevois = JeVoisInterface.getInstance();
            arm = Arm.getInstance();
            drivetrain = Drivetrain.getInstance();
            climber = Climber.getInstance();
            intakeControl = IntakeControl.getInstance();
            pezControl = PEZControl.getInstance();
            onboardAccel = new BuiltInAccelerometer();
            frontUltrasonic = FrontUltrasonic.getInstance();
            backUltrasonic = BackUltrasonic.getInstance();
            linefollow = LineFollower.getInstance();
            superstructure = Superstructure.getInstance();
            eyeOfVeganSauron = VisionLEDRingControl.getInstance();

            /* Init input from humans */
            operatorController = OperatorController.getInstance();
            driverController = DriverController.getInstance();

            /* Init software utilities */
            loadMon= new CasseroleRIOLoadMonitor();
            loopTiming = LoopTiming.getInstance();
            poseCalc = new RobotPose();
            matchState = MatchState.getInstance();
            DrivetrainClosedLoopTestVectors.getInstance();
            AutoSeqDistToTgtEst.getInstance();
            autonomous = Autonomous.getInstance();
            sensorCheck = SensorCheck.getInstance();

            /* Init local telemetry signals */
            rioDSSampLoadSig = new Signal("Dataserver Stored Samples", "count"); 
            rioCurrDrawLoadSig = new Signal("Battery Current Draw", "A");
            rioBattVoltLoadSig = new Signal("Battery Voltage", "V");
            rioDSLogQueueLenSig = new Signal("Dataserver File Logger Queue Length", "count");
            dtFwdRevAccelSig = new Signal("Drivetrain Fwd/Rev Acceleration", "g");
            dtLeftRightAccelSig = new Signal("Drivetrain Left/Right Acceleration", "g");
            dtUpDownAccelSig = new Signal("Drivetrain Up/Down Acceleration", "g");
            intakeLeftCurrentSig = new Signal("Intake Left Motor Current", "A");
            intakeRightCurrentSig = new Signal("Intake Right Motor Current", "A");
            climberReleaseMotorCurrentSig = new Signal("Climber Release Motor Current", "A");
            rioIsBrownoutSig = new Signal("Robot Brownout", "bool");
            matchTimeSig = new Signal("Match Time", "sec");
            rioCANBusUsagePctSig = new Signal("Robot CAN Bus Utilization", "pct");

            
            /* Website setup */
            initDriverView();

            /* Fire up webserver & telemetry dataserver */
            webserver.startServer();
            dataServer.startServer();

            /* print the MAC address to the console for debugging */
            CrashTracker.logAndPrint("[MAC] Current MAC address: " + RioSimMode.getInstance().getMACAddr());
        } catch(Throwable t) {
            CrashTracker.logThrowableCrash(t);
            throw t;
        }
    }

/////////////////////////////////////////////////////////////////////////////////////
// Match-active Init & Periodic Functions
/////////////////////////////////////////////////////////////////////////////////////
    @Override
    public void teleopInit() {
        try{
            /*Update CrashTracker*/
            CrashTracker.logTeleopInit();

            //Intake check - if it's not at the correct location, kill the robot!
            if((intakeControl.getLeftArmPosition() > 15 && intakeControl.getRightArmPosition() > 15) && !wasAutoRun) {
                System.out.println("Error: !!! UNSAFE STARTUP STATE !!!");
                throw new Error("Error!!! Robot started in unsafe state");
            }

            dataServer.logger.startLoggingTeleop();
            matchState.SetPeriod(MatchState.Period.OperatorControl);
            intakeControl.closedLoop();
            eyeOfVeganSauron.setLEDRingState(true);
        } catch(Throwable t) {
            CrashTracker.logThrowableCrash(t);
            throw t;
        }
    }

    @Override
    public void autonomousInit() {
        try{
            /*Update CrashTracker*/
            CrashTracker.logAutoInit();
            wasAutoRun = true;

            //Intake check - if it's not at the correct location, kill the robot!
            if(intakeControl.getLeftArmPosition() > 15 && intakeControl.getRightArmPosition() > 15) {
                System.out.println("Error: !!! UNSAFE STARTUP STATE !!!");
                throw new Error("Error!!! Robot started in unsafe state");
            }

            dataServer.logger.startLoggingAuto();
            ledController.setPattern(LEDPatterns.Pattern4);
            matchState.SetPeriod(MatchState.Period.Autonomous);
            intakeControl.closedLoop();
            eyeOfVeganSauron.setLEDRingState(false); //test
            setMatchInitialCommands();
            pneumaticsControl.start();
            CrashTracker.logMatchInfo();
        } catch(Throwable t) {
            CrashTracker.logThrowableCrash(t);
            throw t;
        }
    }


    /**
     * This function is called periodically in both Auto("sandstorm") and Teleop
     */
    private void matchPeriodicCommon(){
        try{
            loopTiming.markLoopStart();

            eyeOfVeganSauron.setLEDRingState(true);

            /* Sample Sensors */
            frontUltrasonic.update();
            backUltrasonic.update();
            linefollow.update();

            /* Sample inputs from humans */
            driverController.update();
            operatorController.update();

            superstructure.update();

            autonomous.update();

            arm.update();

            pezControl.update();

            intakeControl.update();

            sensorCheck.update();

            if(arm.getActualArmHeight() < 90) {
                AutoSeqDistToTgtEst.getInstance().setVisionDistanceEstimate(jevois.getTgtPositionY(), jevois.isTgtVisible());
                AutoSeqDistToTgtEst.getInstance().setUltrasonicDistanceEstimate(frontUltrasonic.getdistance_ft(), true);
                AutoSeqDistToTgtEst.getInstance().setRobotLinearVelocity(poseCalc.getRobotVelocity_ftpersec());
            } else{
                AutoSeqDistToTgtEst.getInstance().setVisionDistanceEstimate(0, false);
                AutoSeqDistToTgtEst.getInstance().setUltrasonicDistanceEstimate(backUltrasonic.getdistance_ft(), true);
                AutoSeqDistToTgtEst.getInstance().setRobotLinearVelocity(-1 * poseCalc.getRobotVelocity_ftpersec());
            }
            AutoSeqDistToTgtEst.getInstance().update();

            DrivetrainClosedLoopTestVectors.getInstance().update();

            //Arbitrate driver & auto sequencer inputs to drivetrain
            if(autonomous.getAutoSeqActive() || DrivetrainClosedLoopTestVectors.getInstance().isTestActive()){
                //Do nothing - someone else will be setting drivetrain commands.
            } else if(driverController.getGyroAngleLockReq()){
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
            
            /*Update CrashTracker*/
            CrashTracker.logTeleopPeriodic();
            loopTiming.markLoopEnd();


            if(autonomous.getAutoFailed() == true){
                ledController.setPattern(LEDPatterns.Pattern6);
            } else {
                ledController.setPattern(LEDPatterns.Pattern3);
            }

        } catch(Throwable t) {
            CrashTracker.logThrowableCrash(t);
            throw t;
        }

    }

    /**
     * This function is called periodically during operator control only.
     */
    @Override
    public void teleopPeriodic() {
        /*Update CrashTracker*/
        CrashTracker.logTeleopPeriodic();
        matchPeriodicCommon();

    }

    /**
     * This function is called periodically during sandstorm only.
     */
    @Override
    public void autonomousPeriodic() {
        /*Update CrashTracker*/
        CrashTracker.logAutoPeriodic();
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
        try{
            CrashTracker.logDisabledInit();
            dataServer.logger.stopLogging();
            ledController.setPattern(LEDPatterns.Pattern2);
            matchState.SetPeriod(MatchState.Period.Disabled);
            eyeOfVeganSauron.setLEDRingState(false);
            arm.forceArmStop();

        } catch(Throwable t) {
            CrashTracker.logThrowableCrash(t);
            throw t;
        }
    }

    /**
     * This function is called periodically during disabled mode.
     */
    @Override
    public void disabledPeriodic() {
        try{
            loopTiming.markLoopStart();

            /*Update CrashTracker*/
            CrashTracker.logDisabledPeriodic();

            /* Sample Sensors */
            frontUltrasonic.update();
            backUltrasonic.update();
            linefollow.update();

            /* Sample inputs from humans to keep telemetry updated, but we won't actually use it. */
            driverController.update();
            operatorController.update();

            superstructure.update();
            sensorCheck.update();

            /* Map subsystem IO */

            //Initial Match State - Arm Not Moving
            arm.setManualMovementCmd(0);
            arm.setPositionCmd(ArmPos.Disabled);
            arm.updateCalValues(false);
            arm.update();

            pezControl.update();

            intakeControl.update();

            //Keep drivetrain stopped.
            drivetrain.setOpenLoopCmd(0,0);

            drivetrain.update();
            drivetrain.updateGains(false);

            poseCalc.setLeftMotorSpeed(drivetrain.getLeftWheelSpeedRPM());
            poseCalc.setRightMotorSpeed(drivetrain.getRightWheelSpeedRPM());
            poseCalc.setMeasuredPoseAngle(drivetrain.getGyroAngle(), drivetrain.isGyroOnline());
            poseCalc.update();

            if(RobotController.getUserButton()) {
                arm.setCoastMode();
            }
            else {
                arm.setBrakeMode();
            }


            /* Update Other subsytems */
            ledController.update();
            pneumaticsControl.update();
            climber.update();
            telemetryUpdate();

            loopTiming.markLoopEnd();
        } catch(Throwable t) {
            CrashTracker.logThrowableCrash(t);
            throw t;
        }
    }
    
//////////////////////////////////////////////////////////////////////////
// Utilties
//////////////////////////////////////////////////////////////////////////
    public enum GamePiece {
        Nothing, Cargo, Hatch;
    }
    //final String[] gpOptions =    {GamePiece.Hatch.toString(), GamePiece.Cargo.toString(), GamePiece.Nothing.toString()};

    private void setMatchInitialCommands(){
        String gpStart = GamePiece.Hatch.toString();

        if(gpStart.compareTo(GamePiece.Cargo.toString())==0){
            intakeControl.setPositionCmd(IntakePos.Retract); //TODO -what should this be?
            pezControl.setPositionCmd(PEZPos.CargoGrab);
            superstructure.setInitialOpMode(OpMode.CargoCarry);
            arm.setPositionCmd(ArmPos.LowerCargo);
            //arm.setMatchStartPosition(); //todo, this should be something else
            pezControl.setInitCargo();
        } else if(gpStart.compareTo(GamePiece.Hatch.toString())==0){
            intakeControl.setPositionCmd(IntakePos.Retract);
            pezControl.setPositionCmd(PEZPos.HatchGrab);
            superstructure.setInitialOpMode(OpMode.Hatch);
            arm.setPositionCmd(ArmPos.LowerHatch);
            arm.setMatchStartPosition();
            pezControl.setInitHatch();
        } else {
            intakeControl.setPositionCmd(IntakePos.Retract);
            pezControl.setPositionCmd(PEZPos.HatchGrab);
            superstructure.setInitialOpMode(OpMode.Hatch);
            arm.setPositionCmd(ArmPos.LowerHatch);
            arm.setMatchStartPosition();
            pezControl.setInitHatch();
        }
    }

    private void telemetryUpdate(){
        double sampleTimeMs = loopTiming.getLoopStartTimeSec()*1000.0;

        /* Update main loop signals */
        rioDSSampLoadSig.addSample(sampleTimeMs, dataServer.getTotalStoredSamples());
        rioCurrDrawLoadSig.addSample(sampleTimeMs, pdp.getTotalCurrent());
        rioBattVoltLoadSig.addSample(sampleTimeMs, pdp.getVoltage());  
        rioDSLogQueueLenSig.addSample(sampleTimeMs, dataServer.logger.getSampleQueueLength());
        dtFwdRevAccelSig.addSample(sampleTimeMs, onboardAccel.getY());
        dtLeftRightAccelSig.addSample(sampleTimeMs, onboardAccel.getZ());
        dtUpDownAccelSig.addSample(sampleTimeMs, (onboardAccel.getX()*-1));
        intakeLeftCurrentSig.addSample(sampleTimeMs, pdp.getCurrent(RobotConstants.INTAKE_LEFT_MOTOR_PDP_PORT));
        intakeRightCurrentSig.addSample(sampleTimeMs, pdp.getCurrent(RobotConstants.INTAKE_LEFT_MOTOR_PDP_PORT));
        climberReleaseMotorCurrentSig.addSample(sampleTimeMs, pdp.getCurrent(RobotConstants.CLIMBER_RELEASE_MOTOR_PDP_PORT));
        rioIsBrownoutSig.addSample(sampleTimeMs, RobotController.isBrownedOut());
        matchTimeSig.addSample(sampleTimeMs, DriverStation.getInstance().getMatchTime());
        rioCANBusUsagePctSig.addSample(sampleTimeMs, RobotController.getCANStatus().percentBusUtilization);
    
        CasseroleDriverView.setDialValue("Main System Pressure", pneumaticsControl.getPressure());
        CasseroleDriverView.setDialValue("Speed", Math.abs(poseCalc.getRobotVelocity_ftpersec()));
        CasseroleDriverView.setDialValue("Arm Angle", arm.getActualArmHeight());
        CasseroleDriverView.setDialValue("Angle To Target", jevois.getTgtGeneralAngle()); 
        CasseroleDriverView.setBoolean("Gyro Offline", !drivetrain.isGyroOnline());
        CasseroleDriverView.setBoolean("Vision Camera Offline", !jevois.isVisionOnline());
        CasseroleDriverView.setBoolean("Vision Target Available", jevois.isTgtVisible());
        CasseroleDriverView.setBoolean("Auto Failed", autonomous.getAutoFailedLEDState());
        CasseroleDriverView.setBoolean("Line Seen", linefollow.isEstLinePosAvailable());
        CasseroleDriverView.setBoolean("Fault Detected", sensorCheck.isFaultDetected());
        CasseroleDriverView.setStringBox("Op Mode", superstructure.getOpModeString());
        CasseroleDriverView.setBoolean("Arm At Limit", arm.getBottomOfMotion() || arm.getTopOfMotion());
        CasseroleDriverView.setBoolean("Ball In Intake", intakeControl.isBallDetected());
        CasseroleDriverView.setStringBox("Fault Description", sensorCheck.getFaultDescription());
    }
        
    /**
     * This function sets up the driver view website
     */
    private void initDriverView(){
        //CasseroleDriverView.newAutoSelector("Starting Gamepiece", gpOptions);
        CasseroleDriverView.newDial("Main System Pressure", 0, 140, 10, 80, 125);
        CasseroleDriverView.newWebcam("cam1", RobotConstants.CAM_1_STREAM_URL, 50, 75, 270);
        CasseroleDriverView.newWebcam("cam2", RobotConstants.CAM_2_STREAM_URL, 25, 50, 90);
        CasseroleDriverView.newDial("Speed", 0, 20, 2,  1, 15);
        CasseroleDriverView.newDial("Arm Angle", -45, 225, 15,  -45, 110);
        CasseroleDriverView.newDial("Angle To Target", -90, 90, 15, -10, 10);
        CasseroleDriverView.newBoolean("Gyro Offline", "red");
        CasseroleDriverView.newBoolean("Vision Camera Offline", "red");
        CasseroleDriverView.newBoolean("Auto Failed", "red");
        CasseroleDriverView.newBoolean("Vision Target Available", "green");
        CasseroleDriverView.newBoolean("Line Seen", "green");
        CasseroleDriverView.newBoolean("Arm At Limit", "yellow");
        CasseroleDriverView.newBoolean("Fault Detected", "red");
        CasseroleDriverView.newBoolean("Ball In Intake", "green");
        CasseroleDriverView.newStringBox("Op Mode");
        CasseroleDriverView.newStringBox("Fault Description");
    }

    @Override
    public void testInit(){
        intakeControl.openLoop();
        arm.forceArmStop();
    }

    @Override
    public void testPeriodic(){
        
        //Cause arm to stop
        arm.setManualMovementCmd(0);
        
        loopTiming.markLoopStart();
        intakeControl.sampleSensors();
        pneumaticsControl.start();//ensure compressor is running

        pezControl.setPositionCmd(PEZPos.Neutralize);

        //Manual overrides for motors we can't turn by hand
        //climber.setManualMotorCommand(operatorController.xb.getY(Hand.kLeft));
        double intakeCmd =-1.0*operatorController.xb.getY(Hand.kRight);
        if(Math.abs(intakeCmd) < 0.15){
            intakeCmd = 0;
        }
        intakeControl.intakeLeftArmMotor.setManualMotorCommand(intakeCmd);
        intakeControl.intakeRightArmMotor.setManualMotorCommand(intakeCmd);
        intakeControl.updateTelemetry();

        climber.update();
        
        
        loopTiming.markLoopEnd();
    }
 
}