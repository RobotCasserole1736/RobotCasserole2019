package frc.robot;

import com.ctre.phoenix.motorcontrol.ControlMode;
import com.ctre.phoenix.motorcontrol.FeedbackDevice;
import com.ctre.phoenix.motorcontrol.InvertType;
import com.ctre.phoenix.motorcontrol.NeutralMode;

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
 *    find it educational and informative! 
 *  Please feel free to snag our software for your own use in whatever project
 *    you have going on right now! We'd love to be able to help out! Shoot us 
 *    any questions you may have, all our contact info should be on our website
 *    (listed above).
 *  If you happen to end up using our software to make money, that is wonderful!
 *    Robot Casserole is always looking for more sponsors, so we'd be very appreciative
 *    if you would consider donating to our club to help further STEM education.
 */

import com.ctre.phoenix.motorcontrol.can.WPI_TalonSRX;

import edu.wpi.first.wpilibj.PIDController;
import edu.wpi.first.wpilibj.PIDOutput;
import edu.wpi.first.wpilibj.PIDSource;
import edu.wpi.first.wpilibj.PIDSourceType;
import edu.wpi.first.wpilibj.Timer;
import frc.lib.Calibration.Calibration;
import frc.lib.DataServer.Signal;

public class DrivetrainReal implements DrivetrainInterface, PIDSource, PIDOutput {

    private ADXRS453_Gyro gyro;
    private int angleOffset;
    public double forwardReverseCmd;
    public double rotationCmd;
    public double targetAngleLockDesiredAngle;

    private static final int TIMEOUT_MS = 0;
    private static final double ENCODER_CYCLES_PER_REV = 360;
    private double motorSpeedRPMRight = 0;
    private double motorSpeedRPMLeft = 0;

    DrivetrainOpMode opMode; /* The present operational mode */
    DrivetrainOpMode opModeCmd; /* The most recently commanded operational mode */
    DrivetrainOpMode prevOpMode; /* the previous operational mode */

    WPI_TalonSRX rightTalon1;
    WPI_TalonSRX rightTalon2;
    WPI_TalonSRX leftTalon1;
    WPI_TalonSRX leftTalon2;

    PIDController gyroLockPID;

    double gyroLockRotationCmd;
    double desiredAngle;

    double leftSpeedCmd_RPM;
    double rightSpeedCmd_RPM;
    double headingCmd_deg;

    double left1Current  = 0;
    double left2Current  = 0;
    double right1Current = 0;
    double right2Current = 0;

    Calibration gyroGain_P;
    Calibration gyroGain_I;
    Calibration gyroGain_D;

    Calibration leftDtGain_P;
    Calibration leftDtGain_I;
    Calibration leftDtGain_D;
    Calibration leftDtGain_F;
    Calibration rightDtGain_P;
    Calibration rightDtGain_I;
    Calibration rightDtGain_D;
    Calibration rightDtGain_F;

    Calibration gyroCompGain_P;

    //Signal currentR1Sig;
    //Signal currentR2Sig;
    //Signal currentL1Sig;
    //Signal currentL2Sig;
    //Signal opModeSig;
    Signal angleActSig;
    Signal angleDesSig;
    Signal wheelSpdActRightSig;
    Signal wheelSpdActLeftSig;
    Signal wheelSpdDesRightSig;
    Signal wheelSpdDesLeftSig;
    Signal leftMotorCmdSig;
    Signal rightMotorCmdSig;
    //Signal gyroLockRotationCmdSig;

    Signal sensorSampleTimerSig;
    Signal modeTransitionTimerSig;
    Signal motorSetTimerSig;
    Signal telemetryTimerSig;

