package frc.robot;

import com.ctre.phoenix.motorcontrol.InvertType;

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

    DrivetrainOpMode opMode;
    DrivetrainOpMode prevOpMode;

    WPI_TalonSRX rightTalon1;
    WPI_TalonSRX rightTalon2;
    WPI_TalonSRX leftTalon1;
    WPI_TalonSRX leftTalon2;

    Signal currentR1Sig;
    Signal currentR2Sig;
    Signal currentL1Sig;
    Signal currentL2Sig;
    Signal opModeSig;
    Signal gyroscopeSig;

    public DrivetrainReal() {

        gyro = new ADXRS453_Gyro();
        angleOffset = 0;

        rightTalon1 = new WPI_TalonSRX(RobotConstants.DRIVETRAIN_RIGHT_1_CANID);
        rightTalon2 = new WPI_TalonSRX(RobotConstants.DRIVETRAIN_RIGHT_2_CANID);
        leftTalon1 = new WPI_TalonSRX(RobotConstants.DRIVETRAIN_LEFT_1_CANID);
        leftTalon2 = new WPI_TalonSRX(RobotConstants.DRIVETRAIN_LEFT_2_CANID);

        rightTalon2.follow(rightTalon1);
        leftTalon2.follow(leftTalon1);
        rightTalon2.setInverted(InvertType.FollowMaster);
        leftTalon2.setInverted(InvertType.FollowMaster);

        leftTalon1.setInverted(true);

        opMode = DrivetrainOpMode.OpenLoop;

        currentR1Sig = new Signal("Drivetrain R1 Motor Current", "A");
        currentR2Sig = new Signal("Drivetrain R2 Motor Current", "A");
        currentL1Sig = new Signal("Drivetrain L1 Motor Current", "A");
        currentL2Sig = new Signal("Drivetrain L2 Motor Current", "A");
        opModeSig    = new Signal("Drivetrain Operation Mode", "Op Mode Enum");
        gyroscopeSig = new Signal("Drivetrain Pos Angle","Deg");
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
    
    public void update() {

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
        currentR2Sig.addSample(sample_time_ms, rightTalon2.getOutputCurrent());
        currentL1Sig.addSample(sample_time_ms, leftTalon1.getOutputCurrent());
        currentL2Sig.addSample(sample_time_ms, leftTalon2.getOutputCurrent());
        opModeSig.addSample(sample_time_ms, opMode.toInt());
        gyroscopeSig.addSample(sample_time_ms, getGyroAngle());

    }
}