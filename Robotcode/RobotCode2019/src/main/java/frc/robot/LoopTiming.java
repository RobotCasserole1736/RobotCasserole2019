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

import edu.wpi.first.wpilibj.Timer;
import frc.lib.DataServer.Signal;

class LoopTiming{

    double loopStartTime_sec;
    double loopEndTime_sec;

    double prevLoopStartTime_sec;
    double prevLoopEndTime_sec;

    double loopDelta_sec;

    Signal loopDuration_sec;
    Signal loopPeriod_sec;

    /* Singleton stuff */
    private static LoopTiming loopTiming = null;
	public static synchronized LoopTiming getInstance() {
		if(loopTiming == null) loopTiming = new LoopTiming();
		return loopTiming;
	}

    private LoopTiming(){
        loopDuration_sec = new Signal("Main Loop Process Duration", "sec");
        loopPeriod_sec = new Signal("Main Loop Call Period", "sec");
    }

    public void markLoopStart(){
        prevLoopStartTime_sec =loopStartTime_sec;
        loopStartTime_sec = Timer.getFPGATimestamp();
        loopDuration_sec.addSample(prevLoopStartTime_sec, prevLoopEndTime_sec - prevLoopStartTime_sec);
        loopPeriod_sec.addSample(loopStartTime_sec, loopStartTime_sec - prevLoopStartTime_sec);
    }

    public void markLoopEnd(){
        prevLoopEndTime_sec =loopEndTime_sec;
        loopEndTime_sec = Timer.getFPGATimestamp();
    }

    public double getLoopStartTime_sec(){
        return loopStartTime_sec;
    }

}