    public DrivetrainReal() {
        gyro = new ADXRS453_Gyro();
        angleOffset = 0;

        sensorSampleTimerSig = new Signal("Drivetrain Timer Sensor Sample", "ms");
        modeTransitionTimerSig = new Signal("Drivetrain Timer Mode Transition", "ms");
        motorSetTimerSig = new Signal("Drivetrain Timer Motor Update", "ms");
        telemetryTimerSig = new Signal("Drivetrain Timer Telemetry", "ms");

        /* Configure motor controllers */
        rightTalon1 = new WPI_TalonSRX(RobotConstants.DRIVETRAIN_RIGHT_1_CANID);
        rightTalon2 = new WPI_TalonSRX(RobotConstants.DRIVETRAIN_RIGHT_2_CANID);
        leftTalon1 = new WPI_TalonSRX(RobotConstants.DRIVETRAIN_LEFT_1_CANID);
        leftTalon2 = new WPI_TalonSRX(RobotConstants.DRIVETRAIN_LEFT_2_CANID);

        //Reset to factory defaults
        rightTalon1.configFactoryDefault();
        rightTalon2.configFactoryDefault();
        leftTalon1.configFactoryDefault(); 
        leftTalon2.configFactoryDefault(); 

        //Set up master/slave configuration per side of drivetrain
        //NOTE - electrical dislikes the usage of "master/slave" terminology, 
        //       so henceforth we shall refer to it as "master/intern".
        //We've noticed that sometimes this configuration somethimes doesn't seem to take, and
        // we have no clue why. As a maybe-this-might-work-mitgation, set the config multiple times.
        for(int i =0; i < 10; i++){
            rightTalon2.follow(rightTalon1);
            leftTalon2.follow(leftTalon1);
        }

        //Invert the motor direction on one side of the drivetrain.
        rightTalon1.setInverted(true);
        leftTalon1.setInverted(false);
        rightTalon2.setInverted(InvertType.FollowMaster);
        leftTalon2.setInverted(InvertType.FollowMaster);

        //Set up Phase so that the sensors report positive measurment for forward motion
        rightTalon1.setSensorPhase(false);
        leftTalon1.setSensorPhase(false);

        //Set coast mode always
        rightTalon1.setNeutralMode(NeutralMode.Coast);
        rightTalon2.setNeutralMode(NeutralMode.Coast);
        leftTalon1.setNeutralMode(NeutralMode.Coast); 
        leftTalon2.setNeutralMode(NeutralMode.Coast); 

        //Motor Controller 1 is presumed to be the one with a sensor hooked up to it.
        rightTalon1.configSelectedFeedbackSensor(FeedbackDevice.QuadEncoder, 0, TIMEOUT_MS);
        leftTalon1.configSelectedFeedbackSensor(FeedbackDevice.QuadEncoder, 0, TIMEOUT_MS);
        
        //We need a fairly high bandwidth on the velocity measurement, so keep
        // the averaging of velocity samples low to minimize phase shift
        rightTalon1.configVelocityMeasurementWindow(4, TIMEOUT_MS);
        leftTalon1.configVelocityMeasurementWindow(4, TIMEOUT_MS);



        // Start in OpenLoop
        opMode = DrivetrainOpMode.OpenLoop;
        opModeCmd = DrivetrainOpMode.OpenLoop;
        prevOpMode = DrivetrainOpMode.OpenLoop;

        // Configure closed-loop gain calibrations
        gyroGain_P = new Calibration("Drivetrain Gyro Lock P Gain", 0.06); 
        gyroGain_I = new Calibration("Drivetrain Gyro Lock I Gain", 0.0); 
        gyroGain_D = new Calibration("Drivetrain Gyro Lock D Gain", 0.012); 

        leftDtGain_P  = new Calibration("Drivetrain Left P Gain", 12.5); 
        leftDtGain_I  = new Calibration("Drivetrain Left I Gain", 0.008); 
        leftDtGain_D  = new Calibration("Drivetrain Left D Gain", 0.35); 
        leftDtGain_F  = new Calibration("Drivetrain Left F Gain", 2.0);
        rightDtGain_P = new Calibration("Drivetrain Right P Gain", 12.5);
        rightDtGain_I = new Calibration("Drivetrain Right I Gain", 0.008);
        rightDtGain_D = new Calibration("Drivetrain Right D Gain", 0.35);
        rightDtGain_F = new Calibration("Drivetrain Right F Gain", 2.0);

        gyroCompGain_P = new Calibration("Drivetrain Gyro Compensation P Gain", 5.0);

        //Telemetry
        //currentR1Sig = new Signal("Drivetrain R1 Motor Current", "A");
        //currentR2Sig = new Signal("Drivetrain R2 Motor Current", "A");
        //currentL1Sig = new Signal("Drivetrain L1 Motor Current", "A");
        //currentL2Sig = new Signal("Drivetrain L2 Motor Current", "A");
        //opModeSig = new Signal("Drivetrain Operation Mode", "Op Mode Enum");
        angleActSig = new Signal("Drivetrain Actual Pose Angle", "Deg");
        angleDesSig = new Signal("Drivetrain Desired Pose Angle", "Deg");
        wheelSpdActRightSig = new Signal("Drivetrain Right Wheel Actual Speed", "RPM");
        wheelSpdActLeftSig = new Signal("Drivetrain Left Wheel Actual Speed", "RPM");
        wheelSpdDesRightSig = new Signal("Drivetrain Right Wheel Desired Speed", "RPM");
        wheelSpdDesLeftSig = new Signal("Drivetrain Left Wheel Desired Speed", "RPM");
        leftMotorCmdSig = new Signal("Drivetrain Left Motor Command", "cmd");
        rightMotorCmdSig = new Signal("Drivetrain Right Motor Command", "cmd");
        //gyroLockRotationCmdSig = new Signal("Drivetrain Gyro-Lock Rotation Command", "cmd");

        gyroLockPID = new PIDController(gyroGain_P.get(), gyroGain_I.get(), gyroGain_D.get(), this, this);

        updateGains(true);
    }

