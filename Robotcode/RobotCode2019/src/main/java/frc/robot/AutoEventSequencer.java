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

public class AutoEventSequencer {
	// You will want to rename all instances of "EmptyClass" with your actual class name and "empty" with a variable name
	private static AutoEventSequencer empty = null;

	public static synchronized AutoEventSequencer getInstance() {
		if(empty == null)
			empty = new AutoEventSequencer();
		return empty;
	}

	// This is the private constructor that will be called once by getInstance() and it should instantiate anything that will be required by the class
	private AutoEventSequencer() {

    }

    public void setVisionAngleToTgt(double angle_in){

    }
       
    public void setVisionTargetTranslation(double x_in, double y_in){

    }

    public void setVisionTargetSkewAngle(double angle_deg_in){

    }

    public void setAutoAlignCommand(TYPE_TBD cmd_in){

    }

    public void setFrontDistanceMeas(double distance_ft, boolean dist_avail){

    }

    public void setRearDistanceMeas(double distance_ft, boolean dist_avail){

    }

    public void setDrivetrainMeasDist(double right_distance_ft, double left_distance_ft){

    }

    public ArmPosCmd getArmPosCmd(){

    }

    public PEZPosCmd getGripperPosCmd(){

    }

    public boolean getIntakeExtendLockout(){

    }

    public boolean getAutoSequencerActive(){

    }

    public boolean getLeftMotorSpeedCmd_RPM(){

    }

    public boolean getRightMotorSpeedCmd_RPM(){

    }

    public boolean getHeadingCmd_deg(){

    }

    public void update(){

    }
}