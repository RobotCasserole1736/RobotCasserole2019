/*----------------------------------------------------------------------------*/
/* Copyright (c) 2018 FIRST. All Rights Reserved.                             */
/* Open Source Software - may be modified and shared by FRC teams. The code   */
/* must be accompanied by the FIRST BSD license file in the root directory of */
/* the project.                                                               */
/*----------------------------------------------------------------------------*/

package frc.robot;

import frc.lib.Calibration.*;
import frc.robot.RobotConstants;
import edu.wpi.first.wpilibj.DigitalInput;
import edu.wpi.first.wpilibj.DoubleSolenoid;
import edu.wpi.first.wpilibj.PowerDistributionPanel;
import edu.wpi.first.wpilibj.Solenoid;
import edu.wpi.first.wpilibj.Timer;
import frc.robot.Arm;

import com.ctre.phoenix.motorcontrol.ControlMode;
import com.ctre.phoenix.motorcontrol.can.VictorSPX;

public class GrabbyThing {

    VictorSPX milesLeft;
    VictorSPX zoeIsRight;
    Solenoid wristStabilization;
    DoubleSolenoid grabberPos;
    DigitalInput ballGrabbed;
    PowerDistributionPanel pdp;

    Arm myArm;

    //Driver Inputs
    public boolean intakeRequested;
    public boolean ejectRequested;
    public boolean hatchModeDesired;
    public boolean ballModeDesired;
    boolean wristInvert=false;
    //State Variables
    public boolean cargoModeActive;
    public boolean switchGamePiece;
    public double curArmPos;
    public boolean wristIsAngled;
    public boolean stallProtectionActive;
    boolean hatchMode=false;
    public Timer stallTimer;

    public double currentLim = 40;
    public double curCurrentFromIntake;
    //Calibrations
    Calibration intakeHatchMotorSpeedCal;
    Calibration ejectHatchMotorSpeedCal;
    Calibration intakeCargoMotorSpeedCal;
    Calibration ejectCargoMotorSpeedCal;

    Calibration lowerWristLowPosSwitchCal;
    Calibration upperWristLowPosSwitchCal;
    Calibration lowerWristHighPosSwitchCal;
    Calibration upperWristHighPosSwitchCal;
    Calibration maxStallTimeSeconds;

    private static GrabbyThing singularInstance = null;

    public static synchronized GrabbyThing getInstance() {
        if ( singularInstance == null)
            singularInstance = new GrabbyThing();
        return singularInstance;
    }


    private GrabbyThing() {
        myArm = Arm.getInstance();
        milesLeft = new VictorSPX(RobotConstants.LEFT_INTAKE_CANID);
        zoeIsRight = new VictorSPX(RobotConstants.RIGHT_INTAKE_CANID);

        wristStabilization = new Solenoid(RobotConstants.WRIST_STABILIZATION_CYL);
        grabberPos = new DoubleSolenoid(RobotConstants.GRIPPER_POS_CYL_A, RobotConstants.GRIPPER_POS_CYL_B);
        cargoModeActive = false;
        wristIsAngled = false;
        stallProtectionActive = false;
        stallTimer = new Timer();
        stallTimer.reset();
        pdp = new PowerDistributionPanel();

        intakeHatchMotorSpeedCal = new Calibration("Intake Motor Speed % of Max", 1.0);
        ejectHatchMotorSpeedCal = new Calibration("Eject Motor Speed % of Max", .75);
        intakeCargoMotorSpeedCal = new Calibration("Intake Motor Speed % of Max", 1.0);
        ejectCargoMotorSpeedCal = new Calibration("Eject Motor Speed % of Max", 1.0);

        lowerWristLowPosSwitchCal = new Calibration("When Do We Bend Wrist Lower - Low Pos", -35);//Dallas wants this to Be around This only//to -5
        upperWristLowPosSwitchCal = new Calibration("When Do We Bend Wrist Upper - Low Pos", -20);
        lowerWristHighPosSwitchCal = new Calibration("When Do We Bend Wrist Lower - High Pos", 80);
        upperWristHighPosSwitchCal = new Calibration("When Do We Bend Wrist Upper - High Pos", 140); //Obviously more than the arm travels, but it doesn't matter
        maxStallTimeSeconds = new Calibration("Max stall time seconds", 2);

        hatchGripMode();
        wristAlign();
    }

    //Setting motors and solenoid Pos
    public void hatchGripMode() {
        if(cargoModeActive) {
            grabberPos.set(DoubleSolenoid.Value.kReverse);
            cargoModeActive = false;
        }
    }  
     
