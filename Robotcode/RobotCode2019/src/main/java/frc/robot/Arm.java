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
        return (voltage_in - LowerLimitVoltage) * (UpperLimitDegrees - LowerLimitDegrees) / (UpperLimitVoltage - LowerLimitVoltage) + LowerLimitDegrees;
    }

    public void sampleSensors() {
       topOfMotion = upperLimSwitch.get();
       topOfMotion = lowLimSwitch.get();

    }

    
    /////Use Sensor Data in Calculations\\\\\
    public void update() {
        
        
    }
   
    ArmPosReq pos_in;
    public void setPositionCmd(ArmPosReq pos_in) {
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

            case None:
            // Don't change desiredArmAngle
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
