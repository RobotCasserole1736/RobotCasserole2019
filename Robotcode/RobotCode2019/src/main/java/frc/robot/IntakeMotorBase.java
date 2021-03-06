/*----------------------------------------------------------------------------*/
/* Copyright (c) 2018 FIRST. All Rights Reserved.                             */
/* Open Source Software - may be modified and shared by FRC teams. The code   */
/* must be accompanied by the FIRST BSD license file in the root directory of */
/* the project.                                                               */
/*----------------------------------------------------------------------------*/

package frc.robot;

import frc.lib.CasserolePID.CasserolePID;
import com.ctre.phoenix.motorcontrol.ControlMode;
import com.ctre.phoenix.motorcontrol.can.VictorSPX;
import edu.wpi.first.wpilibj.AnalogPotentiometer;

/**
 * Add your docs here.
 */
public class IntakeMotorBase extends CasserolePID{
    VictorSPX intakeArmMotor;
    AnalogPotentiometer armPot;

    boolean isOpenLoop = false;

    boolean isAtForwardLimit = false;

    boolean isInverted = false;
    double outputCmd = 0;

    double lowerLimitVoltage=0;
    double upperLimitVoltage=0;
    double lowerLimitDegrees=0;
    double upperLimitDegrees=0;

    double actualPosDeg = 0;
    double potentiometerReading = 0;

    public IntakeMotorBase(double Kp_in, double Ki_in, double Kd_in,int Motor_id,int Pot_id){
        super(Kp_in,Ki_in,Kd_in);
        intakeArmMotor = new VictorSPX(Motor_id);
        armPot = new AnalogPotentiometer(Pot_id);
        this.threadName = ("IntakePID " + Motor_id);
    }

    public void setInverted(boolean isInverted){
        this.isInverted=isInverted;
    }

    public void killPID(){
        isOpenLoop = true;
        super.stop();
    }

    public void start(){
        isOpenLoop = false;
        super.start();
    }

    public void setAtForwardLimit(boolean atLimit){
        isAtForwardLimit = atLimit;
    }

    double convertVoltsToDeg(double voltage_in){
        double lowerLimitConversionVoltage=lowerLimitVoltage;
        double upperLimitConversionVoltage=upperLimitVoltage;
        if(isInverted){
            lowerLimitConversionVoltage=upperLimitVoltage;
            upperLimitConversionVoltage=lowerLimitVoltage;
        }
        //Thanks Arduino!
        return (voltage_in - lowerLimitConversionVoltage) * (upperLimitDegrees - lowerLimitDegrees) / (upperLimitConversionVoltage - lowerLimitConversionVoltage) + lowerLimitDegrees;
    }
    public void setLowerLimitVoltage(double voltage){
        lowerLimitVoltage = voltage;
    }

    public void setUpperLimitVoltage(double voltage){
        upperLimitVoltage = voltage;
    }

    public void setLowerLimitDegrees(double degrees){
        lowerLimitDegrees = degrees;
    }

    public void setUpperLimitDegrees(double degrees){
        upperLimitDegrees = degrees;
    }

    public double getSensorRawVoltage(){
        return potentiometerReading;
    }

    public double getActualPosDeg(){
        return actualPosDeg;
    }

    @Override
    protected double returnPIDInput() {
        potentiometerReading = armPot.get();
        actualPosDeg = convertVoltsToDeg(potentiometerReading);
        return actualPosDeg;
    }

    @Override
    protected void usePIDOutput(double pidOutput) {
        if(isOpenLoop == false){
            if(isAtForwardLimit && pidOutput > 0){
                pidOutput = 0;
            }
            intakeArmMotor.set(ControlMode.PercentOutput,pidOutput);
        }
    }

    public void setManualMotorCommand(double cmd){
        if(isOpenLoop == true){
            if(isAtForwardLimit && cmd > 0){
                cmd = 0;
            }
            intakeArmMotor.set(ControlMode.PercentOutput,cmd); 
        }
    }
}
