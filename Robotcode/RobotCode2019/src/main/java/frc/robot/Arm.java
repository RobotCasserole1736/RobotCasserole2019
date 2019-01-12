package frc.robot;

//import org.usfirst.frc.team1736.lib.Calibration.Calibration;
//import org.usfirst.frc.team1736.lib.Util.CrashTracker;


import edu.wpi.first.wpilibj.Spark;
import frc.lib.WebServer.DriverViewBoolean;
import edu.wpi.first.wpilibj.AnalogPotentiometer;
import edu.wpi.first.wpilibj.Compressor;
import edu.wpi.first.wpilibj.DigitalInput;



public class Arm {

    /////////Moving Things\\\\\\\\\
    Compressor armBreak;
    Spark armMotor;

    /////////Sensors\\\\\\\\\
    AnalogPotentiometer armPot;
        int potChannel;
        double voltageToDegreeMult;
        double zeroOffset;
    DigitalInput upperLimSwitch;
    DigitalInput lowLimSwitch;

        
        

    /////////Input Commands\\\\\\\\\\\
    

    /////////State Varibles\\\\\\\\\\\\
    public double   desiredArmAngle = 0;
    public double   curentHeight = 0;
    public double   potUpVolt;
    public double   potLowVolt;
    public boolean  brakeActivated;
    //Arm State Heights\\
    public double topRocket = 20;
    public double midRocket = 15;
    public double lowRocket = 12;
    public double intakeHeight = 2;

    ///////Pot State\\\\\\\\\\\\
    public double UpperLimitDegrees = 270;
    public double UpperLimitVoltage = 12;
    public double LowerLimitDegrees = 0;
    public double LowerLimitVoltage = 12;
    public double armPotPos;


    /////////Limit Switches\\\\\\
    public boolean topOfMotion ;
    public boolean bottomOfMotion ; 
    
    
    


    /////Initialization Code\\\\\\
    private static Arm  singularInstance = null;

    public static synchronized Arm getInstance() {
		if ( singularInstance == null)
			singularInstance = new Arm();
        return singularInstance;
    }

    private Arm() {
    /////Analog Inputs\\\\\\\\
        armPot = new AnalogPotentiometer(potChannel, voltageToDegreeMult, zeroOffset);
    /////Movers\\\\\
        armBreak = new Compressor(0);
        armMotor = new Spark(0);
    /////Digital Inputs\\\\\\\
        upperLimSwitch = new DigitalInput(0);
        lowLimSwitch = new DigitalInput(1);

    } 

    public enum armPos {
        Top, Middle, Lower, Intake
    }

    double convertVoltsToDeg(double voltage_in) {
        return (voltage_in - LowerLimitVoltage) * (UpperLimitDegrees - LowerLimitDegrees) / (UpperLimitVoltage - LowerLimitVoltage) + LowerLimitDegrees;
    }

    public void sampleSensors() {
       topOfMotion = upperLimSwitch.get();
       topOfMotion = lowLimSwitch.get();

    }

    
    /////Use Sensor Data in Calculations\\\\\
    public void update() {
        
        
    }
   
    armPos pos_in;
    public void setPositionCmd(armPos pos_in) {
        this.pos_in = pos_in;
    }
    
    /////Movement Settings\\\\\
    public void defArmPos() {
        switch(pos_in) {
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
            
        }
    }

    public void setSolBrake(Boolean brake_in) {
        if(brake_in) {
            
        }
        else {
             
        }
    }

    public void setManualMovementCmd(double mov_cmd_in) {
        
        if(mov_cmd_in>0.5 || mov_cmd_in<-0.5) {    
            
        }
        else if(mov_cmd_in < 0.5 && mov_cmd_in > -0.5 && !brakeActivated) {
        
        }
        else {
            mov_cmd_in = 0;
            
        }
        }
    }