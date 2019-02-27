package frc.robot;

import java.util.Set;

import edu.wpi.first.wpilibj.PWM;

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

public class LEDController {

    private static LEDController ledCtrl = null;

    PWM ctrl;

    private LEDPatterns patternCmd;

    public static synchronized LEDController getInstance() {
        if(ledCtrl == null)
            ledCtrl = new LEDController();
        return ledCtrl;
    }

    public enum LEDPatterns {
        Pattern0(0), /* "Blank" */ 
        Pattern1(1), /* "Fire" */
        Pattern2(2), /* "Meteor Rain" */
        Pattern3(3), /* "Fade In and Fade Out R&W" */
        Pattern4(4), /* "Running Lights R&W" */
        Pattern5(5), /* "Theatre Chase R&W" */
        Pattern6(6), /* "Strobe: Red" */
        Pattern7(7), /* "Strobe: Green" */
        Pattern8(8), /* "Strobe: Blue" */
        Pattern9(9); /* "Strobe: White" */
     
        public final int value;

        private LEDPatterns(int value) {
            this.value = value;
        }
                
        public int toInt(){
            return this.value;
        }
    }


    

    // This is the private constructor that will be called once by getInstance() and it should instantiate anything that will be required by the class
    private LEDController() {
        
        patternCmd = LEDPatterns.Pattern0;
        ctrl = new PWM(RobotConstants.LED_CONTROLLER_PORT);
    }

    public void update(){
        switch(patternCmd){
            case Pattern0:
                ctrl.setDisabled();

            break;
            case Pattern1:
                ctrl.setSpeed(-1.0);

            break;
            case Pattern2:
                ctrl.setSpeed(-0.25);

            break;
            case Pattern3:
                ctrl.setSpeed(-0.5);

            break;
            case Pattern4:
                ctrl.setSpeed(-0.75);

            break;
            case Pattern5:
                ctrl.setSpeed(0.0);

            break;
            case Pattern6:
                ctrl.setSpeed(0.25);
            
            break;
            case Pattern7:
                ctrl.setSpeed(0.5);
            
            break;
            case Pattern8:
                ctrl.setSpeed(0.75);

            break;
            case Pattern9:
                ctrl.setSpeed(1.0);
        }
    }

    public void setPattern(LEDPatterns pattern_in){
        patternCmd = pattern_in;
    }

}