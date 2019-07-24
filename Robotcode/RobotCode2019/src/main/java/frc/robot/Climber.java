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

    

    Solenoid climbLeftCyl;
    Solenoid climbRightCyl;

    /* Singleton stuff */
    private static Climber climbCtrl = null;
    
    public static synchronized Climber getInstance() {
        if(climbCtrl == null) climbCtrl = new Climber();
        return climbCtrl;
    }

    private Climber(){
        
        climbLeftCyl= new Solenoid(RobotConstants.CLIMBER_LEFT_CYL);
        //climbRightCyl = new Solenoid(RobotConstants.CLIMBER_RIGHT_CYL);

    }


    public void update(){
        boolean enable = OperatorController.getInstance().getClimberEnable();
        boolean release = OperatorController.getInstance().getClimberReleace();
        
                if(enable && release){
                    climbLeftCyl.set(true);
                    //climbRightCyl.set(true);
                } else {
                    climbLeftCyl.set(false);
                    //climbRightCyl.set(false);
                }
           
        }
       
    }

