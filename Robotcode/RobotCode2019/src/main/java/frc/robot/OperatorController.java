package frc.robot;

import edu.wpi.first.wpilibj.XboxController;

public class OperatorController {

    XboxController xb;

    boolean ballPickupReq;
    boolean hatchPickupReq;
    boolean releaseReq;

    boolean armTopPosReq;
    boolean armMidPosReq;
    boolean armBotPosReq;

    double armManualPosCmd;

    boolean autoAlignHighReq;
    boolean autoAlignMidReq;
    boolean autoAlignLowReq;

    boolean intakeIntakeReq;
    boolean intakeEjectReq;
    boolean intakeExtendReq;
    boolean intakeRetractReq;

    /* Singleton stuff */
    private static OperatorController opCtrl = null;
	public static synchronized OperatorController getInstance() {
		if(opCtrl == null) opCtrl = new OperatorController();
		return opCtrl;
	}

    private OperatorController(){
        xb = new XboxController(RobotConstants.OPERATOR_CONTROLLER_USB_IDX);
    }


    public void update(){

    }



    public boolean getBallPickupReq() {
        return this.ballPickupReq;
    }

    public boolean isBallPickupReq() {
        return this.ballPickupReq;
    }

    public boolean getHatchPickupReq() {
        return this.hatchPickupReq;
    }

    public boolean isHatchPickupReq() {
        return this.hatchPickupReq;
    }

    public boolean getReleaseReq() {
        return this.releaseReq;
    }

    public boolean isReleaseReq() {
        return this.releaseReq;
    }

    public boolean getArmTopPosReq() {
        return this.armTopPosReq;
    }

    public boolean isArmTopPosReq() {
        return this.armTopPosReq;
    }

    public boolean getArmMidPosReq() {
        return this.armMidPosReq;
    }

    public boolean isArmMidPosReq() {
        return this.armMidPosReq;
    }

    public boolean getArmBotPosReq() {
        return this.armBotPosReq;
    }

    public boolean isArmBotPosReq() {
        return this.armBotPosReq;
    }

    public double getArmManualPosCmd() {
        return this.armManualPosCmd;
    }

    public boolean getAutoAlignHighReq() {
        return this.autoAlignHighReq;
    }

    public boolean isAutoAlignHighReq() {
        return this.autoAlignHighReq;
    }

    public boolean getAutoAlignMidReq() {
        return this.autoAlignMidReq;
    }

    public boolean isAutoAlignMidReq() {
        return this.autoAlignMidReq;
    }

    public boolean getAutoAlignLowReq() {
        return this.autoAlignLowReq;
    }

    public boolean isAutoAlignLowReq() {
        return this.autoAlignLowReq;
    }

    public boolean getIntakeIntakeReq() {
        return this.intakeIntakeReq;
    }

    public boolean isIntakeIntakeReq() {
        return this.intakeIntakeReq;
    }

    public boolean getIntakeEjectReq() {
        return this.intakeEjectReq;
    }

    public boolean isIntakeEjectReq() {
        return this.intakeEjectReq;
    }

    public boolean getIntakeExtendReq() {
        return this.intakeExtendReq;
    }

    public boolean isIntakeExtendReq() {
        return this.intakeExtendReq;
    }

    public boolean getIntakeRetractReq() {
        return this.intakeRetractReq;
    }

    public boolean isIntakeRetractReq() {
        return this.intakeRetractReq;
    }
    
}