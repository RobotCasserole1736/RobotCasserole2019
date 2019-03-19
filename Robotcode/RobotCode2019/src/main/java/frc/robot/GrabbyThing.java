/*----------------------------------------------------------------------------*/
/* Copyright (c) 2018 FIRST. All Rights Reserved.                             */
/* Open Source Software - may be modified and shared by FRC teams. The code   */
/* must be accompanied by the FIRST BSD license file in the root directory of */
/* the project.                                                               */
/*----------------------------------------------------------------------------*/

package frc.robot;

import frc.lib.Calibration.*;
import frc.lib.DataServer.Signal;
import frc.robot.RobotConstants;
import edu.wpi.first.wpilibj.DigitalInput;
import edu.wpi.first.wpilibj.Solenoid;
import frc.robot.Arm;

import com.ctre.phoenix.motorcontrol.ControlMode;
import com.ctre.phoenix.motorcontrol.can.VictorSPX;
import com.ctre.phoenix.motorcontrol.can.WPI_TalonSRX;
import com.ctre.phoenix.motorcontrol.can.WPI_VictorSPX;
//import com.ctre.phoenix.motorcontrol.can.VictorSPX;

public class GrabbyThing {

    VictorSPX milesLeft;
    VictorSPX zoeIsRight;
    Solenoid wristStabilization;
    Solenoid grabberPos;
    DigitalInput ballGrabbed;

    Arm myArm;

    //Driver Inputs
    public boolean intakeRequested;
    public boolean ejectRequested;
    public boolean hatchModeDesired;
    public boolean ballModeDesired;
    //State Variables
    public boolean switchGamePiece;
    public double curArmPos;
    
    GamePiece gamePiece_in; 
    GripperPos gripperPos_in;
    WristPos wristPos_in;
    GrabbyStateMachine grabbyState_in;
    GrabbyStateMachine curState;
    GrabbyStateMachine prevState;
    GrabbyStateMachine nextState;

    public double currentLim = 40;
    public double curCurrentFromIntake;
    public boolean currentIsOk;
    //Calibrations
    Calibration intakeHatchMotorSpeedCal;
    Calibration ejectHatchMotorSpeedCal;
    Calibration intakeCargoMotorSpeedCal;
    Calibration ejectCargoMotorSpeedCal;

    Calibration lowerWristSwitchCal;
    Calibration upperWristSwitchCal;

    private static GrabbyThing singularInstance = null;

    public static synchronized GrabbyThing getInstance() {
        if ( singularInstance == null)
            singularInstance = new GrabbyThing();
        return singularInstance;
    }


    private GrabbyThing() {
        myArm = Arm.getInstance();
        milesLeft = new VictorSPX(RobotConstants.INTAKE_LEFT_MOTOR_PWM_PORT);
        zoeIsRight = new VictorSPX(RobotConstants.INTAKE_LEFT_MOTOR_PWM_PORT);
        wristStabilization = new Solenoid(RobotConstants.WRIST_STABILIZATION_CYL);
        grabberPos = new Solenoid(RobotConstants.GRIPPER_POS_CYL);

        intakeHatchMotorSpeedCal = new Calibration("Intake Motor Speed % of Max", 0.5);
        ejectHatchMotorSpeedCal = new Calibration("Eject Motor Speed % of Max", 0.5);
        intakeCargoMotorSpeedCal = new Calibration("Intake Motor Speed % of Max", 0.5);
        ejectCargoMotorSpeedCal = new Calibration("Eject Motor Speed % of Max", 0.5);

        lowerWristSwitchCal = new Calibration("When Do We Bend Wrist Lower", -10);
        upperWristSwitchCal = new Calibration("When Do We Bend Wrist Upper",100);
    }

    public enum GamePiece {
        Cargo(0), Hatch(1);

        public final int value;

        private GamePiece(int value) {
            this.value = value;
        }
                
