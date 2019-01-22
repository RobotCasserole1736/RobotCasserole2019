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

import edu.wpi.first.wpilibj.XboxController;
import edu.wpi.first.wpilibj.GenericHID.Hand;
import frc.lib.Calibration.Calibration;
import frc.lib.DataServer.Signal;
import frc.robot.IntakeControl.IntakePos;
import frc.robot.IntakeControl.IntakeSpd;

public class DriverController {

    XboxController xb;

    /* Driver input command state */
    double driverFwdRevCmd;
    double driverRotateCmd;
    double invertFactor = 1;
    boolean slowMoveReq;
    boolean gyroAngleLockReq;
    boolean compressorDisableReq;
    boolean compressorEnableReq;
    IntakeSpd intakeSpdReq;
    IntakePos intakePosReq;

    /* Behavior/performance calibrations */
    Calibration slowMoveFwdRevScaleFactor;
    Calibration slowMoveRotateScaleFactor;
    Calibration joystickExpScaleFactor;
    Calibration joystickDeadzone;
  
    /* Telemetry */
    Signal driverFwdRevCmdSig;
    Signal driverRotateCmdSig;
    Signal slowMoveReqSig;
    Signal gyroAngleLockReqSig;
    Signal compressorDisableReqSig;
    Signal compressorEnableReqSig;
    Signal intakeSpdReqSig;
    Signal intakePosReqSig;

    /* Singleton stuff */
    private static DriverController drvCtrl = null;
    public static synchronized DriverController getInstance() {
        if(drvCtrl == null) drvCtrl = new DriverController();
        return drvCtrl;
    }

    /** Constructor */
    private DriverController(){
        xb = new XboxController(RobotConstants.DRIVER_CONTROLLER_USB_IDX);
        slowMoveFwdRevScaleFactor = new Calibration("Driver Fwd-Rev Slow Move Scale Factor",  0.25, 0, 1);
        slowMoveRotateScaleFactor = new Calibration("Driver Rotation Slow Move Scale Factor", 0.25, 0, 1);
        joystickExpScaleFactor = new Calibration("Driver Joystick Exponential Scale Factor", 3.0 , 1, 10);
        joystickDeadzone = new Calibration("Driver Joystick Deadzone ", 0.15, 0, 1);

        driverFwdRevCmdSig = new Signal("Driver Fwd-Rev Command", "cmd");
        driverRotateCmdSig = new Signal("Driver Rotate Command", "cmd");
        slowMoveReqSig = new Signal("Driver SlowMove Command", "bool");
        gyroAngleLockReqSig = new Signal("Driver Gyro Angle Lock Command", "bool");
        compressorDisableReqSig = new Signal("Driver Compressor Disable Command", "bool");
        compressorEnableReqSig = new Signal("Driver Compressor Enable Command", "bool");
        intakeSpdReqSig = new Signal("Driver Intake Speed Command", "speed enum");
        intakePosReqSig = new Signal("Driver Intake Position Command", "pos enum");
    }

    /** Main update function */
    public void update(){

        if(xb.getBumper(Hand.kRight)){
            intakePosReq = IntakePos.Extend;
        } else {
            intakePosReq = IntakePos.Retract;
        }

        if(xb.getTriggerAxis(Hand.kRight) > 0.5){
            //When pulling a ball in, override the intake to be extended.
            intakePosReq = IntakePos.Extend;
            intakeSpdReq = IntakeSpd.Intake;
        } else if(xb.getTriggerAxis(Hand.kLeft) > 0.5){
            //Same deal as pulling a ball in
            intakePosReq = IntakePos.Extend;
            intakeSpdReq = IntakeSpd.Eject;
        } else {
            intakeSpdReq = IntakeSpd.Stop;
            //If not pressing a trigger or right bumper, override the intake to be retracted
            intakePosReq = IntakePos.Retract;
        }

        if(xb.getStartButton()){
            compressorEnableReq = true;
            compressorDisableReq = false;
        } else if(xb.getBackButton()) {
            compressorEnableReq = false;
            compressorDisableReq = true;
        } else {
            compressorEnableReq = false;
            compressorDisableReq = false;
        }

        driverFwdRevCmd = Utils.ctrlAxisScale(-1*xb.getY(Hand.kLeft),  joystickExpScaleFactor.get(), joystickDeadzone.get());
        driverRotateCmd = Utils.ctrlAxisScale(   xb.getX(Hand.kRight), joystickExpScaleFactor.get(), joystickDeadzone.get());

        if(xb.getBumper(Hand.kRight)){
            slowMoveReq = true;
        } else {
            slowMoveReq = false;
        }

        if(slowMoveReq){
            driverFwdRevCmd *= slowMoveFwdRevScaleFactor.get();
            driverRotateCmd *= slowMoveRotateScaleFactor.get();
        }

        if(xb.getAButton()){
            gyroAngleLockReq = true;
        } else {
            gyroAngleLockReq = false;
        }

        if(xb.getBumper(Hand.kLeft)){
            invertFactor = -1;
        } else {
            invertFactor = 1;
        } 

        //If we want to drive backward, invert the command.
        driverFwdRevCmd *= invertFactor;
        driverRotateCmd *= invertFactor;

        /*Update Telemetry */
        double sample_time_ms = LoopTiming.getInstance().getLoopStartTimeSec()*1000.0;
        driverFwdRevCmdSig.addSample(sample_time_ms, driverFwdRevCmd);
        driverRotateCmdSig.addSample(sample_time_ms, driverRotateCmd);
        slowMoveReqSig.addSample(sample_time_ms, slowMoveReq);
        gyroAngleLockReqSig.addSample(sample_time_ms, gyroAngleLockReq);
        compressorDisableReqSig.addSample(sample_time_ms, compressorDisableReq);
        compressorEnableReqSig.addSample(sample_time_ms, compressorEnableReq);
        intakeSpdReqSig.addSample(sample_time_ms, intakeSpdReq.toInt());
        intakePosReqSig.addSample(sample_time_ms, intakePosReq.toInt());
    }

    /* Getters for getting driver commands */

    public IntakePos getIntakePosReq() {
        return this.intakePosReq;
    }

    public IntakeSpd getIntakeSpdReq() {
        return this.intakeSpdReq;
    }

    public double getDriverFwdRevCmd() {
        return this.driverFwdRevCmd;
    }

    public double getDriverRotateCmd() {
        return this.driverRotateCmd;
    }

    public boolean getCompressorDisableReq() {
        return this.compressorDisableReq;
    }

    public boolean getCompressorEnableReq() {
        return this.compressorEnableReq;
    }

    public boolean getGyroAngleLockReq() {
        return this.gyroAngleLockReq;
    }

}