    double angleErrorInput = 0;
    public void setPositionCmd(double forwardReverseCmd, double angleError){
        opModeCmd = DrivetrainOpMode.TargetAngleLock;
        this.forwardReverseCmd = forwardReverseCmd;
        angleErrorInput = angleError;

    }

    public void setOpenLoopCmd(double forwardReverseCmd, double rotationCmd) {
        opModeCmd = DrivetrainOpMode.OpenLoop;
        this.forwardReverseCmd = forwardReverseCmd;
        this.rotationCmd = rotationCmd;
    }

    public void setGyroLockCmd(double forwardReverseCmd) {
        opModeCmd = DrivetrainOpMode.GyroLock;
        this.forwardReverseCmd = forwardReverseCmd;
    }

    public void setClosedLoopSpeedCmd(double leftCmdRPM, double rightCmdRPM) {
        opModeCmd = DrivetrainOpMode.ClosedLoop;
        leftSpeedCmd_RPM  = leftCmdRPM;
        rightSpeedCmd_RPM = rightCmdRPM;
    }

    public void setClosedLoopSpeedCmd(double leftCmdRPM, double rightCmdRPM, double headingCmdDeg) {
        opModeCmd = DrivetrainOpMode.ClosedLoopWithGyro;
        leftSpeedCmd_RPM  = leftCmdRPM;
        rightSpeedCmd_RPM = rightCmdRPM;
        headingCmd_deg = headingCmdDeg;
    }

    public double getGyroAngle() {
        return angleOffset + gyro.getAngle();
    }

    public void resetGyro() {
        gyro.reset();
    }

    public void setGyroAngleOffset(int angle) {
        angleOffset = angle;
    }

    public int getGyroAngleOffset() {
        return angleOffset;
    }

    public boolean isGyroOnline() {
        return gyro.isOnline();
    }

    public void sampleSensors() {
        motorSpeedRPMRight = CTRE_VelUnitsToRPM(rightTalon1.getSelectedSensorVelocity(0));
        motorSpeedRPMLeft = CTRE_VelUnitsToRPM(leftTalon1.getSelectedSensorVelocity(0));
        //left1Current  = leftTalon1.getOutputCurrent();
        //left2Current  = leftTalon2.getOutputCurrent();
        //right1Current = rightTalon1.getOutputCurrent();
        //right2Current = rightTalon2.getOutputCurrent();

    }

    public double getRightWheelSpeedRPM() {
        return motorSpeedRPMRight;
    }

    public double getLeftWheelSpeedRPM() {
        return motorSpeedRPMLeft;
    }

    public double getLeftMotorCmd() {
        return leftTalon1.getMotorOutputPercent();
    }

    public double getRightMotorCmd() {
        return rightTalon1.getMotorOutputPercent();
    }

    public double getGyroLockRotationCmd(){
        return gyroLockRotationCmd;
    }

    private double CTRE_VelUnitsToRPM(double ctreUnits) {
        return ctreUnits * 600.0 / ENCODER_CYCLES_PER_REV / 4.0;
    }

    private double RPMtoCTRE_VelUnits(double ctreUnits) {
        return ctreUnits / 600.0 * ENCODER_CYCLES_PER_REV * 4.0;
    }

    public double getLeftTalon1Current() {
        return left1Current;
    }

    public double getLeftTalon2Current() {
        return left2Current;
    }

    public double getRightTalon1Current() {
        return right1Current;
    }

    public double getRightTalon2Current() {
        return right2Current;
    }
    
