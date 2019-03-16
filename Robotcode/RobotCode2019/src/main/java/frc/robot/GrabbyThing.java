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

import edu.wpi.first.wpilibj.Solenoid;

import com.ctre.phoenix.motorcontrol.ControlMode;
import com.ctre.phoenix.motorcontrol.can.WPI_TalonSRX;
import com.ctre.phoenix.motorcontrol.can.WPI_VictorSPX;
//import com.ctre.phoenix.motorcontrol.can.VictorSPX;

public class GrabbyThing {

    WPI_VictorSPX leftIntakeMotor;
    WPI_VictorSPX rightIntakeMotor;
    Solenoid wristStabilization;
    Solenoid grabberPos;

    //State Variables
    public boolean IntakeRequested;
    public boolean ejectRequested;
    public boolean hatchModeDesired;
    public boolean ballModeDesired;
    //Calibrations
    Calibration intakeMotorSpeedCal;
    Calibration ejectMotorSpeedCal;

    private static GrabbyThing singularInstance = null;

    public static synchronized GrabbyThing getInstance() {
        if ( singularInstance == null)
            singularInstance = new GrabbyThing();
        return singularInstance;
    }

    private GrabbyThing() {
        leftIntakeMotor = WPI_VictorSPX(RobotConstants.INTAKE_MOTOR_LEFT_CANID);
        rightIntakeMotor = WPI_VictorSPX(RobotConstants.INTAKE_MOTOR_RIGHT_CANID);
        wristStabilization = new Solenoid(RobotConstants.WRIST_STABILIZATION_CYL);
        grabberPos = new Solenoid(RobotConstants.GRIPPER_POS_CYL);

        intakeMotorSpeedCal = new Calibration("Intake Motor Speed % of Max", 0.5);
        ejectMotorSpeedCal = new Calibration("Eject Motor Speed % of Max", 0.5);
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

    public void update() {
       if(IntakeRequested) {
           leftIntakeMotor.set(ControlMode.PercentOutput, intakeMotorSpeedCal.get());
           rightIntakeMotor.set(ControlMode.PercentOutput, intakeMotorSpeedCal.get());

       }
       else if()
      
    }
    }