        public int toInt(){
            return this.value;
        }
    }

    public enum GripperPos {
        Expanded(0), Shrunken(1);

        public final int value;

        private GripperPos(int value) {
            this.value = value;
        }
                
        public int toInt(){
            return this.value;
        }
    }
    public enum WristPos {
        LowHighPlace(0), MidPlace(1);
        public final int value;
        private WristPos(int value) {
            this.value = value;
        }
        public int toInt(){
            return this.value;
        }
    }
    public enum GrabbyStateMachine {
        
        HatchReady(0),
        CargoReady(1),
        HatchHold(2),
        CargoHold(3), 
        HatchShoot(4), 
        CargoShoot(5),
        HatchIntake(6),
        CargoIntake(7); 
    
        public final int value;
        private GrabbyStateMachine(int value) {
            this.value = value;
        }

        public int toInt() {
            return this.value;
        }    
    }
    //Set Variables


    //Setting motors and solenoid Pos
    public void hatchGrip() {
        grabberPos.set(false);
    }   
    public void cargoGrip() {
        grabberPos.set(true);
    }

    public void ejectCargo() {
        milesLeft.set(ControlMode.PercentOutput, ejectCargoMotorSpeedCal.get());
        zoeIsRight.set(ControlMode.PercentOutput, -1*ejectCargoMotorSpeedCal.get());
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
        milesLeft.set(ControlMode.PercentOutput, intakeHatchMotorSpeedCal.get());
        zoeIsRight.set(ControlMode.PercentOutput, -1*intakeHatchMotorSpeedCal.get());
    }
    public void switchZone() {
        
    }
    public void wristAlign() {
        curArmPos = myArm.getActualArmHeight();
        if(curArmPos < lowerWristSwitchCal.get() || curArmPos > upperWristSwitchCal.get()) {

        }
    }
    
    
    

    public void update(){
        //Main update loop
        nextState = curState;
            
        if(curCurrentFromIntake > currentLim) {
            currentIsOk = false;
        }
        else {
            currentIsOk = true;
        }
        //Step 0 - save previous state
        prevState = curState;
        
        
        switch(curState) {
            //Can switch States and is ready to grab a game piece
            case HatchReady:
                hatchGrip();
                    if(intakeRequested) {
                        nextState = GrabbyStateMachine.HatchIntake;
                    } else if(switchGamePiece) {
                        nextState = GrabbyStateMachine.CargoReady;
                        switchGamePiece = false;
                    }
            break;

            case CargoReady:
                cargoGrip();
                    if(intakeRequested) {
                        nextState = GrabbyStateMachine.CargoIntake;
                    } else if(switchGamePiece) {
                        nextState = GrabbyStateMachine.HatchReady;
                        switchGamePiece = false;
                    }
            break;

            //Is currently holding a game piece
            case HatchHold:
                if(ejectRequested) {
                nextState = GrabbyStateMachine.HatchShoot;
                }
            break;

            case CargoHold:
            if(ejectRequested){
                nextState = GrabbyStateMachine.CargoShoot;
            } 
            break;

            //Shooting a Game Piece
            case HatchShoot:
                if(ejectRequested) {
                    ejectHatch();
                }
            break; 
            case CargoShoot:
                if(ejectRequested) {
                    ejectCargo();
                }
            break;

            //Intaking a Game Piece 
            case HatchIntake:
                if(intakeRequested && currentIsOk){
                    intakeHatch();
                    
                }
                else {
                    nextState = GrabbyStateMachine.HatchHold;
                }
            break;
            case CargoIntake:
                if(intakeRequested && currentIsOk){
                    intakeCargo();
                } else if(ballGrabbed.get()) {
                    nextState = GrabbyStateMachine.CargoHold;
                }
            break;
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
    public boolean getCurGripperPos() {
        return grabberPos.get();
    }
    public boolean getCurWristPos() {
        return grabberPos.get();
    }
}






