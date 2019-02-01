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
import frc.lib.Calibration.Calibration;
import frc.lib.DataServer.Signal;

public class LineFollower {

    final int NUM_SENSORS = 5;

    double forwardReverseCmd;
    double rotationCmd;
    boolean[] sensorStates = {false, false, false, false, false};
    boolean[] sensorStatesPrev = {false, false, false, false, false};

    double estPosFt = 0;
    boolean estPosAvailable = false;

    final double[] SENSOR_POS_FT = {0.5, 0.25, 0.0, -0.25, -0.5};

    Signal estPosSig;
    Signal estPosAvailSig;
    Signal measPosSig;
    Signal measPosAvailSig;

    Calibration rotGain_P;
    
    DigitalInput digitalInput1;
    DigitalInput digitalInput2;
    DigitalInput digitalInput3;
    DigitalInput digitalInput4;
    DigitalInput digitalInput5;

    /* Singleton stuff */
    private static LineFollower lnFlwr = null;
    public static synchronized LineFollower getInstance() {
        if(lnFlwr == null) lnFlwr = new LineFollower();
        return lnFlwr;
    }

    private LineFollower(){
        digitalInput1 = new DigitalInput(RobotConstants.LINE_FOLLOWING_SENSOR_1_PORT);
        digitalInput2 = new DigitalInput(RobotConstants.LINE_FOLLOWING_SENSOR_2_PORT);
        digitalInput3 = new DigitalInput(RobotConstants.LINE_FOLLOWING_SENSOR_3_PORT);
        digitalInput4 = new DigitalInput(RobotConstants.LINE_FOLLOWING_SENSOR_4_PORT);
        digitalInput5 = new DigitalInput(RobotConstants.LINE_FOLLOWING_SENSOR_5_PORT);
        
        estPosSig       = new Signal("Line Follower Estimated Position", "ft");
        estPosAvailSig  = new Signal("Line Follower Estimated Position Available", "bool");
        measPosSig      = new Signal("Line Follower Measured Position", "ft");
        measPosAvailSig = new Signal("Line Follower Measured Position Available", "bool");

        rotGain_P = new Calibration("Line Follower Rotation P Gain", 0.0);
    }

    public double getForwardCmd() {
        return forwardReverseCmd;
    }

    public double getRotationCmd(){
        return rotationCmd;
    }

    public double getEstLinePosFt(){
        return estPosFt;
    }

    public boolean isEstLinePosAvailable(){
        return estPosAvailable;
    }

    public void update() {
        sensorStatesPrev = sensorStates;
        sensorStates[0] = digitalInput1.get();
        sensorStates[1] = digitalInput2.get();
        sensorStates[2] = digitalInput3.get();
        sensorStates[3] = digitalInput4.get();
        sensorStates[4] = digitalInput5.get();

        //Based on line position sensors, genreate a "measurement" of where we think the line is
        int numSensorsSeeingLine = 0;
        double measPos = 0;
        boolean measPosAvailable = false;
        for(int i = 0; i < NUM_SENSORS; i++){
            if(sensorStates[i]){
                numSensorsSeeingLine++;
                measPos += SENSOR_POS_FT[i];
            }
        }

        if(numSensorsSeeingLine > 0){
            measPos /= numSensorsSeeingLine;
            measPosAvailable = true;
        } else {
            measPosAvailable = false;
        }


        //Incorporate the measuremnet (if available) into our estimate of where the line is at.
        //TODO: Maybe a more complex translation from measurements to estimate?
        if(measPosAvailable){
            estPosFt = measPos;
            estPosAvailable = true;
        } else {
            estPosAvailable = false;
        }
        
        
        double sample_time_ms = LoopTiming.getInstance().getLoopStartTimeSec()*1000.0;
        estPosSig.addSample(sample_time_ms, estPosFt);
        estPosAvailSig.addSample(sample_time_ms, estPosAvailable?1.0:0.0);
        measPosSig.addSample(sample_time_ms, measPos);
        measPosAvailSig.addSample(sample_time_ms, measPosAvailable?1.0:0.0);
    }

    public void updateController(){
        //Simple P controller to rotation command
        if(estPosAvailable){
            rotationCmd = rotGain_P.get()*estPosFt;
        } else {
            rotationCmd = 0;
        }
    }
    
}