    public void cargoGripMode() {
        if(!cargoModeActive) {
            grabberPos.set(DoubleSolenoid.Value.kForward);
            cargoModeActive = true;
        }
    }

    public void ejectCargo() {
        milesLeft.set(ControlMode.PercentOutput, -1*ejectCargoMotorSpeedCal.get());
        zoeIsRight.set(ControlMode.PercentOutput, ejectCargoMotorSpeedCal.get());
    }
    public void ejectHatch() {
        milesLeft.set(ControlMode.PercentOutput, ejectHatchMotorSpeedCal.get());
        zoeIsRight.set(ControlMode.PercentOutput, -1*ejectHatchMotorSpeedCal.get());
    }
    public void intakeCargo() {
        milesLeft.set(ControlMode.PercentOutput, intakeHatchMotorSpeedCal.get());
        zoeIsRight.set(ControlMode.PercentOutput, -1*intakeHatchMotorSpeedCal.get());
    }
    public void intakeHatch() {
        milesLeft.set(ControlMode.PercentOutput, -1*intakeHatchMotorSpeedCal.get());
        zoeIsRight.set(ControlMode.PercentOutput, intakeHatchMotorSpeedCal.get());
    }
    public void holdCargo() {
        milesLeft.set(ControlMode.PercentOutput, 0.25*intakeHatchMotorSpeedCal.get());
        zoeIsRight.set(ControlMode.PercentOutput, -0.25*intakeHatchMotorSpeedCal.get());
    }
    public void holdHatch() {
        milesLeft.set(ControlMode.PercentOutput, -0.25*intakeHatchMotorSpeedCal.get());
        zoeIsRight.set(ControlMode.PercentOutput, 0.25*intakeHatchMotorSpeedCal.get());
    }

    public void wristAlign() {
        curArmPos = myArm.getActualArmHeight();
        if(wristInvert){
            wristStabilization.set(!wristIsAngled);
        }else{
            wristStabilization.set(wristIsAngled);
        }
    }

    public void checkStallProtection() {
        double leftCurrent = pdp.getCurrent(RobotConstants.INTAKE_LEFT_MOTOR_PDP_PORT);
        double rightCurrent = pdp.getCurrent(RobotConstants.INTAKE_LEFT_MOTOR_PDP_PORT);

        if(leftCurrent + rightCurrent > currentLim) {
            if(stallTimer.get() == 0) {
                stallTimer.start();
            }
            if(stallTimer.get() > maxStallTimeSeconds.get()) {
                stallProtectionActive = true;
                stallTimer.stop();
                stallTimer.reset();
            }
        }
        else {
            stallTimer.stop();
            stallTimer.reset();
        }
    }
    

    public void update(){
            
        checkStallProtection();
        if(Arm.getInstance().getActualArmHeight()>=-33 && Arm.getInstance().getActualArmHeight()<=-16 ){
            wristIsAngled=true;
        }else{
            wristIsAngled=false;
        }
        wristAlign();

        intakeRequested = OperatorController.getInstance().getGampieceGrabRequest();
        ejectRequested = OperatorController.getInstance().getGampieceReleaseRequest();
        
        hatchModeDesired = OperatorController.getInstance().getHatchMode();
        if(hatchModeDesired){
            hatchMode=true;
        }

        ballModeDesired = OperatorController.getInstance().getCargoMode();
        if(ballModeDesired){
            hatchMode=false;
        }

        wristInvert= OperatorController.getInstance().getInverted();

        if(hatchModeDesired) {
            hatchGripMode();
        } else {
            cargoGripMode();
        }

        if(cargoModeActive) {
            if(intakeRequested) {
                if(stallProtectionActive) {
                    holdCargo();
                } else {
                    intakeCargo();
                }
            } else if(ejectRequested) {
                stallProtectionActive = false;
                ejectCargo();
            } else {
                stallProtectionActive = false;
                holdCargo();
            }
        } else {
            if(intakeRequested) {
                if(stallProtectionActive) {
                    holdHatch();
                } else {
                    intakeHatch();
                }
            } else if(ejectRequested) {
                stallProtectionActive = false;
                ejectHatch();
            } else {
                stallProtectionActive = false;
                holdHatch();
            }
        } 
    }

    public boolean getIsBallInIntake() {
        return ballGrabbed.get();
    }
    public boolean getIntakeRequested() {
        return intakeRequested;
    }
    public boolean getEjectRequested() {
        return ejectRequested;
    }
    public boolean getMode() {
        return hatchMode;
    }


    public boolean getStallProtectionActive() {
        return stallProtectionActive;
    }
}






