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
 *    find it educational and informative! 
 *  Please feel free to snag our software for your own use in whatever project
 *    you have going on right now! We'd love to be able to help out! Shoot us 
 *    any questions you may have, all our contact info should be on our website
 *    (listed above).
 *  If you happen to end up using our software to make money, that is wonderful!
 *    Robot Casserole is always looking for more sponsors, so we'd be very appreciative
 *    if you would consider donating to our club to help further STEM education.
 */

public interface DrivetrainInterface {

    public enum DrivetrainOpMode {
        OpenLoop(0),   /* A command for fwd/rev and rotation in units of motor-command comes from the outside world */ 
        GyroLock(1),   /* A fwd/rev command from the outside world is used, but rotation command is servoed by a PID to keep the pose angle locked */
        ClosedLoop(2), /* Wheel speed commands (in RPM) are commanded from the outside world. Gyro is not used. */
        ClosedLoopWithGyro(3); /* Both wheel speed commands (in RPM) and a desird gyro heading are commanded from the outside world. */

        public final int value;

        private DrivetrainOpMode(int value) {
            this.value = value;
        }

        public int toInt() {
            return this.value;
        }
    }

    public void setOpenLoopCmd(double forwardReverseCmd, double rotaionCmd);

    public void setGyroLockCmd(double forwardReverseCmd);

    public void update();

    public boolean isGyroOnline();

    public double getLeftWheelSpeedRPM();

    public double getRightWheelSpeedRPM();

    public void updateGains(boolean force);

    public void setClosedLoopSpeedCmd(double leftCmdRPM, double rightCmdRPM);

    public void setClosedLoopSpeedCmd(double leftCmdRPM, double rightCmdRPM, double headingCmdDeg);
}