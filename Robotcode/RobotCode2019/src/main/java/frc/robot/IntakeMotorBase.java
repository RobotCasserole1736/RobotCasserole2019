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
import edu.wpi.first.wpilibj.Counter;


/**
 * Add your docs here.
 */
public class IntakeMotorBase extends CasserolePID{
    VictorSPX intakeArmMotor;
    Counter intakeArmHallSensor;

    boolean isOpenLoop = false;

    double outputCmd = 0;
    double curPosDeg=0;

    double direction =0;
    double curMotorCmd=0;

    boolean limitSwitchPressed = false;


    public IntakeMotorBase(double Kp_in, double Ki_in, double Kd_in,int Motor_id,int Hall_Sensor_id){
        super(Kp_in,Ki_in,Kd_in);
        intakeArmMotor = new VictorSPX(Motor_id);
        intakeArmHallSensor = new Counter();
        intakeArmHallSensor.setUpDownCounterMode();
        intakeArmHallSensor.setUpSource(Hall_Sensor_id);
        intakeArmHallSensor.setSemiPeriodMode(true); //Count both risnig and falling edges
        intakeArmHallSensor.setSamplesToAverage(127);
        this.threadName = ("IntakePID " + Motor_id);
    }

    public void setLimitSwitchPressed(boolean pressed){
        limitSwitchPressed =pressed;
    }

    public void killPID(){
        isOpenLoop = true;
        super.stop();
    }

    public void start(){
        isOpenLoop = false;
        super.start();
    }

    public void resetPosition(){
        curPosDeg=0;
    }


    public void checkDirection(){

    }

    double prevHallSensorCount = 0;

    public double getRawSensorCount(){
        return prevHallSensorCount;
    }

    public double samplePosition(){ //must be called externally fast enough

        final double HALL_CONVERSION_FACTOR_DEG_PER_EDGE=360.0/(174.9*2); //From spec sheet, 174.9 pulses per output revolution. 360 degrees/rev, and we are watching every edge (two edge per pulse)
        double curHallSensorCount = intakeArmHallSensor.get();
        double hallSensorDifference = curHallSensorCount - prevHallSensorCount;
        double deltaDeg = HALL_CONVERSION_FACTOR_DEG_PER_EDGE*hallSensorDifference; 

        if(curMotorCmd>0){
            direction=1;
        }else if (curMotorCmd<0){
            direction=-1;
        }else{
            direction=0;
        }

        if(direction>0){
            curPosDeg = curPosDeg+deltaDeg; 
        }else if(direction<0){
            curPosDeg = curPosDeg-deltaDeg; 
        }

        prevHallSensorCount = curHallSensorCount;

        return curPosDeg;
    }

    @Override
    protected double returnPIDInput() {
        return curPosDeg;
    }

    @Override
    protected void usePIDOutput(double pidOutput) {
        //Safety - do not run motor further past limit switch
        if(pidOutput < 0 && limitSwitchPressed){
            pidOutput = 0;
        }

        if(isOpenLoop == false){
            intakeArmMotor.set(ControlMode.PercentOutput,pidOutput);
            curMotorCmd=pidOutput;
        }
    }

    public void setManualMotorCommand(double cmd){
        //Safety - do not run motor further past limit switch
        if(cmd < 0 && limitSwitchPressed){
            cmd = 0;
        }

        if(isOpenLoop == true){
            intakeArmMotor.set(ControlMode.PercentOutput,cmd); 
            curMotorCmd=cmd;
        }
    }
}
