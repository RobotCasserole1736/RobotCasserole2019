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

import edu.wpi.first.wpilibj.DigitalInput;
import edu.wpi.first.wpilibj.Spark;

import com.ctre.phoenix.motorcontrol.can.WPI_TalonSRX;

import edu.wpi.first.wpilibj.AnalogPotentiometer;
import frc.lib.Calibration.Calibration;
import frc.lib.DataServer.Signal;

public class IntakeControl{

    boolean runSimMode;

    DigitalInput ballInIntake;

    WPI_TalonSRX intakeMotor;
    IntakeMotorBase intakeLeftArmMotor;
    IntakeMotorBase intakeRightArmMotor;
    
    int loopCounter = 0;

    Signal leftIntakeMotorPosSig;
    Signal rightIntakeMotorPosSig;
    Signal retractStateCmdSig;
    Signal motorSpeedCmdSig;
    Signal ballInIntakeSig;
    Signal leftIntakePosSensorVoltageSig;
    Signal rightIntakePosSensorVoltageSig;

    Calibration intakeSpeed;
    Calibration ejectSpeed;
    Calibration extendTime;
    Calibration retractAngle;
    Calibration extendAngle;
    Calibration groundAngle;
    Calibration intakeMotorP;
    Calibration intakeMotorI;
    Calibration intakeMotorD;
    Calibration lowerLeftPotVoltage;
    Calibration upperLeftPotVoltage;
    Calibration lowerRightPotVoltage;
    Calibration upperRightPotVoltage;
    Calibration minIntakeAngle;
    Calibration maxIntakeAngle;

    Calibration positionOverride;

    double currentLeftPosition;
    double currentRightPosition;

    final double MAX_ALLOWABLE_ERR_DEG = 5.0;


    DriverController dController;
    OperatorController opController;
    Arm arm;

    boolean ballDetected = false;

    private static IntakeControl iControl = null;

    public static synchronized IntakeControl getInstance() {
        if(iControl == null)
            iControl = new IntakeControl();
        return iControl;
    }
    
    private IntakeControl(){
        runSimMode = RioSimMode.getInstance().isSimMode();

        intakeSpeed = new Calibration("Intake Intake Speed motor cmd", 0.5, 0, 1);
        ejectSpeed = new Calibration("Intake Eject Speed motor cmd", 0.5, 0, 1);
        extendTime = new Calibration("Intake Est Extend Time sec", 0.500, 0, 5);
        intakeMotorP = new Calibration("Intake Motor P", 0.1);
        intakeMotorI = new Calibration("Intake Motor I", 0);
        intakeMotorD = new Calibration("Intake Motor D", 0);
        retractAngle = new Calibration("Intake Angle of Retracted State deg", 5);
        extendAngle = new Calibration("Intake Angle of Extended State deg", 130);
        groundAngle = new Calibration("Intake Angle of Ground State deg", 170);

        lowerLeftPotVoltage = new Calibration("Intake Lowest Value of Left Potentiometer V", 0.292, 0, 6);
        upperLeftPotVoltage = new Calibration("Intake Highest Value of Left Potentiometer V", 0.982, 0, 6);
        lowerRightPotVoltage = new Calibration("Intake Lowest Value of Right Potentiometer V", 0.0573, 0, 6);
        upperRightPotVoltage = new Calibration("Intake Highest Value of Right Potentiometer V", 0.750, 0, 6);
        minIntakeAngle = new Calibration("Intake Minimum Angle deg", 0) ;
        maxIntakeAngle = new Calibration("Intake Maximum Angle deg", 180) ;
        
        positionOverride = new Calibration("Intake Position Override Enable", 0, 0, 5);
        
        ballInIntake = new DigitalInput(RobotConstants.BALL_INTAKE_PORT);
        intakeMotor = new WPI_TalonSRX(RobotConstants.INTAKE_MOTOR_CANID);
        //TODO figure out which one is inverted
        intakeLeftArmMotor = new IntakeMotorBase(intakeMotorP.get(),intakeMotorI.get(),intakeMotorD.get(),RobotConstants.INTAKE_MOTOR_LEFT_CANID,RobotConstants.INTAKE_LEFT_POT_PORT);
        intakeRightArmMotor = new IntakeMotorBase(intakeMotorP.get(),intakeMotorI.get(),intakeMotorD.get(),RobotConstants.INTAKE_MOTOR_RIGHT_CANID,RobotConstants.INTAKE_RIGHT_POT_PORT);
        intakeLeftArmMotor.setInverted(false);
        intakeRightArmMotor.setInverted(true);
        intakeLeftArmMotor.setLowerLimitVoltage(lowerLeftPotVoltage.get());
        intakeLeftArmMotor.setUpperLimitVoltage(upperLeftPotVoltage.get());
        intakeLeftArmMotor.setLowerLimitDegrees(minIntakeAngle.get());
        intakeLeftArmMotor.setUpperLimitDegrees(maxIntakeAngle.get());
        intakeRightArmMotor.setLowerLimitVoltage(lowerRightPotVoltage.get());
        intakeRightArmMotor.setUpperLimitVoltage(upperRightPotVoltage.get());
        intakeRightArmMotor.setLowerLimitDegrees(minIntakeAngle.get());
        intakeRightArmMotor.setUpperLimitDegrees(maxIntakeAngle.get());

        dController = DriverController.getInstance();
        opController = OperatorController.getInstance();
        arm = Arm.getInstance();
        leftIntakeMotorPosSig = new Signal("Intake Left Motor Actual Position","deg");
        rightIntakeMotorPosSig = new Signal("Intake Right Motor Actual Position","deg");
        leftIntakePosSensorVoltageSig = new Signal("Intake Left Sensor Raw Voltage","V");
        rightIntakePosSensorVoltageSig = new Signal("Intake Right Sensor Raw Voltage","V");
        retractStateCmdSig = new Signal("Intake Commanded Position", "Intake Pos Enum");
        motorSpeedCmdSig = new Signal("Intake Roller Motor Command", "cmd");
        ballInIntakeSig = new Signal("Intake Ball Present", "bool");

    }