    public void updateGains(boolean force){
        if(force || gyroGain_P.isChanged() || gyroGain_I.isChanged() || gyroGain_D.isChanged()){
            gyroLockPID.setPID(gyroGain_P.get(), gyroGain_I.get(), gyroGain_D.get());
            gyroGain_P.acknowledgeValUpdate();
            gyroGain_I.acknowledgeValUpdate();
            gyroGain_D.acknowledgeValUpdate();
        }

        if(force || rightDtGain_P.isChanged()){
            rightTalon1.config_kP(0, rightDtGain_P.get());
            rightDtGain_P.acknowledgeValUpdate();
        }
        if(force || rightDtGain_I.isChanged()){
            rightTalon1.config_kI(0, rightDtGain_I.get());
            rightDtGain_I.acknowledgeValUpdate();
        }
        if(force || rightDtGain_D.isChanged()){
            rightTalon1.config_kD(0, rightDtGain_D.get());
            rightDtGain_D.acknowledgeValUpdate();
        }
        if(force || rightDtGain_F.isChanged()){
            rightTalon1.config_kF(0, rightDtGain_F.get());
            rightDtGain_F.acknowledgeValUpdate();
        }
        
        if(force || leftDtGain_P.isChanged()){
            leftTalon1.config_kP(0, leftDtGain_P.get());
            leftDtGain_P.acknowledgeValUpdate();
        }
        if(force || leftDtGain_I.isChanged()){
            leftTalon1.config_kI(0, leftDtGain_I.get());
            leftDtGain_I.acknowledgeValUpdate();
        }
        if(force || leftDtGain_D.isChanged()){
            leftTalon1.config_kD(0, leftDtGain_D.get());
            leftDtGain_D.acknowledgeValUpdate();
        }
        if(force || leftDtGain_F.isChanged()){
            leftTalon1.config_kF(0, leftDtGain_F.get());
            leftDtGain_F.acknowledgeValUpdate();
        }
    }
    
