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

    public double DesRightRPM;
    public double DesLeftRPM;
    public double ActRightRPM;
    public double ActLeftRPM;

    Signal ActualRightSimRPM;
    Signal ActualLeftSimRPM;


    public DrivetrainSim() {
        ActualRightSimRPM = new Signal("Drivetrain Sim Right Speed", "RPM");
        ActualLeftSimRPM = new Signal("Drivetrain Sim Left Speed", "RPM");
    }

    public void setOpenLoopCmd(double forwardReverseCmd, double rotaionCmd) {
        opModeCmd = DrivetrainOpMode.OpenLoop;
    }

    public boolean isGyroOnline(){
        return false;
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
        } else if (opModeCmd == DrivetrainOpMode.OpenLoop){
            if(ActLeftRPM < DesLeftRPM){
                ActLeftRPM++;
            } else if (ActLeftRPM > DesLeftRPM){
                ActLeftRPM--;
            }
            
            if(ActRightRPM < DesRightRPM){
                ActRightRPM++;
            } else if (ActRightRPM > DesRightRPM){
                ActRightRPM--;
            }
        }

        double sampleTimeMs = LoopTiming.getInstance().getLoopStartTimeSec() * 1000.0;

        ActualLeftSimRPM.addSample(sampleTimeMs, getLeftWheelSpeedRPM());
        ActualRightSimRPM.addSample(sampleTimeMs, getRightWheelSpeedRPM());
    }

    public double getLeftWheelSpeedRPM() {
        return ActLeftRPM;
    }

    public double getRightWheelSpeedRPM() {
        return ActRightRPM;
    } 

    public void updateGains(boolean force) {
        //TODO
    }

    public void setClosedLoopSpeedCmd(double leftCmdRPM, double rightCmdRPM, double headingCmdDeg) {
        opModeCmd = DrivetrainOpMode.ClosedLoopWithGyro;
        DesRightRPM = rightCmdRPM;
        DesLeftRPM = leftCmdRPM;
    }

    @Override
    public void setClosedLoopSpeedCmd(double leftCmdRPM, double rightCmdRPM) {
        opModeCmd = DrivetrainOpMode.ClosedLoop;
        DesRightRPM = rightCmdRPM;
        DesLeftRPM = leftCmdRPM;
    }

}