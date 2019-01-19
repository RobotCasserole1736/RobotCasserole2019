package frc.robot;


import edu.wpi.first.wpilibj.AnalogInput;
import frc.lib.DataServer.Signal;
import frc.lib.SignalMath.AveragingFilter;


public class Ultrasonic {

    AnalogInput inAnalog;

    double distance_ft = 0;
    boolean distanceAvailable = false;

    Signal sensorDistanceSig;

    AveragingFilter filt;
    
    final double FEET_PER_VOLT = 1.0;

    public Ultrasonic(int pin_in, String name_in){
        inAnalog = new AnalogInput(pin_in);
        sensorDistanceSig = new Signal(name_in + " Sensor Distance", "ft");
        filt = new AveragingFilter(3,0);
    }

    public void update(){
        distance_ft = filt.filter(inAnalog.getVoltage() * FEET_PER_VOLT);

        double sample_time_ms = LoopTiming.getInstance().getLoopStartTime_sec() * 1000;
        sensorDistanceSig.addSample(sample_time_ms, distance_ft);
    }

    public double getDistanceFt(){
        return distance_ft;
    }

    public boolean getDistanceAvailable(){
        return distanceAvailable;
    }

}