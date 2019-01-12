package frc.robot;

import edu.wpi.first.wpilibj.XboxController;

public class DriverController {

    XboxController xb;

    double driverFwdRevCmd;
    double driverRotateCmd;
    boolean slowMoveReq;
    boolean compressorDisableReq;
    boolean compressorEnableReq;

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
