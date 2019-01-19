package frc.robot;

import edu.wpi.first.wpilibj.DigitalOutput;

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

    private DigitalOutput out0;
    private DigitalOutput out1;

    private LEDPatterns patternCmd;

    public static synchronized LEDController getInstance() {
        if(ledCtrl == null)
            ledCtrl = new LEDController();
        return ledCtrl;
    }

    public enum LEDPatterns {
        Pattern1(0), /* TODO- what pattern is this? */
        Pattern2(1), /* TODO- what pattern is this? */
        Pattern3(2), /* TODO- what pattern is this? */
        Pattern4(3); /* TODO- what pattern is this? */

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
        out0 = new DigitalOutput(RobotConstants.LED_PATTERN_OUTPUT_0);
        out1 = new DigitalOutput(RobotConstants.LED_PATTERN_OUTPUT_1);
        patternCmd = LEDPatterns.Pattern1;
    }

    public void update(){
        switch(patternCmd){
            case Pattern1:
                out0.set(false);
                out1.set(false);
            break;
            case Pattern2:
                out0.set(true);
                out1.set(false);
            break;
            case Pattern3:
                out0.set(false);
                out1.set(true);
            break;
            case Pattern4:
                out0.set(true);
                out1.set(true);
            break;
        }
    }

    public void setPattern(LEDPatterns pattern_in){
        patternCmd = pattern_in;
    }

}