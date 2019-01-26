package frc.robot.auto;
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

import frc.lib.DataServer.Signal;
import frc.robot.LoopTiming;

public class AutoSeqDistToTgtEst {

    private static AutoSeqDistToTgtEst autoSeqInstance = null;

    //The current estimate of distance to target
    double distanceEst_ft = 0;

    //The vision system's estimate of the distance to the target
    double visionDistance_ft = 0;
    boolean visionAvailable = false;

    //The Ultrasonic Sensor's estimate of distance to the target
    double ultrasonicDistance_ft = 0;
    boolean ultrasonicAvailable = false;

    //The robot's present speed
    double robotLinearVelocity_ftpersec = 0;
    Signal estdist;

    public static synchronized AutoSeqDistToTgtEst getInstance() {
        if(autoSeqInstance == null)
            autoSeqInstance = new AutoSeqDistToTgtEst();
        return autoSeqInstance;
        
    }

    //Constructor
    private AutoSeqDistToTgtEst(){
        //TODO - put init here
        estdist = new Signal ("estimated distance to target", "ft");
    }

    /**
     * Set the current robot linear velocity toward or away from the target
     */
    public void setRobotLinearVelocity(double linearVel_ftpsec){
        robotLinearVelocity_ftpersec = linearVel_ftpsec;
    }

    /**
     * Call this when the current distance is known for sure (ie, line sensor first detects the line). It will force the current estimate to this number.
     */
    public void setDistance(double distance_ft){
        distanceEst_ft = distance_ft;
    }

    public void setVisionDistanceEstimate(double distance_ft, boolean visionAvailable_in){
        visionDistance_ft = distance_ft;
        visionAvailable = visionAvailable_in;
    }

    public void setUltrasonicDistanceEstimate(double distance_ft,  boolean ultraSonicAvailable_in){
        ultrasonicDistance_ft = distance_ft;
        ultrasonicAvailable = ultraSonicAvailable_in;
    }

    public double getEstDistanceFt(){
        return distanceEst_ft; 
    }

    public void update(){
        
        if(ultrasonicAvailable){
            this.setDistance(ultrasonicDistance_ft);
        }
        else if(visionAvailable) {
            this.setDistance(visionDistance_ft);
        }
        else

        {
            //double rev_per_update = ((Drivetrain.getInstance().getLeftWheelSpeedRPM() 
            //+ Drivetrain.getInstance().getRightWheelSpeedRPM())/2)/3000;

           // this.setDistance(rev_per_update*2*RobotConstants.WHEELRADIUS_FT*3.14);
           distanceEst_ft += robotLinearVelocity_ftpersec * 0.02;
        
        } 
        double sample_time_ms = LoopTiming.getInstance().getLoopStartTimeSec()*1000.0;
        estdist.addSample(sample_time_ms, distanceEst_ft);
       
    }

   

}