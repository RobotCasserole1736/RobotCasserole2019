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

public class Utils {

    public static double ctrlAxisScale(double input, double exp_scale, double deadzone){
        boolean inputIsNegative = false;
        double output = 0;

        // Track if the input was less than zero
        if(input < 0){
            inputIsNegative = true;
            input *= -1;
        }

        if(input < deadzone){
            //Input within deadzone range, set output to zero.
            output = 0;
        } else{
            //Input is outside deadzon range, calcualte output

            //Raise input to desired power
            input = Math.pow(input, exp_scale);
            
            output = input;
        }

        

        // re-apply input sign
        if(inputIsNegative){
            output *= -1;
        }

        return output;
    }



    public static double RPM_TO_FT_PER_SEC(double rpm_in){
        return 2*Math.PI*RobotConstants.WHEEL_RADIUS_FT / 60 * rpm_in;
    }

    public static double FT_PER_SEC_TO_RPM(double fps_in){
        return fps_in / (2*Math.PI*RobotConstants.WHEEL_RADIUS_FT / 60) ;
    }

    public static double capMotorCmd(double in){
        if(in > 1.0){
            return 1.0;
        } else if (in < -1.0) {
            return -1.0;
        } else {
            return in;
        }
    }
}