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
import frc.robot.Arm.ArmPosReq;
import frc.robot.IntakeControl.IntakePos;
import frc.robot.IntakeControl.IntakeSpd;

public class OperatorController {

    /* The USB controller itself */
    XboxController xb;

    /* Operator commands state*/
    boolean   ballPickupReq;
    boolean   hatchPickupReq;
    boolean   releaseReq;
    ArmPosReq armPosReq;
    double    armManualPosCmd;
    boolean   autoAlignHighReq;
    boolean   autoAlignMidReq;
    boolean   autoAlignLowReq;
    IntakeSpd intakeSpdReq;
    IntakePos intakePosReq;

    /* State Machine Related Variables */
    boolean   topLevelPlace;
    boolean   midLevelPlace;
    boolean   lowLevelPlace;
    boolean   autoMove;

    /* Behavior/feel calibrations */
    Calibration joystickExpScaleFactor;
    Calibration joystickDeadzone;

    /* Telemetry */
    Signal ballPickupReqSig;
    Signal hatchPickupReqSig;
    Signal releaseReqSig;
    Signal armPosReqSig;
    Signal armManualPosCmdSig;
    Signal autoAlignHighReqSig;
    Signal autoAlignMidReqSig;
    Signal autoAlignLowReqSig;
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

        joystickExpScaleFactor    = new Calibration("Operator Joystick Exponential Scale Factor", 3.0 , 1, 10);
        joystickDeadzone          = new Calibration("Operator Joystick Deadzone ", 0.15, 0, 1);

        ballPickupReqSig = new Signal("Operator Ball Pickup Command", "bool");
        hatchPickupReqSig = new Signal("Operator Hatch Pickup Command", "bool");
        releaseReqSig = new Signal("Operator Gamepiece Release Command", "bool");
        armPosReqSig = new Signal("Operator Arm Position Command", "Arm Pos Enum");
        armManualPosCmdSig = new Signal("Operator Manual Arm Position Command", "cmd");
        autoAlignHighReqSig = new Signal("Operator Auto Align Top Command", "bool");
        autoAlignMidReqSig = new Signal("Operator Auto Align Mid Command", "bool");
        autoAlignLowReqSig = new Signal("Operator Auto Align Low Command", "bool");
        intakeSpdReqSig = new Signal("Operator Intake Speed Command", "speed enum");
        intakePosReqSig = new Signal("Operator Intake Position Command", "pos enum");
        
    }


    public void update(){
        ballPickupReq  = xb.getAButton();
        hatchPickupReq = xb.getYButton();
        releaseReq     = xb.getBButton();

        armPosReq = ArmPosReq.None;
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
                armPosReq = ArmPosReq.Top;
            } else if(povAngle == 90 || povAngle == 270) {
                armPosReq = ArmPosReq.Middle;
            } else if(povAngle == 180) {
                armPosReq = ArmPosReq.Lower;
            }
        }

        armManualPosCmd = Utils.ctrlAxisScale(-1*xb.getY(Hand.kLeft), joystickExpScaleFactor.get(), joystickDeadzone.get());

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

        //code for Third level placement. 0 references the top button of the Dpad on the Xbox controller
        if(xb.getPOV() == 0 && xb.getXButton()){
            topLevelPlace = true;
            autoMove = true;
        }
        else {
            topLevelPlace = false;
            autoMove = false;
        }

        //code for Second level placement. 90 and 270 are referencing the sides of the Dpad on the Xbox controller
        //90 is the right while 270 is the left
        if((xb.getPOV() == 90 || xb.getPOV() == 270) && xb.getXButton()){
            midLevelPlace = true;
            autoMove = true;
        }
        else {
            midLevelPlace = false;
            autoMove = false;
        }

        //code for first level placement. 180 references the bottom button of the Dpad on the Xbox controller
        if(xb.getPOV() == 180 && xb.getXButton()){
            lowLevelPlace = true;
            autoMove = true;
        }
        else {
            lowLevelPlace = false;
            autoMove = false;
        }

        /* Update Telemetry */
        double sample_time_ms = LoopTiming.getInstance().getLoopStartTime_sec()*1000.0;
        ballPickupReqSig.addSample(sample_time_ms,ballPickupReq);
        hatchPickupReqSig.addSample(sample_time_ms,hatchPickupReq);
        releaseReqSig.addSample(sample_time_ms,releaseReq);
        armPosReqSig.addSample(sample_time_ms,armPosReq.toInt());
        armManualPosCmdSig.addSample(sample_time_ms,armManualPosCmd);
        autoAlignHighReqSig.addSample(sample_time_ms,autoAlignHighReq);
        autoAlignMidReqSig.addSample(sample_time_ms,autoAlignMidReq);
        autoAlignLowReqSig.addSample(sample_time_ms,autoAlignLowReq);
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

    public ArmPosReq getArmPosReq() {
        return this.armPosReq;
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

    public IntakeSpd getIntakeSpdReq() {
        return this.intakeSpdReq;
    }

    public IntakePos getIntakePosReq() {
        return this.intakePosReq;
    }

    public boolean getAutoMove() {
        return this.autoMove;
    }

    public boolean getLowLevelPlace() {
        return this.lowLevelPlace;
    }

    public boolean getMidLevelPlace() {
        return this.midLevelPlace;
    }

    public boolean getTopLevelPlace() {
        return this.topLevelPlace;
    }
    
}