package frc.robot;

import edu.wpi.first.wpilibj.XboxController;
import edu.wpi.first.wpilibj.GenericHID.Hand;
import frc.robot.IntakeControl.IntakePos;
import frc.robot.IntakeControl.IntakeSpd;

public class DriverController {

    XboxController xb;

    double driverFwdRevCmd;
    double driverRotateCmd;
    boolean slowMoveReq;
    boolean compressorDisableReq;
    boolean compressorEnableReq;

    IntakeSpd intakeSpdReq;
    IntakePos intakePosReq;

    /* Singleton stuff */
    private static DriverController drvCtrl = null;
    public static synchronized DriverController getInstance() {
        if(drvCtrl == null) drvCtrl = new DriverController();
        return drvCtrl;
    }

    private DriverController(){
        xb = new XboxController(RobotConstants.DRIVER_CONTROLLER_USB_IDX);
    }

    public void update(){

        if(xb.getBumper(Hand.kRight)){
            intakePosReq = IntakePos.Extend;
        } else {
            intakePosReq = IntakePos.Retract;
        }

        if(xb.getTriggerAxis(Hand.kRight) > 0.5){
            intakeSpdReq = IntakeSpd.Intake;
            //When pulling a ball in, override the intake to be extended.
            intakePosReq = IntakePos.Extend;
        } else if(xb.getTriggerAxis(Hand.kLeft) > 0.5){
            intakeSpdReq = IntakeSpd.Eject;
        } else {
            intakeSpdReq = IntakeSpd.Stop;
        }
    }

    public IntakePos getIntakePosReq() {
        return this.intakePosReq;
    }

    public double getDriverFwdRevCmd() {
        return this.driverFwdRevCmd;
    }

    public double getDriverRotateCmd() {
        return this.driverRotateCmd;
    }

    public boolean getSlowMoveReq() {
        return this.slowMoveReq;
    }

    public boolean isSlowMoveReq() {
        return this.slowMoveReq;
    }

    public boolean getCompressorDisableReq() {
        return this.compressorDisableReq;
    }

    public boolean isCompressorDisableReq() {
        return this.compressorDisableReq;
    }

    public boolean getCompressorEnableReq() {
        return this.compressorEnableReq;
    }

    public boolean isCompressorEnableReq() {
        return this.compressorEnableReq;
    }

}
