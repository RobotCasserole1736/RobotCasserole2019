package frc.robot;

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