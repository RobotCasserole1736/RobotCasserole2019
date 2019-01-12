/*----------------------------------------------------------------------------*/
/* Copyright (c) 2017-2018 FIRST. All Rights Reserved.                        */
/* Open Source Software - may be modified and shared by FRC teams. The code   */
/* must be accompanied by the FIRST BSD license file in the root directory of */
/* the project.                                                               */
/*----------------------------------------------------------------------------*/

package frc.robot;

import edu.wpi.first.wpilibj.PowerDistributionPanel;
import edu.wpi.first.wpilibj.TimedRobot;
import edu.wpi.first.wpilibj.Timer;
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

  //Website Utilities
  CasseroleWebServer webserver;
  CalWrangler wrangler;

  //Processor metric utilities
  CasseroleRIOLoadMonitor loadMon;

  //Physical devices
  PowerDistributionPanel pdp;

  PneumaticsControl pneumatics;
  Drivetrain drivetrain;

  //Top level telemetry signals
  Signal rioCPULoad;
  Signal rioMemLoad;
  Signal pneumaticsPressure;
  
  /**
   * This function is run when the robot is first started up and should be
   * used for any initialization code.
   */
  @Override
  public void robotInit() {
    /* Init website utilties */
    webserver = new CasseroleWebServer();
    wrangler = new CalWrangler();

    pneumatics = PneumaticsControl.getInstance();
    drivetrain = Drivetrain.getInstance();

    /* Init Robot parts */
    pdp = new PowerDistributionPanel();

    /* Init input from humans */
    OperatorController.getInstance();
    DriverController.getInstance();

    /* Init software utilities */
    loadMon= new CasseroleRIOLoadMonitor();
    LoopTiming.getInstance();

    /* Init local telemetry signals */
    rioCPULoad = new Signal("roboRIO CPU Load", "Pct");
    rioMemLoad = new Signal("roboRIO Memory Load", "Pct"); 
    pneumaticsPressure = new Signal("Main system pressure", "Psi");

    /* Website setup */
    initDriverView();

    /* Fire up webserver & telemetry dataserver */
    webserver.startServer();
    CasseroleDataServer.getInstance().startServer();

  }

  /**
   * This function is called once right before the start of disabled mode.
   */
  @Override
  public void disabledInit() {
    CasseroleDataServer.getInstance().logger.stopLogging();
    drivetrain.update();

  }

  /**
   * This function is called periodically during disabled mode.
   */
  @Override
  public void disabledPeriodic() {

    telemetryUpdate();
  }

  @Override
  public void teleopInit() {
    CasseroleDataServer.getInstance().logger.startLoggingTeleop();


  }

  /**
   * This function is called periodically during operator control.
   */
  @Override
  public void teleopPeriodic() {
    LoopTiming.getInstance().markLoopStart();

    drivetrain.update();
    telemetryUpdate();
    
    LoopTiming.getInstance().markLoopEnd();
  }

  @Override
  public void autonomousInit() {
    CasseroleDataServer.getInstance().logger.startLoggingAuto();


  }

  /**
   * This function is called periodically during autonomous.
   */
  @Override
  public void autonomousPeriodic() {

    telemetryUpdate();
  }

  
  //////////////////////////////////////////////////////////////////////////
  // Utilties
  //////////////////////////////////////////////////////////////////////////
  private void telemetryUpdate(){
    double sample_time_ms = LoopTiming.getInstance().getLoopStartTime_sec()*1000.0;

    rioCPULoad.addSample(sample_time_ms,loadMon.getCPULoadPct());
    rioMemLoad.addSample(sample_time_ms,loadMon.getMemLoadPct());
    pneumaticsPressure.addSample(sample_time_ms,PneumaticsControl.getInstance().GetPressure());
    CasseroleDriverView.setDialValue("Main System Pressure", PneumaticsControl.getInstance().GetPressure());
  }
    
  /**
   * This function sets up the driver view website
   */
  private void initDriverView(){
    String[] gpOptions =  {"Cargo (ball)", "Hatch Panel", "Nothing"};
    CasseroleDriverView.newAutoSelector("Starting Gamepiece", gpOptions);
    CasseroleDriverView.newDial("Main System Pressure", 0, 140, 10, 80, 125);
    CasseroleDriverView.newWebcam("cam1", "http://10.17.36.10:1181/stream.mjpg", 0, 0, 0);
    CasseroleDriverView.newWebcam("cam2", "http://10.17.36.10:1182/stream.mjpg", 0, 0, 0);
  }
}
