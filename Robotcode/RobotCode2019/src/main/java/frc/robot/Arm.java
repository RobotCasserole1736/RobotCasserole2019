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

//import org.usfirst.frc.team1736.lib.Calibration.Calibration;
//import org.usfirst.frc.team1736.lib.Util.CrashTracker;

import frc.lib.Calibration.*;
import frc.robot.IntakeControl.IntakePos;
import edu.wpi.first.wpilibj.Spark;

import edu.wpi.first.wpilibj.AnalogPotentiometer;
import edu.wpi.first.wpilibj.Solenoid;
import edu.wpi.first.wpilibj.DigitalInput;

import com.revrobotics.CANSparkMax;
import com.revrobotics.CANSparkMaxLowLevel.MotorType;
import com.revrobotics.CANEncoder;


public class Arm {

    /////////Moving Things\\\\\\\\\
    Solenoid armBrake;
    CANSparkMax sadey;
    public double armEncoder;

    /////////Sensors\\\\\\\\\
    AnalogPotentiometer armPot;
    int potChannel;
    double voltageToDegreeMult;
    double zeroOffset;
    DigitalInput upperLimSwitch;
    DigitalInput lowLimSwitch;

    /////////Input Commands\\\\\\\\\\\
    ArmPosReq posIn;

    /////////State Varibles\\\\\\\\\\\\
    public double   desiredArmAngle = 0;
    public double   curHeight = 0;
    public double   desHeight = 0;
    public double   potUpVolt;
    public double   potLowVolt;
    public boolean  brakeActivated;
    public double   pastManMoveCmd = 0;
    public double   curManMoveCmd = 0;
    public double   uncompensatedMotorCmd = 0;
    public boolean  brakeIn = false;
    public double   gravityCompensation = 0;
    public boolean  isZeroed = false;
    public IntakePos curIntakePos;

    //Arm State Heights\\
    
    public double topRocket;
    public double midRocket;
    public double lowRocket;
    public double intakeHeight;
    
    
    Calibration topRocketCal;
    Calibration midRocketCal;
    Calibration lowRocketCal;
    Calibration intakeHeightCal;
    Calibration armUpCal;
    Calibration armDownCal;
    

    ///////////Pot State\\\\\\\\\\\\
    public double UpperLimitDegrees = 270;
    public double UpperLimitVoltage = 12;
    public double LowerLimitDegrees = 0;
    public double LowerLimitVoltage = 12;
    public double armPotPos;


    /////////Limit Switches\\\\\\\\\\
    public boolean topOfMotion;
    public boolean bottomOfMotion; 


    
    /////////Initialization Code\\\\\\\\\\\
    private static Arm  singularInstance = null;

    public static synchronized Arm getInstance() {
        if ( singularInstance == null)
            singularInstance = new Arm();
        return singularInstance;
    }

    private Arm() {
        /////Analog Inputs\\\\\\\\
        armPot = new AnalogPotentiometer(RobotConstants.ARM_POS_SENSOR_PORT, voltageToDegreeMult, zeroOffset);
        /////Movers\\\\\
        armBrake = new Solenoid(RobotConstants.ARM_MECH_BRAKE_SOL_PORT);
        sadey = new CANSparkMax(RobotConstants.ARM_MOTOR_PORT, MotorType.kBrushless);
        armEncoder = sadey.get();
        /////Digital Inputs\\\\\\\
        upperLimSwitch = new DigitalInput(RobotConstants.ARM_UPPER_LIMIT_SWITCH_PORT);
        lowLimSwitch = new DigitalInput(RobotConstants.ARM_LOWER_LIMIT_SWITCH_PORT);
        
        /////Calibration Things\\\\\
        topRocketCal = new Calibration("Arm Top Level Pos (Deg)", 0);
        midRocketCal = new Calibration("Arm Mid Level Pos (Deg)", 0);
        lowRocketCal = new Calibration("Arm Bottom Level Pos (Deg)", 0);
        intakeHeightCal = new Calibration("Arm Intake Level Pos (Deg)", 0);

    } 