    //Intake positions that can be requested
    public enum IntakePos {
        Extend(0), Retract(1), Ground(2), Stop(3);

        public final int value;

        private IntakePos(int value) {
            this.value = value;
        }
                
        public int toInt(){
            return this.value;
        }
    }
    
    
    //Intake positions that can be requested
    public enum IntakeSpd {
        Stop(0), Intake(1), Eject(2);

        public final int value;

        private IntakeSpd(int value) {
            this.value = value;
        }
        
        public int toInt(){
            return this.value;
        }
    }

    //Start with intake retracted and stopped
    private IntakePos intakePosCmd = IntakePos.Retract;
    private IntakeSpd intakeSpdCmd = IntakeSpd.Stop;

    public void setPositionCmd(IntakePos posIn){

        //Test only - enable manual control from driver controller when override is active
        if(positionOverride.get() == 1.0){
            int dpadPos = DriverController.getInstance().xb.getPOV();

            if(dpadPos == 0){
                intakePosCmd = IntakePos.Ground;
            } else if(dpadPos == 90 || dpadPos == 270){
                intakePosCmd = IntakePos.Extend;
            } else if(dpadPos == 180){
                intakePosCmd = IntakePos.Retract;
            }
        } else {
            intakePosCmd = posIn;
        }

    }

    public IntakePos getPositionCmd(){
        return intakePosCmd;
    }

    public void setSpeedCmd(IntakeSpd speedIn){
        intakeSpdCmd=speedIn;
    }

    public void emergencyStop(){
        intakeLeftArmMotor.stop();
        intakeRightArmMotor.stop();
        intakePosCmd=IntakePos.Stop;
        intakeSpdCmd=IntakeSpd.Stop;
        intakeMotor.set(0);
    }

    public boolean isAtDesPos(){
        if(runSimMode){
            return true;
        } else {
            if(Math.abs(intakeLeftArmMotor.getCurError())<=MAX_ALLOWABLE_ERR_DEG && Math.abs(intakeRightArmMotor.getCurError())<=MAX_ALLOWABLE_ERR_DEG){
                return true;
            }else{
                return false;
            }
        }
    }


    public void sampleSensors(){
        if(!runSimMode){
            ballDetected = !ballInIntake.get(); //Sensor outputs high for no ball, low for ball 
            currentLeftPosition= intakeLeftArmMotor.returnPIDInput();
            currentRightPosition= intakeRightArmMotor.returnPIDInput();
        }
    }

