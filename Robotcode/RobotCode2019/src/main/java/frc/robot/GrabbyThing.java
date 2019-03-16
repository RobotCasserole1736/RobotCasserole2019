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
import com.ctre.phoenix.motorcontrol.can.WPI_TalonSRX;

public class GrabbyThing {

    WPI_TalonSRX leftIntakeMotor;
    WPI_TalonSRX rightIntakeMotor;
    Solenoid wristStabilization;
    Solenoid grabberPos;

    //State Variables
 

    public boolean IntakeRequested;
    public boolean ejectRequested;
    public boolean HatchModeDesired;
    public boolean ballModeDesired;

    private static GrabbyThing singularInstance = null;

    public static synchronized GrabbyThing getInstance() {
        if ( singularInstance == null)
            singularInstance = new GrabbyThing();
        return singularInstance;
    }

    private GrabbyThing() {
        leftIntakeMotor = new WPI_TalonSRX(RobotConstants.INTAKE_LEFT_MOTOR_PWM_PORT);
        rightIntakeMotor = new WPI_TalonSRX(RobotConstants.INTAKE_RIGHT_MOTOR_PWM_PORT);
        wristStabilization = new Solenoid(RobotConstants.WRIST_STABILIZATION_CYL);
        grabberPos = new Solenoid(RobotConstants.GRIPPER_POS_CYL);
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
       
      
    }
    }




