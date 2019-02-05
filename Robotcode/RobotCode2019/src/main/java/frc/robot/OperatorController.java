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
import frc.robot.Arm.ArmPos;
import frc.robot.IntakeControl.IntakePos;
import frc.robot.IntakeControl.IntakeSpd;
import frc.robot.Superstructure.OpMode;

public class OperatorController {

    /* The USB controller itself */
    XboxController xb;

    /* Operator commands state*/
    boolean releaseReq;
    boolean grabReq;
    ArmPosCmd armPosReq;
    double  armManualPosCmd;
    IntakeSpd intakeSpdReq;
    IntakePos intakePosReq;
    boolean climberReleaseReq;
    boolean climberRelEnable;

    /* Auto Alignment Related Variables */
    boolean   autoMove;
    boolean autoAlignHighReq;
    boolean autoAlignMidReq;
    boolean autoAlignLowReq;

    /* Behavior/feel calibrations */
    Calibration joystickExpScaleFactor;
    Calibration joystickDeadzone;

    /* Telemetry */
    Signal gamepieceGrabReqSig;
    Signal gamepieceReleaseReqSig;
    Signal armPosReqSig;
    Signal armManualPosCmdSig;
    Signal autoAlignHighReqSig;
    Signal autoAlignMidReqSig;
    Signal autoAlignLowReqSig;
    Signal intakeSpdReqSig;
    Signal intakePosReqSig;
    Signal climberEnableReqSig;
    Signal climberReleaseReqSig;
    

    /* Singleton stuff */
    private static OperatorController opCtrl = null;
    public static synchronized OperatorController getInstance() {
        if(opCtrl == null)
            opCtrl = new OperatorController();
        return opCtrl;
    }

    public enum ArmPosCmd {
        Top(4),
        Middle(3),
        Lower(2),
        IntakeHatch(1),
        IntakeCargo(0),
        None(-1);

        public final int value;

        private ArmPosCmd(int value) {
            this.value = value;
        }
                
        public int toInt(){
            return this.value;
        }
    }

    private OperatorController(){
        xb = new XboxController(RobotConstants.OPERATOR_CONTROLLER_USB_IDX);

        joystickExpScaleFactor = new Calibration("Operator Joystick Exponential Scale Factor", 3.0 , 1, 10);
        joystickDeadzone = new Calibration("Operator Joystick Deadzone ", 0.15, 0, 1);

        gamepieceGrabReqSig = new Signal("Operator Gamepiece Grab Command", "bool");
        gamepieceReleaseReqSig = new Signal("Operator Gamepiece Release Command", "bool");
        armPosReqSig = new Signal("Operator Arm Position Command", "Arm Pos Enum");
        armManualPosCmdSig = new Signal("Operator Manual Arm Position Command", "cmd");
        autoAlignHighReqSig = new Signal("Operator Auto Align Top Command", "bool");
        autoAlignMidReqSig = new Signal("Operator Auto Align Mid Command", "bool");
        autoAlignLowReqSig = new Signal("Operator Auto Align Low Command", "bool");
        intakeSpdReqSig = new Signal("Operator Intake Speed Command", "speed enum");
        intakePosReqSig = new Signal("Operator Intake Position Command", "pos enum");
        climberEnableReqSig = new Signal("Operator Climber Release Enable Command", "bool");
        climberReleaseReqSig = new Signal("Operator Climber Release Command", "bool");
    }


    public void update(){
        //Init requests
        armPosReq = ArmPosCmd.None;
        autoAlignHighReq = false;
        autoAlignMidReq = false;
        autoAlignLowReq = false;
        autoMove = false;

        //Get Gamepiece Release/Grab request
        grabReq = xb.getBumper(Hand.kLeft);
        releaseReq = xb.getBumper(Hand.kRight);

        //Get arm or auto-align requests
        int povAngle = xb.getPOV(0);
        if(xb.getXButton()){
            if(povAngle == 0){
                autoAlignHighReq = true;
                autoMove = true;
            } else if(povAngle == 90 || povAngle == 270) {
                autoAlignMidReq = true;
                autoMove = true;
            } else if(povAngle == 180) {
                autoAlignLowReq = true;
                autoMove = true;
            }
        } else {
            if(povAngle == 0){
                armPosReq = ArmPosCmd.Top;
            } else if(povAngle == 90 || povAngle == 270) {
                armPosReq = ArmPosCmd.Middle;
            } else if(povAngle == 180) {
                armPosReq = ArmPosCmd.Lower;
            } else if(xb.getYButton()) {
                armPosReq = ArmPosCmd.IntakeHatch;
            } else if(xb.getAButton()) {
                armPosReq = ArmPosCmd.IntakeCargo;
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

        climberRelEnable = xb.getBackButton();
        climberReleaseReq = ( Math.abs(xb.getY(Hand.kRight)) > 0.5);

        /* Update Telemetry */
        double sample_time_ms = LoopTiming.getInstance().getLoopStartTimeSec()*1000.0;
        gamepieceGrabReqSig.addSample(sample_time_ms,grabReq);
        gamepieceReleaseReqSig.addSample(sample_time_ms,releaseReq);
        armPosReqSig.addSample(sample_time_ms,armPosReq.toInt());
        armManualPosCmdSig.addSample(sample_time_ms,armManualPosCmd);
        autoAlignHighReqSig.addSample(sample_time_ms,autoAlignHighReq);
        autoAlignMidReqSig.addSample(sample_time_ms,autoAlignMidReq);
        autoAlignLowReqSig.addSample(sample_time_ms,autoAlignLowReq);
        intakeSpdReqSig.addSample(sample_time_ms, intakeSpdReq.toInt());
        intakePosReqSig.addSample(sample_time_ms, intakePosReq.toInt());
        climberEnableReqSig.addSample(sample_time_ms, climberRelEnable);
        climberReleaseReqSig.addSample(sample_time_ms, climberReleaseReq);
    }

    public boolean getGampieceGrabRequest() {
        return this.grabReq;
    }

    public boolean getGampieceReleaseRequest() {
        return this.releaseReq;
    }

    public ArmPosCmd getArmPosReq() {
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

    public boolean getClimberEnable() {
        return this.climberRelEnable;
    }

    public boolean getClimberReleace() {
        return this.climberReleaseReq;
    }
}