package frc.robot.auto;

import frc.lib.PathPlanner.FalconPathPlanner;
import frc.lib.PathPlanner.PathPlannerAutoEvent;

import frc.lib.AutoSequencer.AutoEvent;

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

public class Backup extends AutoEvent {
	
	Backup() {
        driveBackward = new PathPlannerAutoEvent(waypoints, time, true, 0.2, 0.5, 0.001, 0.9);
    }

    PathPlannerAutoEvent driveBackward;

	private final double[][] waypoints = new double[][] {
		{0,0},
		{0,-50}
	};
	
	private final double time = 1.5;

	@Override
	public void userUpdate() {
		driveBackward.userUpdate();
		// shotCTRL.setDesiredShooterState(ShooterStates.PREP_TO_SHOOT);
	}

	@Override
	public void userForceStop() {
		driveBackward.userForceStop();
	}

	@Override
	public boolean isTriggered() {
		return driveBackward.isTriggered();
	}

	@Override
	public boolean isDone() {
		return driveBackward.isDone();
	}

	@Override
	public void userStart() {
		driveBackward.userStart();
	}
    public static void main(String[] args) {
    	Backup autoEvent = new Backup();
		FalconPathPlanner.plotPath(autoEvent.driveBackward.path);
	}
}