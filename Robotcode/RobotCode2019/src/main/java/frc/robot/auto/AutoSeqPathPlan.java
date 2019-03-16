package frc.robot.auto;

import java.util.ArrayList;

import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.interfaces.Gyro;
import frc.lib.AutoSequencer.AutoEvent;
import frc.lib.DataServer.Signal;
import frc.lib.PathPlanner.FalconPathPlanner;
import frc.lib.Util.CrashTracker;
import frc.robot.Drivetrain;
import frc.robot.RioSimMode;
import frc.robot.RobotConstants;
import frc.robot.RobotPose;
import frc.robot.Utils;

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

public class AutoSeqPathPlan extends AutoEvent {

    public ArrayList<double[]> waypoints;

    double pathDurationSec = 0;

    Drivetrain dt;

    double startTime = 0;

    double thisLoopTime;

    FalconPathPlanner path;

    double initialHeading = 0;

    final double PATH_DURATION_FROM_DISTANCE_RATIO = 1.0/1.0 ; //units of seconds per foot, because reasons

    //final double JEVOIS_TO_PATH_PLAN_ANGLE_OFFSEST_DEG = 0.0;

    
    /**
     * Creates a new path from the current robot position up to a point in front of the target, pointed straight at it.
     */
    public AutoSeqPathPlan(double tgt_pos_y_ft, double tgt_pos_x_ft, double tgt_pos_angle_rad){
        dt = Drivetrain.getInstance();

        
        //Convert reference frames
        double targetAngleRad = tgt_pos_angle_rad;

        //Use a completely made up formula to calcualte the duration of the path plan event.
        double psuedoDistance = Math.sqrt(tgt_pos_x_ft*tgt_pos_x_ft + tgt_pos_y_ft*tgt_pos_y_ft); //as-the-bird-flies distance to target
        pathDurationSec = psuedoDistance*PATH_DURATION_FROM_DISTANCE_RATIO;

        waypoints = new ArrayList<double[]>(6);

        //This matrix is for the x and y position of the target. It is supposed to be a 2 X 1.
        double [] pointAheadOfEndMatrix = 
            {tgt_pos_x_ft, tgt_pos_y_ft-1.5};

        double [] endOfLineMatrix = 
            {tgt_pos_x_ft, tgt_pos_y_ft};

        CrashTracker.logAndPrint("[AutoSeq Path Plan] Target Position X (ft) = " + tgt_pos_x_ft);
        CrashTracker.logAndPrint("[AutoSeq Path Plan] Target Position Y (ft) = " + tgt_pos_y_ft);

        double [][] rotationMatrix = {
            {java.lang.Math.cos(targetAngleRad), -java.lang.Math.sin(targetAngleRad)},
            {java.lang.Math.sin(targetAngleRad), java.lang.Math.cos(targetAngleRad)}
        };
        double targetAngleDeg=(targetAngleRad*180)/Math.PI;
        CrashTracker.logAndPrint("[AutoSeq Path Plan] Angle from Target (Deg) = " + targetAngleDeg+tgt_pos_angle_rad);

        double [] wayPoint3 = multiplyMatrices(rotationMatrix, pointAheadOfEndMatrix);

        double [] wayPoint4 = multiplyMatrices(rotationMatrix, endOfLineMatrix);

        double[] wp1 = 
            {0, 0};
        waypoints.add(wp1);

        double[] wp2 = 
            {0, 1.5};
        waypoints.add(wp2);

        double[] wp3 = wayPoint3;
        waypoints.add(wp3);
        CrashTracker.logAndPrint("[AutoSeq Path Plan] Waypoint3 X = " + wayPoint3[0]);
        CrashTracker.logAndPrint("[AutoSeq Path Plan] Waypoint3 Y = " + wayPoint3[1]);

        double[] wp4 = wayPoint4;
        waypoints.add(wp4);
        CrashTracker.logAndPrint("[AutoSeq Path Plan] Waypoint4 X = " + wayPoint4[0]);
        CrashTracker.logAndPrint("[AutoSeq Path Plan] Waypoint4 Y = " + wayPoint4[1]);

        //Calculate the smooth path
        path = new FalconPathPlanner(waypoints.toArray(new double[waypoints.size()][2]));
        path.setPathBeta(0.02);
        path.setPathAlpha(0.5);
        path.setVelocityAlpha(0.001);
        path.setVelocityBeta(0.9);
        path.calculate(pathDurationSec, RobotConstants.MAIN_LOOP_SAMPLE_RATE_S, RobotConstants.ROBOT_TRACK_WIDTH_FT);

        if(RioSimMode.getInstance().isDesktop()){
            FalconPathPlanner.plotPath(path);
        }

        //Update the pose view with our new desired location, converting to robotPose reference frame
        RobotPose.getInstance().setDesiredPose(wayPoint4[0], wayPoint4[1], Math.toDegrees(targetAngleRad) + RobotPose.getInstance().INIT_POSE_T);
    }

