package frc.robot.auto;

import java.util.ArrayList;

import frc.lib.AutoSequencer.AutoEvent;
import frc.lib.PathPlanner.FalconPathPlanner;

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

    final double PLANNER_SAMPLE_RATE_S = 0.02; // 20ms update rate
    final double ROBOT_TRACK_WIDTH_FT = 3.5; // 3.5 ft effective track width

    public ArrayList<double[]> waypoints;

    double pathDurationSec = 0;

    FalconPathPlanner path;

    /**
     * Creates a new path from the current robot position up to a point in front of the target, pointed straight at it.
     */
    public AutoSeqPathPlan(double tgt_pos_x_ft, double tgt_pos_y_ft, double tgt_pos_angle_deg){

        waypoints = new ArrayList<double[]>(0);

        double[] wp1 = {0,0};
        waypoints.add(wp1);

        double[] wp2 = {0,0.5};
        waypoints.add(wp2);

        double[] wp3 = {,};
        waypoints.add(wp3);

        double [] wp4 = {,};
        waypoints.add(wp4);

        //TODO add more waypoints based on the final location rquested

        path = new FalconPathPlanner((double[][])waypoints.toArray());
        path.calculate(pathDurationSec, PLANNER_SAMPLE_RATE_S, ROBOT_TRACK_WIDTH_FT);

    }

    /**
     * Returns True if a path could be calculated, false if no valid path was found.
     */
    public boolean getPathAvailable(){
        return ( path.smoothLeftVelocity.length > 1);
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
        int timestep = (int)Math.floor(time_sec/PLANNER_SAMPLE_RATE_S);
        double cmdSpeed = 0;

        if(timestep < path.smoothLeftVelocity.length){
            cmdSpeed = path.smoothLeftVelocity[timestep][1];
        } 

        //TODO Convert ft/sec to RPM
        return 0;
    }


    /**
     * Return the right motor speed command at the specified time (in sec)
     */
    public double getRightSpeedCmdRPM(double time_sec){
        int timestep = (int)Math.floor(time_sec/PLANNER_SAMPLE_RATE_S);
        double cmdSpeed = 0;

        if(timestep < path.smoothRightVelocity.length){
            cmdSpeed = path.smoothRightVelocity[timestep][1];
        } 

        //TODO Convert ft/sec to RPM
        return 0;
    }


    /**
     * Return the heading command, relative to how the gyro returns angles, at the specified time (in sec)
     */
    public double getHeadingCmdRPM(double time_sec){
        int timestep = (int)Math.floor(time_sec/PLANNER_SAMPLE_RATE_S);
        double cmdHeading = 0;

        if(timestep < path.heading.length){
            cmdHeading = path.heading[timestep][1];
        } else {
            cmdHeading = path.heading[path.heading.length-1][1];
        }

        return cmdHeading - 90;
    }

    @Override
    public void userStart() {

    }

    @Override
    public void userUpdate() {

    }

    @Override
    public void userForceStop() {

    }

    @Override
    public boolean isTriggered() {
        return false;
    }

    @Override
    public boolean isDone() {
        return false;
    }

}