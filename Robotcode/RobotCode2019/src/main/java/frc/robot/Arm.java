package frc.robot;

import edu.wpi.first.wpilibj.DigitalSource;
import edu.wpi.first.wpilibj.Encoder;
import edu.wpi.first.wpilibj.AnalogPotentiometer;
import edu.wpi.first.wpilibj.DigitalInput;

public class Arm {

    /////////Input Commands\\\\\\\\\\\
    public double   desiredPreset;
    
    //////State Varibles\\\\\\\\\\\\
    public double   desiredHeight = 0;
    public double   curentHeight = 0;
    public double   potUpVolt;
    public double   potLowVolt;

    /////////Limit Switches\\\\\\
    public boolean topOfMotion = false;
    public boolean bottomOfMotion = false; 
    
    
    /////////Sensors\\\\\\\\\s
    AnalogPotentiometer armPotPos;
    Encoder armEncoder;


    /////Initialization Code\\\\\\
    private static Arm  singularInstance = null;

    public static synchronized Arm getInstance() {
		if ( singularInstance == null)
			singularInstance = new Arm();
        return singularInstance;
    }

    private Arm() {
        
        armEncoder = new Encoder(0,1);
        //armPotPos = AnalogPotentiometer(); WIP needs fix
    } 

    public enum armPos {
        Top, Middle, Lower, Intake
    }


    public void sampleSensors() {

    }

    /////Use Sensor Data in Calculations\\\\\
    public void update() {
        

    }

    //double convertVoltsToDeg(double voltage_in) {
    //    return (x - LowerLimitVoltage) * (UpperLimitDegrees - LowerLimitDegrees) / (UpperLimitVoltage - LowerLimitVoltage) + LowerLimitDegrees;
    //}

    public void setPositionCmd(armPos pos_in) {

    }

    public void setManualMovementCmd(double mov_cmd_in) {

    }























    
}