    public enum ArmPosReq {
        Top(4), Middle(3), Lower(2), Intake(1), None(0);

        public final int value;

        private ArmPosReq(int value) {
            this.value = value;
        }
                
        public int toInt(){
            return this.value;
        }
    }

    double convertVoltsToDeg(double voltage_in) {
        return (voltage_in - LowerLimitVoltage) * 
                (UpperLimitDegrees - LowerLimitDegrees) / 
                (UpperLimitVoltage - LowerLimitVoltage) + LowerLimitDegrees;
    }

    public void sampleSensors() {
       topOfMotion = upperLimSwitch.get();
       topOfMotion = lowLimSwitch.get();
    }

    
    /////Use Sensor Data in Calculations\\\\\
    public void update() {
        if(!isZeroed) {
            
        } 
        else {
            //Preset Heights Logic

            //If arm == 0 ??needed??
            desHeight = desiredArmAngle;

            if(curHeight - desHeight <= -2) {
                uncompensatedMotorCmd = 1;
            }
            else if (curHeight - desHeight >= 2) {
                uncompensatedMotorCmd = -1;
            }
            else {
                uncompensatedMotorCmd = 0;
            }
            
            if(curManMoveCmd != pastManMoveCmd) {

            }
            //Joystick Operation by Drivers
            
            if(curManMoveCmd != pastManMoveCmd) {
                uncompensatedMotorCmd = curManMoveCmd;
            }

            if(topOfMotion && uncompensatedMotorCmd < 0) {
                sadey.set(uncompensatedMotorCmd);
            } 
            else if(bottomOfMotion && uncompensatedMotorCmd > 0) {
                sadey.set(uncompensatedMotorCmd);
            }

            else {
                sadey.set(uncompensatedMotorCmd);
            } 
            
            if(uncompensatedMotorCmd == 0) {
                armBrake.set(true);
            } 
            else {
                armBrake.set(false);
            }
        } 
    }

    public void setPositionCmd(ArmPosReq posIn) {
        this.posIn = posIn;
    }
    
    /////Movement Settings\\\\\
    public void defArmPos() {
        switch(posIn) {
            case Top:
            desiredArmAngle = topRocket;
             
            break;

            case Middle:
            desiredArmAngle = midRocket;

            break;

            case Lower:
            desiredArmAngle = lowRocket;

            break;

            case Intake:
            desiredArmAngle = intakeHeight;

            break;

            case None:
            // Don't change desiredArmAngle
            break;
            
        }
    }

    public void setManualMovementCmd(double mov_cmd_in) {
        
        if(mov_cmd_in>0.5 || mov_cmd_in<-0.5) {    
            curManMoveCmd = mov_cmd_in;
            brakeIn = false;
        
        }
        else if(mov_cmd_in < 0.5 && mov_cmd_in > -0.5 /*&& !brakeActivated*/) {
        
            curManMoveCmd = 0;
            brakeIn = true;

        }
        else {
            curManMoveCmd = 0;
            
        }
    }

    public void setSolBrake(boolean brakeIn) {
        if(brakeIn) {
            armBrake.set(true); 
        }
        else {
            armBrake.set(true);
        }
    }
        

    public boolean getTopOfMotion() {
        return topOfMotion;
    }

    public boolean getBottomOfMotion() {
        return bottomOfMotion;
    }
            

    public double getActualArmHeight() {
        curHeight = armPot.get();
        return curHeight;
    }

    public double getDesiredArmHeight() {
        return desHeight;
    }

    public double getuncompensatedMotorCmd() {
        return uncompensatedMotorCmd;
    
    }
    public boolean atDesiredHeight() {
        return(desHeight == curHeight);
    }

    /**
     * Set the current state of the intake. If Retracted, and the arm is 
     * inside the Danger Zone, motion should be stopped.
     * 
     */
    public void setIntakeActualState(IntakePos state_in){
        curIntakePos = IntakeControl.getInstance().getEstimatedPosition();   
    }

    /**
     * 
     * @return True if the arm is forcing the intake to extend, false otherwise.
     */
    public boolean intakeExtendOverride(){
        //TODO
        return false;
    }
}