    public void update(){
        double intakeMotorCmd = 0;
        
        //Temp - do this elsewhere
        intakeLeftArmMotor.setKp(intakeMotorP.get());
        intakeLeftArmMotor.setKi(intakeMotorI.get());
        intakeLeftArmMotor.setKd(intakeMotorD.get());

        intakeRightArmMotor.setKp(intakeMotorP.get());
        intakeRightArmMotor.setKi(intakeMotorI.get());
        intakeRightArmMotor.setKd(intakeMotorD.get());

        
        sampleSensors();

        if(runSimMode){
            if(intakePosCmd==IntakePos.Extend){
                currentLeftPosition = (extendAngle.get());
                currentRightPosition = (extendAngle.get());
            }else if(intakePosCmd==IntakePos.Ground){
                currentLeftPosition = (groundAngle.get());
                currentRightPosition = (groundAngle.get()); 
            }else if(intakePosCmd==IntakePos.Retract){
                currentLeftPosition = (retractAngle.get());
                currentRightPosition = (retractAngle.get()); 
            }

            ballDetected = false;

        } else {
    
            if(MatchState.getInstance().GetPeriod() != MatchState.Period.OperatorControl &&
               MatchState.getInstance().GetPeriod() != MatchState.Period.Autonomous) {
                //Start intake within frame perimiter and in safe state
                setPositionCmd(IntakePos.Retract);
                setSpeedCmd(IntakeSpd.Stop);
            }
    
            //Intake Arm stuffs
            if(intakePosCmd==IntakePos.Extend){
                intakeLeftArmMotor.setSetpoint(extendAngle.get());
                intakeRightArmMotor.setSetpoint(extendAngle.get());
            }else if(intakePosCmd==IntakePos.Ground){
                intakeLeftArmMotor.setSetpoint(groundAngle.get());
                intakeRightArmMotor.setSetpoint(groundAngle.get()); 
            }else if(intakePosCmd==IntakePos.Retract){
                intakeLeftArmMotor.setSetpoint(retractAngle.get());
                intakeRightArmMotor.setSetpoint(retractAngle.get()); 
            }else{
                //Don't change the setpoint otherwise.
            }
            

            //Intake motor control
            if((ballDetected) && (intakeSpdCmd == IntakeSpd.Intake)){ //If we got a ball, don't Intake
                intakeMotorCmd = 0;
            }else if((!ballDetected) && (intakeSpdCmd == IntakeSpd.Intake)){ //If we don't, Intake
                intakeMotorCmd = intakeSpeed.get();
            }else if(intakeSpdCmd == IntakeSpd.Stop){ //Whether we have a ball or not, we can Stop and Eject
                intakeMotorCmd = 0;
            }else if(intakeSpdCmd == IntakeSpd.Eject){
                intakeMotorCmd = -1 * ejectSpeed.get();
            }else{ //If for some reason it is confused, don't run the intake
                intakeMotorCmd = 0;
            }
    
            intakeMotor.set(-1*intakeMotorCmd); //motor is mechanically inverted
    
            double sampleTimeMS = LoopTiming.getInstance().getLoopStartTimeSec() * 1000.0;
            motorSpeedCmdSig.addSample(sampleTimeMS, intakeMotorCmd);
            updateTelemetry();
        }


    }

    public void updateTelemetry(){
        /* Update Telemetry */
        double sampleTimeMS = LoopTiming.getInstance().getLoopStartTimeSec() * 1000.0;
        leftIntakeMotorPosSig.addSample(sampleTimeMS, currentLeftPosition);
        rightIntakeMotorPosSig.addSample(sampleTimeMS, currentRightPosition);
        leftIntakePosSensorVoltageSig.addSample(sampleTimeMS, intakeLeftArmMotor.getSensorRawVoltage());
        rightIntakePosSensorVoltageSig.addSample(sampleTimeMS, intakeRightArmMotor.getSensorRawVoltage());
        retractStateCmdSig.addSample(sampleTimeMS, intakePosCmd.toInt());
        ballInIntakeSig.addSample(sampleTimeMS, ballDetected);
    }

    public double getLeftArmPosition() {
        return currentLeftPosition;
    }

    public double getRightArmPosition() {
        return currentRightPosition;
    }

    public boolean isBallDetected(){
        return ballDetected;
    }
    public void openLoop(){
        intakeLeftArmMotor.stop();
        intakeRightArmMotor.stop();
    }
    public void closedLoop(){
        intakeLeftArmMotor.start();
        intakeRightArmMotor.start();
    }
}