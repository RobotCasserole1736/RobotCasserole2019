package frc.robot;

import frc.lib.DataServer.Signal;

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


public class DrivetrainSim implements DrivetrainInterface {

    DrivetrainOpMode opModeCmd;
    DrivetrainOpMode opMode;
    DrivetrainOpMode prevOpMode;

    double DesRightRPM;
    double DesLeftRPM;
    double ActRightRPM;
    double ActLeftRPM;

    double desPoseAngle = RobotPose.getInstance().INIT_POSE_T;
    double actPoseAngle = RobotPose.getInstance().INIT_POSE_T;
    boolean headingAvailable = false;

    Signal ActualRightSimRPM;
    Signal ActualLeftSimRPM;
    Signal DesiredRightSimRPM;
    Signal DesiredLeftSimRPM;

    final double DT_MAX_SPEED_FT_PER_SEC = 15.0;
    final double DT_MAX_ACCEL_FT_PER_SEC_PER_SEC = 8.0;


    public DrivetrainSim() {
        ActualRightSimRPM = new Signal("Drivetrain Sim Actual Right Speed", "RPM");
        ActualLeftSimRPM = new Signal("Drivetrain Sim Actual Left Speed", "RPM");
        DesiredRightSimRPM = new Signal("Drivetrain Sim Desired Right Speed", "RPM");
        DesiredLeftSimRPM = new Signal("Drivetrain Sim Desired Left Speed", "RPM");
    }

    public void setOpenLoopCmd(double forwardReverseCmd, double rotationCmd) {
        opModeCmd = DrivetrainOpMode.OpenLoop;

        double motorSpeedLeftCMD = Utils.capMotorCmd(forwardReverseCmd + rotationCmd);
        double motorSpeedRightCMD = Utils.capMotorCmd(forwardReverseCmd - rotationCmd);

        DesLeftRPM = Utils.FT_PER_SEC_TO_RPM(DT_MAX_SPEED_FT_PER_SEC)*motorSpeedLeftCMD;
        DesRightRPM = Utils.FT_PER_SEC_TO_RPM(DT_MAX_SPEED_FT_PER_SEC)*motorSpeedRightCMD;
    }

    public void setGyroLockCmd(double forwardReverseCmd) {
        opModeCmd = DrivetrainOpMode.GyroLock;
    }

    public void update() {

        prevOpMode = opMode;
        opMode = opModeCmd;

        if(opModeCmd == DrivetrainOpMode.ClosedLoop || opModeCmd == DrivetrainOpMode.ClosedLoopWithGyro){
            //Asssume perfect drivetrain closed loop.
            ActLeftRPM = DesLeftRPM;
            ActRightRPM = DesRightRPM;
            if(opModeCmd == DrivetrainOpMode.ClosedLoopWithGyro){
                headingAvailable = true;
                actPoseAngle = desPoseAngle;
            } else {
                headingAvailable = false;
                actPoseAngle = RobotPose.getInstance().getRobotPoseAngleDeg();
            }
        } else if (opModeCmd == DrivetrainOpMode.OpenLoop){

            ActLeftRPM = simMotor(ActLeftRPM, DesLeftRPM);
            ActRightRPM = simMotor(ActRightRPM, DesRightRPM);
            headingAvailable = false;
            actPoseAngle = RobotPose.getInstance().getRobotPoseAngleDeg();
        } else if(opModeCmd == DrivetrainOpMode.GyroLock) {
            //TODO
        }

        double sampleTimeMs = LoopTiming.getInstance().getLoopStartTimeSec() * 1000.0;

        ActualLeftSimRPM.addSample(sampleTimeMs, ActLeftRPM);
        ActualRightSimRPM.addSample(sampleTimeMs, ActRightRPM);
        DesiredLeftSimRPM.addSample(sampleTimeMs, DesLeftRPM);
        DesiredRightSimRPM.addSample(sampleTimeMs, DesRightRPM);
    }

    public double getLeftWheelSpeedRPM() {
        return ActLeftRPM;
    }

    public double getRightWheelSpeedRPM() {
        return ActRightRPM;
    } 

    public void updateGains(boolean force) {
        // No one here but us chickens
    }

    public void setClosedLoopSpeedCmd(double leftCmdRPM, double rightCmdRPM, double headingCmdDeg) {
        opModeCmd = DrivetrainOpMode.ClosedLoopWithGyro;
        DesRightRPM = rightCmdRPM;
        DesLeftRPM = leftCmdRPM;
        desPoseAngle = headingCmdDeg;
    }

    @Override
    public void setClosedLoopSpeedCmd(double leftCmdRPM, double rightCmdRPM) {
        opModeCmd = DrivetrainOpMode.ClosedLoop;
        DesRightRPM = rightCmdRPM;
        DesLeftRPM = leftCmdRPM;
    }

    /* A crude aproximation of how a motor and gearbox and wheel behaves */
    private double simMotor(double actSpeedRPM, double desSpeedRPM){

        double accelFactor = Utils.FT_PER_SEC_TO_RPM( DT_MAX_ACCEL_FT_PER_SEC_PER_SEC * 0.02);
        double maxSpd = Utils.FT_PER_SEC_TO_RPM(DT_MAX_SPEED_FT_PER_SEC);

        double delta = actSpeedRPM - desSpeedRPM ;

        if(Math.abs(desSpeedRPM) < 10){
            actSpeedRPM *= 0.90; // Static-ish Frictional constant
        } else {
            actSpeedRPM *= 0.98; // Frictional constant
        }


        if(delta < 0){
            //Accelerate
            actSpeedRPM += 1/accelFactor * Math.abs(delta);
        } else if (delta > 0){
            //Decellerate
            actSpeedRPM -= 1/accelFactor * Math.abs(delta);
        } else {
            //Cruse at constant speed
        }

        //Cap at absolute min/max
        if(actSpeedRPM > maxSpd){
            actSpeedRPM = maxSpd;
        } else if(actSpeedRPM < -1.0*maxSpd) {
            actSpeedRPM = -1.0*maxSpd;
        }

        return actSpeedRPM;


    }

    @Override
    public double getGyroAngle() {
        return actPoseAngle;
    }

    @Override
    public boolean isGyroOnline(){
        return headingAvailable;
    }

    @Override
    public double getLeftTalon1Current() {
        return 0;
    }

    @Override
    public double getLeftTalon2Current() {
        return 0;
    }

    @Override
    public double getRightTalon1Current() {
        return 0;
    }

    @Override
    public double getRightTalon2Current() {
        return 0;
    }

    @Override
    public void setPositionCmd(double forwardReverseCmd, double angleError) {
        //Not yet implemented
    }

}
