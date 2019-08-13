package frc.robot;

import com.ctre.phoenix.motorcontrol.can.WPI_TalonSRX;

import edu.wpi.first.wpilibj.PWM;
import edu.wpi.first.wpilibj.Servo;
import edu.wpi.first.wpilibj.Solenoid;
import edu.wpi.first.wpilibj.VictorSP;
import frc.lib.Calibration.Calibration;

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


public class Climber {

    Servo climbServo1;
    Servo climbServo2;   
    Solenoid climbEjectSol;

    Calibration servo1LockedCal;
    Calibration servo1UnLockedCal;
    Calibration servo2LockedCal;
    Calibration servo2UnLockedCal;
    boolean manualMode = false;

    /* Singleton stuff */
    private static Climber climbCtrl = null;
    
    public static synchronized Climber getInstance() {
        if(climbCtrl == null) climbCtrl = new Climber();
        return climbCtrl;
    }

    private Climber(){
        climbServo1 = new Servo(RobotConstants.CLIMBER_SERVO1);
        climbServo2 = new Servo(RobotConstants.CLIMBER_SERVO2);
        climbEjectSol = new Solenoid(RobotConstants.CLIMBER_EJECT_SOL);
        servo1LockedCal = new Calibration("Servo 1 Locked Angle DEG", 0.8);
        servo1UnLockedCal = new Calibration("Servo 1 Unlocked Angle DEG", 0.2);
        servo2LockedCal = new Calibration("Servo 2 Locked Angle DEG", 0);
        servo2UnLockedCal = new Calibration("Servo 2 Unlocked Angle DEG", 0.8);

    }

    public void setManualMovement(boolean manualCmd){
        manualMode=manualCmd;
    }

    public void update(){
        boolean enable = OperatorController.getInstance().getClimberEnable();
        boolean release = OperatorController.getInstance().getClimberReleace();
        if(manualMode){
            if(enable){
                climbServo1.set(servo1UnLockedCal.get());
                climbServo2.set(servo2UnLockedCal.get());
                if(release){
                    climbEjectSol.set(true);
                } else {
                    climbEjectSol.set(false);
                }
            } else {
                climbServo1.set(servo1LockedCal.get());
                climbServo2.set(servo2LockedCal.get());
                climbEjectSol.set(false);
            }

        }else{
            if(enable && release){
                climbServo1.set(servo1UnLockedCal.get());
                climbServo2.set(servo2UnLockedCal.get());
                climbEjectSol.set(true);

            }else {
                climbServo1.set(servo1LockedCal.get());
                climbServo2.set(servo2LockedCal.get());
                climbEjectSol.set(false);
            }
        }
       
    }

}