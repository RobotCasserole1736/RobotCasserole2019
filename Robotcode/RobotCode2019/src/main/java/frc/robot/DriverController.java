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
import frc.lib.SignalMath.AveragingFilter;

public class DriverController {

    public XboxController xb;

    /* Driver input command state */
    double driverFwdRevCmd;
    double driverRotateCmd;
    double invertFactor = 1;
    boolean slowMoveReq;
    boolean gyroAngleLockReq;
    boolean compressorDisableReq;
    boolean compressorEnableReq;
    boolean LockDrivetrainAngle = false;
    boolean autoMove;
    boolean autoAlignHatchPickupReq;
    boolean autoAlignMidPlaceReq;
    boolean autoAlignLowPlaceReq;

    /* Behavior/performance calibrations */
    Calibration slowMoveFwdRevScaleFactor;
    Calibration slowMoveRotateScaleFactor;
    Calibration joystickExpScaleFactor;
    Calibration joystickDeadzone;
    Calibration joystickUpperDeadzone;
  
    AveragingFilter filt;

    /* Telemetry */
    Signal driverFwdRevCmdSig;
    Signal driverRotateCmdSig;
    Signal slowMoveReqSig;
    Signal gyroAngleLockReqSig;
    Signal compressorDisableReqSig;
    Signal compressorEnableReqSig;
    Signal autoAlignHatchPickupReqSig;
    Signal autoAlignMidPlaceReqSig;
    Signal autoAlignLowPlaceReqSig;

    /* Singleton stuff */
    private static DriverController drvCtrl = null;
    public static synchronized DriverController getInstance() {
        if(drvCtrl == null) drvCtrl = new DriverController();
        return drvCtrl;
    }

    /** Constructor */
    private DriverController(){
        filt = new AveragingFilter(2,0);
        xb = new XboxController(RobotConstants.DRIVER_CONTROLLER_USB_IDX);
        slowMoveFwdRevScaleFactor = new Calibration("Driver Fwd-Rev Slow Move Scale Factor",  0.30, 0, 1);
        slowMoveRotateScaleFactor = new Calibration("Driver Rotation Slow Move Scale Factor", 0.5, 0, 1);
        joystickExpScaleFactor = new Calibration("Driver Joystick Exponential Scale Factor", 3.0 , 1, 10);
        joystickDeadzone = new Calibration("Driver Joystick Deadzone ", 0.15, 0, 1);
        joystickUpperDeadzone = new Calibration("Upper Deadzone of Joystick",0.95);

        driverFwdRevCmdSig = new Signal("Driver Fwd-Rev Command", "cmd");
        driverRotateCmdSig = new Signal("Driver Rotate Command", "cmd");
        slowMoveReqSig = new Signal("Driver SlowMove Command", "bool");
        gyroAngleLockReqSig = new Signal("Driver Gyro Angle Lock Command", "bool");
        compressorDisableReqSig = new Signal("Driver Compressor Disable Command", "bool");
        compressorEnableReqSig = new Signal("Driver Compressor Enable Command", "bool");
        autoAlignHatchPickupReqSig = new Signal("Driver Auto Align Hatch Pickup Command", "bool");
        autoAlignMidPlaceReqSig = new Signal("Driver Auto Align Mid Place Command", "bool");
        autoAlignLowPlaceReqSig = new Signal("Driver Auto Align Low Place Command", "bool");
    }

    /** Main update function */
    public void update(){

        autoAlignHatchPickupReq = false;
        autoAlignMidPlaceReq = false;
        autoAlignLowPlaceReq = false;
        autoMove = false;

        int povAngle = xb.getPOV(0);
        if(povAngle == 0){
            autoAlignHatchPickupReq = true;
            autoMove = true;
        } else if(povAngle == 90 || povAngle == 270) {
            autoAlignMidPlaceReq = true;
            autoMove = true;
        } else if(povAngle == 180) {
            autoAlignLowPlaceReq = true;
            autoMove = true;
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

        //UpperDeadzone logic
        double frCmd=xb.getY(Hand.kLeft)/joystickUpperDeadzone.get();
        double rCmd=xb.getX(Hand.kRight)/joystickUpperDeadzone.get();

        if(frCmd>1){
            frCmd=1;
        }else if(frCmd<-1){
            frCmd=-1;
        }

        if(rCmd>1){
            rCmd=1;
        }else if(rCmd<-1){
            rCmd=-1;
        }
        
        driverFwdRevCmd = Utils.ctrlAxisScale(-1*frCmd,  joystickExpScaleFactor.get(), joystickDeadzone.get());
        driverRotateCmd = Utils.ctrlAxisScale(   rCmd, joystickExpScaleFactor.get(), joystickDeadzone.get());

        if(xb.getBumper(Hand.kRight)){
            slowMoveReq = true;
        } else {
            slowMoveReq = false;
        }

        if(slowMoveReq){
            driverFwdRevCmd *= slowMoveFwdRevScaleFactor.get();
            driverRotateCmd *= slowMoveRotateScaleFactor.get();
        }

        //code to Parkify turning
        //driverRotateCmd = filt.filter(driverRotateCmd);

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

        if(xb.getBButton()){
            LockDrivetrainAngle = true;
        } else {
            LockDrivetrainAngle = false;
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
        autoAlignHatchPickupReqSig.addSample(sample_time_ms,autoAlignHatchPickupReq);
        autoAlignMidPlaceReqSig.addSample(sample_time_ms,autoAlignMidPlaceReq);
        autoAlignLowPlaceReqSig.addSample(sample_time_ms,autoAlignLowPlaceReq);
    }

    /* Getters for getting driver commands */

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

    public boolean getVisionSimpleAlignRequest() {
        return this.LockDrivetrainAngle;
    }

    public boolean getAutoAlignHatchPickupReq() {
        return this.autoAlignHatchPickupReq;
    }

    public boolean getAutoAlignMidPlaceReq() {
        return this.autoAlignMidPlaceReq;
    }

    public boolean getAutoAlignLowPlaceReq() {
        return this.autoAlignLowPlaceReq;
    }

    public boolean getAutoMove() {
        return this.autoMove;
    }

}
