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

public class MatchState {

    public enum Period {
        Disabled(0), Autonomous(1), OperatorControl(2), Test(3), Simulation(4);
        public final int value;

        private Period(int value) {
            this.value = value;
        }
    }

    private Period period;

	private static MatchState matchState = null;
	public static synchronized MatchState getInstance() {
		if(matchState == null)
			matchState = new MatchState();
		return matchState;
	}

	private MatchState() {
        period = Period.Disabled;
	}

	public void SetPeriod(Period currentPeriod) {
        period = currentPeriod;
    }
    
    public Period GetPeriod() {
        return period;
    }
}