    /**
     * Returns True if a path could be calculated, false if no valid path was found.
     */
    public boolean getPathAvailable(){
        return (path.smoothLeftVelocity.length > 1);
    }

    /**
     * Get the duration of the path (in seconds). This can be used to determine if the path is completed or not.
     */
    public double getPathDurationSec(){
        return pathDurationSec;
    }


    /**
     * Return the left motor speed command at the specified time (in sec)
     */
    public double getLeftSpeedCmdRPM(double time_sec){
        int timestep = (int)Math.floor(time_sec/RobotConstants.MAIN_LOOP_SAMPLE_RATE_S);
        double cmdSpeed = 0;

        if(timestep < path.smoothLeftVelocity.length){
            cmdSpeed = Utils.FT_PER_SEC_TO_RPM(path.smoothLeftVelocity[timestep][1]);
        } 

        return cmdSpeed;
    }


    /**
     * Return the right motor speed command at the specified time (in sec)
     */
    public double getRightSpeedCmdRPM(double time_sec){
        int timestep = (int)Math.floor(time_sec/RobotConstants.MAIN_LOOP_SAMPLE_RATE_S);
        double cmdSpeed = 0;

        if(timestep < path.smoothRightVelocity.length){
            cmdSpeed = Utils.FT_PER_SEC_TO_RPM(path.smoothRightVelocity[timestep][1]);
        } 

        return cmdSpeed;
    }


    /**
     * Return the heading command, relative to how the gyro returns angles, at the specified time (in sec)
     */
    public double getHeadingCmdDeg(double time_sec){
        int timestep = (int)Math.floor(time_sec/RobotConstants.MAIN_LOOP_SAMPLE_RATE_S);
        double cmdHeading = 0;

        if(timestep < path.heading.length){
            cmdHeading = path.heading[timestep][1];
        } else {
            cmdHeading = path.heading[path.heading.length-1][1];
        }

        return cmdHeading - 90 + initialHeading;
    }

    @Override
    public void userStart() {
        initialHeading = dt.getGyroAngle();
        startTime = Timer.getFPGATimestamp();
    }

    @Override
    public void userUpdate() {
        thisLoopTime = Timer.getFPGATimestamp() - startTime;
        dt.setClosedLoopSpeedCmd(getLeftSpeedCmdRPM(thisLoopTime), getRightSpeedCmdRPM(thisLoopTime),getHeadingCmdDeg(thisLoopTime));
    }

    @Override
    public void userForceStop() {
        dt.setOpenLoopCmd(0, 0);
    }

    @Override
    public boolean isTriggered() {
        return true;
    }

    @Override
    public boolean isDone() {
        return thisLoopTime > (pathDurationSec + 0.1);
    }

    public static double[] multiplyMatrices(double[][] firstMatrix, double[] secondMatrix) {

        int r1 = firstMatrix.length;
        int r2 = secondMatrix.length;
        int c1 = firstMatrix[0].length;
        int c2 = 1;

        double[] product = new double[r1];
        for(int i = 0; i < r1; i++) {
            for (int j = 0; j < c2; j++) {
                for (int k = 0; k < c1; k++) {
                    product[i] += firstMatrix[i][k] * secondMatrix[k];
                }
            }
        }

        return product;
    }

}