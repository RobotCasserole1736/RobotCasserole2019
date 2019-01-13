package frc.robot;

import edu.wpi.first.wpilibj.XboxController;
import edu.wpi.first.wpilibj.GenericHID.Hand;
import frc.lib.DataServer.Signal;
import frc.robot.IntakeControl.IntakePos;
import frc.robot.IntakeControl.IntakeSpd;

public class OperatorController {

    XboxController xb;

    boolean ballPickupReq;
    boolean hatchPickupReq;
    boolean releaseReq;

    boolean armTopPosReq;
    boolean armMidPosReq;
    boolean armLowPosReq;

    double armManualPosCmd;

    boolean autoAlignHighReq;
    boolean autoAlignMidReq;
    boolean autoAlignLowReq;

    IntakeSpd intakeSpdReq;
    IntakePos intakePosReq;

    /* Telemetry */
    Signal intakeSpdReqSig;
    Signal intakePosReqSig;
    

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
        ballPickupReq = xb.getAButton();
        hatchPickupReq = xb.getYButton();
        releaseReq = xb.getBButton();

        armTopPosReq = false;
        armMidPosReq = false;
        armLowPosReq = false;
        autoAlignHighReq = false;
        autoAlignMidReq = false;
        autoAlignLowReq = false;
        int povAngle = xb.getPOV(0);
        if(xb.getXButton()){
            if(povAngle == 0){
                autoAlignHighReq = true;
            } else if(povAngle == 90 || povAngle == 270) {
                autoAlignMidReq = true;
            } else if(povAngle == 180) {
                autoAlignLowReq = true;
            }
        } else {
            if(povAngle == 0){
                armTopPosReq = true;
            } else if(povAngle == 90 || povAngle == 270) {
                armMidPosReq = true;
            } else if(povAngle == 180) {
                armLowPosReq = true;
            }
        }

        armManualPosCmd = Utils.ctrlAxisScale(xb.getY(Hand.kLeft), 3, 0.15)

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

        double sample_time_ms = LoopTiming.getInstance().getLoopStartTime_sec()*1000.0;
        intakeSpdReqSig.addSample(sample_time_ms, intakeSpdReq.toInt());
        intakePosReqSig.addSample(sample_time_ms, intakePosReq.toInt());

    }



    public boolean getBallPickupReq() {
        return this.ballPickupReq;
    }

    public boolean getHatchPickupReq() {
        return this.hatchPickupReq;
    }

    public boolean getReleaseReq() {
        return this.releaseReq;
    }

    public boolean getArmTopPosReq() {
        return this.armTopPosReq;
    }

    public boolean getArmMidPosReq() {
        return this.armMidPosReq;
    }

    public boolean getArmLowPosReq() {
        return this.armLowPosReq;
    }

    public double getArmManualPosCmd() {
        return this.armManualPosCmd;
    }

    public boolean getAutoAlignHighReq() {
        return this.autoAlignHighReq;
    }

    public boolean getAutoAlignMidReq() {
        return this.autoAlignMidReq;
    }

    public boolean getAutoAlignLowReq() {
        return this.autoAlignLowReq;
    }

    public IntakeSpd getIntakeIntakeReq() {
        return this.intakeSpdReq;
    }

    public IntakePos getIntakePosReq() {
        return this.intakePosReq;
    }
    
}