    public void update() {
        double sampleTimeMS = LoopTiming.getInstance().getLoopStartTimeSec() * 1000.0;
        Timer updateTimer = new Timer();
        updateTimer.start();

        
        
        updateTimer.reset();
        sampleSensors();
        sensorSampleTimerSig.addSample(sampleTimeMS, updateTimer.get() * 1000);

        updateTimer.reset();
        prevOpMode = opMode;
        opMode = opModeCmd;


        // Handle mode transition changes
        if(prevOpMode != DrivetrainOpMode.GyroLock && opMode == DrivetrainOpMode.GyroLock){
            //Going into GyroLock
            desiredAngle = getGyroAngle();
            gyroLockPID.setSetpoint(desiredAngle);
            gyroLockPID.enable();
        } else if(prevOpMode != DrivetrainOpMode.OpenLoop && opMode == DrivetrainOpMode.OpenLoop) {
            //Going into OpenLoop
            gyroLockPID.disable();
        } else if(prevOpMode != DrivetrainOpMode.TargetAngleLock && opMode == DrivetrainOpMode.TargetAngleLock){
            desiredAngle = getGyroAngle() - angleErrorInput;
            gyroLockPID.setSetpoint(desiredAngle);
            gyroLockPID.enable();
            JeVoisInterface.getInstance().latchTarget();//Ensure we one-time save pictures on the camera
        } else if(prevOpMode != DrivetrainOpMode.ClosedLoop && opMode == DrivetrainOpMode.ClosedLoop) {
            gyroLockPID.disable();
            //I term Accumulator should be auto-cleared
        } else if(prevOpMode != DrivetrainOpMode.ClosedLoopWithGyro && opMode == DrivetrainOpMode.ClosedLoopWithGyro) {
            gyroLockPID.disable();
            //I term Accumulator should be auto-cleared
        }

        modeTransitionTimerSig.addSample(sampleTimeMS, updateTimer.get() * 1000);

        updateTimer.reset();

        if (opMode == DrivetrainOpMode.OpenLoop) {
            /* Drivetrain running open-loop, assign outputs straight from input commands */
            double motorSpeedLeftCMD = 0;
            double motorSpeedRightCMD = 0;

            motorSpeedLeftCMD = Utils.capMotorCmd(forwardReverseCmd + rotationCmd);
            motorSpeedRightCMD = Utils.capMotorCmd(forwardReverseCmd - rotationCmd);

            rightTalon1.set(ControlMode.PercentOutput, motorSpeedRightCMD);
            //rightTalon2.set(ControlMode.PercentOutput, motorSpeedRightCMD);
            leftTalon1.set(ControlMode.PercentOutput, motorSpeedLeftCMD);
            //leftTalon2.set(ControlMode.PercentOutput, motorSpeedLeftCMD);

        } else if (opMode == DrivetrainOpMode.GyroLock || opMode == DrivetrainOpMode.TargetAngleLock){
            /* Drivetrain running in Gyro-lock. Fwd/Rev command comes from driver, but rotation from a closed-loop control algorithm*/

            double motorSpeedLeftCMD = 0;
            double motorSpeedRightCMD = 0;

            headingCmd_deg = gyroLockRotationCmd;
            motorSpeedLeftCMD = forwardReverseCmd - gyroLockRotationCmd;
            motorSpeedRightCMD = forwardReverseCmd + gyroLockRotationCmd;

            rightTalon1.set(ControlMode.PercentOutput, motorSpeedRightCMD);
            leftTalon1.set(ControlMode.PercentOutput, motorSpeedLeftCMD);


        } else if (opMode == DrivetrainOpMode.ClosedLoop){
            /* Drivetrain running in closed loop mode */
            rightTalon1.set(ControlMode.Velocity, RPMtoCTRE_VelUnits(rightSpeedCmd_RPM));
            leftTalon1.set(ControlMode.Velocity,  RPMtoCTRE_VelUnits(leftSpeedCmd_RPM));

        } else if (opMode == DrivetrainOpMode.ClosedLoopWithGyro){
            /* Drivetrain running in closed loop mode with gyro compensation. */
            double gyroErr =  headingCmd_deg - getGyroAngle();

            rightTalon1.set(ControlMode.Velocity, RPMtoCTRE_VelUnits(rightSpeedCmd_RPM + gyroErr * gyroCompGain_P.get()));
            leftTalon1.set(ControlMode.Velocity,  RPMtoCTRE_VelUnits(leftSpeedCmd_RPM  - gyroErr * gyroCompGain_P.get()));

        } else {
            /* Some other mode we didn't write software for. Hmmm. Programming team did a bad, so stop everything */
            rightTalon1.set(ControlMode.PercentOutput, 0);
            leftTalon1.set(ControlMode.PercentOutput, 0);
        }

        motorSetTimerSig.addSample(sampleTimeMS, updateTimer.get() * 1000);

        updateTimer.reset();

        /* Update Telemetry */

        //currentL1Sig.addSample(sampleTimeMS, left1Current );
        //currentL2Sig.addSample(sampleTimeMS, left2Current );
        //currentR1Sig.addSample(sampleTimeMS, right1Current);
        //currentR2Sig.addSample(sampleTimeMS, right2Current);
        //opModeSig.addSample(sampleTimeMS, opMode.toInt());
        angleActSig.addSample(sampleTimeMS, getGyroAngle());
        angleDesSig.addSample(sampleTimeMS, headingCmd_deg);
        wheelSpdActRightSig.addSample(sampleTimeMS, getRightWheelSpeedRPM());
        wheelSpdActLeftSig.addSample(sampleTimeMS, getLeftWheelSpeedRPM());
        wheelSpdDesRightSig.addSample(sampleTimeMS, rightSpeedCmd_RPM);
        wheelSpdDesLeftSig.addSample(sampleTimeMS, leftSpeedCmd_RPM);
        leftMotorCmdSig.addSample(sampleTimeMS, getLeftMotorCmd());
        rightMotorCmdSig.addSample(sampleTimeMS, getRightMotorCmd());
        //gyroLockRotationCmdSig.addSample(sampleTimeMS, getGyroLockRotationCmd());

        telemetryTimerSig.addSample(sampleTimeMS, updateTimer.get() * 1000);
        
    }

    @Override
    public void pidWrite(double output) {
        if(gyro.isOnline()){
            gyroLockRotationCmd = output;
        } else {
            gyroLockRotationCmd = 0; //If gyro is faulted, don't attempt to rotate.
        }

    }

    @Override
    public void setPIDSourceType(PIDSourceType pidSource) {
        
    }

    @Override
    public PIDSourceType getPIDSourceType() {
        return PIDSourceType.kDisplacement;
    }

    @Override
    public double pidGet() {
        return getGyroAngle();
    }
}