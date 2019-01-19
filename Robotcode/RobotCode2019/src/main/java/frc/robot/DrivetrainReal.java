package frc.robot;

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

import frc.lib.DataServer.Signal;

public class DrivetrainReal implements DrivetrainInterface {

    private ADXRS453_Gyro gyro;
	private int angleOffset;
    public double forwardReverseCmd;
    public double rotationCmd;

    private static final int TIMEOUT_MS = 0;
    private static final double ENCODER_CYCLES_PER_REV = 2048;
    private static final double GEARBOX_RATIO = 72.0/12.0;
    private double motor_speed_rpm_Right = 0;
    private double motor_speed_rpm_Left = 0;


    DrivetrainOpMode opMode;
    DrivetrainOpMode prevOpMode;

    WPI_TalonSRX rightTalon1;
   // WPI_TalonSRX rightTalon2;
    WPI_TalonSRX leftTalon1;
    //WPI_TalonSRX leftTalon2;

    Signal currentR1Sig;
    Signal currentR2Sig;
    Signal currentL1Sig;
    Signal currentL2Sig;
    Signal opModeSig;
    Signal gyroscopeSig;
    Signal wheelSpeedRightSig;
    Signal wheelSpeedLeftSig;

    public DrivetrainReal() {

        gyro = new ADXRS453_Gyro();
        angleOffset = 0;

        rightTalon1 = new WPI_TalonSRX(RobotConstants.DRIVETRAIN_RIGHT_1_CANID);
        //rightTalon2 = new WPI_TalonSRX(RobotConstants.DRIVETRAIN_RIGHT_2_CANID);
        leftTalon1 = new WPI_TalonSRX(RobotConstants.DRIVETRAIN_LEFT_1_CANID);
        //leftTalon2 = new WPI_TalonSRX(RobotConstants.DRIVETRAIN_LEFT_2_CANID);

        //rightTalon2.follow(rightTalon1);
        //leftTalon2.follow(leftTalon1);
        //rightTalon2.setInverted(InvertType.FollowMaster);
        //leftTalon2.setInverted(InvertType.FollowMaster);

        leftTalon1.setInverted(true);

        //Set coast mode always
        rightTalon1.setNeutralMode(NeutralMode.Coast);
        //rightTalon2.setNeutralMode(NeutralMode.Coast);
        leftTalon1.setNeutralMode(NeutralMode.Coast); 
        //leftTalon2.setNeutralMode(NeutralMode.Coast); 

		//Motor 1 is presumed to be the one with a sensor hooked up to it.
		rightTalon1.configSelectedFeedbackSensor(FeedbackDevice.QuadEncoder, 0, TIMEOUT_MS);
		leftTalon1.configSelectedFeedbackSensor(FeedbackDevice.QuadEncoder, 0, TIMEOUT_MS);
		
		//We need a fairly high bandwidth on the velocity measurement, so keep
		// the averaging of velocity samples low to minimize phase shift
		rightTalon1.configVelocityMeasurementWindow(4, TIMEOUT_MS);
		leftTalon1.configVelocityMeasurementWindow(4, TIMEOUT_MS);

        opMode = DrivetrainOpMode.OpenLoop;

        currentR1Sig = new Signal("Drivetrain R1 Motor Current", "A");
        currentR2Sig = new Signal("Drivetrain R2 Motor Current", "A");
        currentL1Sig = new Signal("Drivetrain L1 Motor Current", "A");
        currentL2Sig = new Signal("Drivetrain L2 Motor Current", "A");
        opModeSig    = new Signal("Drivetrain Operation Mode", "Op Mode Enum");
        gyroscopeSig = new Signal("Drivetrain Pos Angle","Deg");
        wheelSpeedRightSig = new Signal("Drivetrain Right Wheel Speed", "RPM");
        wheelSpeedLeftSig = new Signal("Drivetrain Left Wheel Speed", "RPM");
    }

    public void setOpenLoopCmd(double forwardReverseCmd_in, double rotaionCmd_in) {
        prevOpMode = opMode;
        opMode = DrivetrainOpMode.OpenLoop;
        forwardReverseCmd = forwardReverseCmd_in;
        rotationCmd = rotaionCmd_in;
    }

    public void setGyroLockCmd(double forwardReverseCmd_in) {
        prevOpMode = opMode;
        opMode = DrivetrainOpMode.GyroLock;
        forwardReverseCmd = forwardReverseCmd_in;
        rotationCmd = 0;
    }

    public double getGyroAngle() {
		return angleOffset - gyro.getAngle();
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
		motor_speed_rpm_Right = CTRE_VEL_UNITS_TO_RPM(rightTalon1.getSelectedSensorVelocity(0));
		motor_speed_rpm_Left = CTRE_VEL_UNITS_TO_RPM(leftTalon1.getSelectedSensorVelocity(0));
    }

    public double getSpeedRightRPM() {
		return motor_speed_rpm_Right;
    }

    public double getSpeedleftRPM() {
		return motor_speed_rpm_Left;
    }
    
	private double CTRE_VEL_UNITS_TO_RPM(double ctre_units) {
		return ctre_units * 600.0 / ENCODER_CYCLES_PER_REV / 4.0;
	}
    
    public void update() {

        sampleSensors();

        if (opMode == DrivetrainOpMode.OpenLoop) {
            /* Drivetrain running open-loop, assign outputs straight from input commands */
            double motorSpeedLeftCMD = 0;
            double motorSpeedRightCMD = 0;

            motorSpeedLeftCMD = forwardReverseCmd - rotationCmd;
            motorSpeedRightCMD = forwardReverseCmd + rotationCmd;

            rightTalon1.set(motorSpeedRightCMD);
            leftTalon1.set(motorSpeedLeftCMD);
        } else {
            /* Some other mode - stop everything */
            rightTalon1.set(0);
            leftTalon1.set(0);
        }

        /* Update Telemetry */
        double sample_time_ms = LoopTiming.getInstance().getLoopStartTime_sec() * 1000.0;
        currentR1Sig.addSample(sample_time_ms, rightTalon1.getOutputCurrent());
        //currentR2Sig.addSample(sample_time_ms, rightTalon2.getOutputCurrent());
        currentL1Sig.addSample(sample_time_ms, leftTalon1.getOutputCurrent());
        //currentL2Sig.addSample(sample_time_ms, leftTalon2.getOutputCurrent());
        opModeSig.addSample(sample_time_ms, opMode.toInt());
        gyroscopeSig.addSample(sample_time_ms, getGyroAngle());
        wheelSpeedRightSig.addSample(sample_time_ms, getSpeedRightRPM());
        wheelSpeedLeftSig.addSample(sample_time_ms, getSpeedleftRPM());
    }
}