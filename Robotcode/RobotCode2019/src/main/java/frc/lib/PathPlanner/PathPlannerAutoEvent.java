package frc.lib.PathPlanner;

/*
 *******************************************************************************************
 * Copyright (C) 2017 FRC Team 1736 Robot Casserole - www.robotcasserole.org
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

import frc.lib.AutoSequencer.AutoEvent;
import frc.robot.Drivetrain;
import frc.robot.RobotConstants;
import edu.wpi.first.wpilibj.Timer;

/**
 * Interface into the Casserole autonomous sequencer for a path-planned traversal. Simply wraps
 * path-planner functionality into the AutoEvent abstract class.
 */

public class PathPlannerAutoEvent extends AutoEvent {

    /* Path planner wrapped by this auto event */
    public FalconPathPlanner path;
    private double[][] waypoints;
    private double time_duration_s; 
    boolean pathCalculated = false;
    boolean reversed = false;
    
    boolean done = false;

    private int timestep;
    private double taskRate = 0.02;
    private final double DT_TRACK_WIDTH_FT = 25.0 / 12.0; //Width in Feet
    
    //Special mode for supporting two-cube auto
    // Will lock-in a given heading at the start of the path execution,
    boolean useFixedHeadingMode = false;
    double userManualHeadingDesired = 0;

    
    /**
     * Constructor. Set up the parameters of the planner here.
     * 
     * @param waypoints_in Set of x/y points which define the path the robot should take. In Inches.
     * @param timeAllowed_in Number of seconds the path traversal should take. Must be long enough
     *        to allow the path planner to output realistic speeds.         
     */
    public PathPlannerAutoEvent(double[][] waypoints_in, double timeAllowed_in) { 
    	super();
    	commonConstructor(waypoints_in, timeAllowed_in, false, 0.2, 0.5, 0.01, 0.9);
    }
    
    /**
     * Constructor. Set up the parameters of the planner here.
     * 
     * @param waypoints_in Set of x/y points which define the path the robot should take. Assumes Inches
     * @param timeAllowed_in Number of seconds the path traversal should take. Must be long enough
     *        to allow the path planner to output realistic speeds. 
     * @param reversed set to True if you desire the robot to travel backward through the provided path        
     */
    public PathPlannerAutoEvent(double[][] waypoints_in, double timeAllowed_in, boolean reversed_in) {        
    	super();
    	commonConstructor(waypoints_in, timeAllowed_in, reversed_in, 0.2, 0.5, 0.01, 0.9);

    }
    
    
    /**
     * Constructor. Set up the parameters of the planner here.
     * 
     * @param waypoints_in Set of x/y points which define the path the robot should take. Assumes Inches
     * @param timeAllowed_in Number of seconds the path traversal should take. Must be long enough
     *        to allow the path planner to output realistic speeds. 
     * @param reversed set to True if you desire the robot to travel backward through the provided path        
     */
    public PathPlannerAutoEvent(double[][] waypoints_in, double timeAllowed_in, boolean reversed_in, double alpha, double beta, double valpha, double vbeta) {        
    	super();
    	commonConstructor(waypoints_in, timeAllowed_in, reversed_in, alpha, beta, valpha, vbeta);

    }
    
    private void commonConstructor(double[][] waypoints_in, double timeAllowed_in, boolean reversed_in, double alpha, double beta, double valpha, double vbeta) {
        waypoints = waypoints_in;
        time_duration_s = timeAllowed_in;
        reversed = reversed_in;
        
        if(reversed) {
	        //Reflect all points across the origin. It is expected the user will provide the actual
	        // waypoints to us. We will invert before sending to the pathPlanner (to satisfy its assumptions)
	        // then re-invert as needed before sending to drivetrain.
	        for(int ii = 0; ii < waypoints.length; ii++) {
	        	for(int jj = 0; jj < waypoints[ii].length; jj++) {
	        		waypoints[ii][jj] *= -1;
	        	}
	        }
	   
        }
        
        //Convert all waypoints from inches to ft
        for(int ii = 0; ii < waypoints.length; ii++) {
        	for(int jj = 0; jj < waypoints[ii].length; jj++) {
        		waypoints[ii][jj] *= 1.0/12.0;
        	}
        }
        
        path = new FalconPathPlanner(waypoints);
        pathCalculated = false;
        
        //Default alpha/beta
		path.setPathAlpha(alpha);
		path.setPathBeta(beta);
		path.setVelocityAlpha(valpha);
		path.setVelocityBeta(vbeta);
		
		if (pathCalculated == false) {
            path.calculate(time_duration_s, taskRate, DT_TRACK_WIDTH_FT);
            timestep = 0;
            pathCalculated = true;
		}

    }
    /**
     * On the first loop, calculates velocities needed to take the path specified. Later loops will
     * assign these velocities to the drivetrain at the proper time.
     */
    double startTime = 0;
    public void userUpdate() {
    	double tmp;
        
        //For _when_ loop timing isn't exact 20ms, and we need to skip setpoints,
        // calculate the proper timestep based on FPGA timestamp.
        tmp = (Timer.getFPGATimestamp()-startTime)/taskRate;
        timestep = (int) Math.round(tmp);
        
        if(timestep >= path.numFinalPoints) {
        	timestep = (int) (path.numFinalPoints - 1);
        	done = true;
        }
        
        //Be sure we skip the first timestep. The planner produces a bogus all-zeros point for it
        if (timestep == 0) {
        	timestep = 1;
        }
        
        //Interpret the path planner outputs into commands which are meaningful.
        double leftCommand_RPM  = FT_PER_SEC_TO_RPM(path.smoothLeftVelocity[timestep][1]);
        double rightCommand_RPM = FT_PER_SEC_TO_RPM(path.smoothRightVelocity[timestep][1]);
        double poseCommand_deg = 90.0-path.heading[timestep][1];
        
        if(reversed) {
        	//When running in reversed mode, we need to undo the inversion applied to the 
        	// the waypoints.
        	leftCommand_RPM  *= -1;
        	rightCommand_RPM *= -1;
        	
        	//Note that we don't invert the pose command. Despite the fact that the "heading"
        	// has inverted by 180 degrees (aka robot travels backward), the pose angle remains
        	// forward. Since the path planner assumes we are actually traveling forward, and the 
        	// drivetrain takes in a pose command, we don't need to invert the Pose command before
        	// passing to the drivetrain.
        }
        
        if(useFixedHeadingMode) {
        	Drivetrain.getInstance().setClosedLoopSpeedCmd(leftCommand_RPM,rightCommand_RPM);
        } else {
            Drivetrain.getInstance().setClosedLoopSpeedCmd(leftCommand_RPM,rightCommand_RPM,poseCommand_deg);
        }

        /*
        Drivetrain.getInstance().autoTimestamp = timestep;
        Drivetrain.getInstance().leftAutoCmdFtPerSec = path.smoothLeftVelocity[timestep][1];
        Drivetrain.getInstance().rightAutoCmdFtPerSec = path.smoothRightVelocity[timestep][1];
        */
    }


    /**
     * Force both sides of the drivetrain to zero
     */
    public void userForceStop() {
    	Drivetrain.getInstance().setClosedLoopSpeedCmd(0, 0);
    }


    /**
     * Always returns true, since the routine should run as soon as it comes up in the list.
     */
    public boolean isTriggered() {
        return true; // we're always ready to go
    }


    /**
     * Returns true once we've run the whole path
     */
    public boolean isDone() {
        return done;
    }
    
    /**
     * Manually set what the heading should be - useful if you moved the robot
     * without the pathplanner's knowledge.
     */
    public void setDesiredHeadingOverride(double heading) {
    	userManualHeadingDesired = heading;
    	useFixedHeadingMode = true;
    }


	@Override
	public void userStart() {
		if (pathCalculated == false) {
            path.calculate(time_duration_s, taskRate, DT_TRACK_WIDTH_FT);
            timestep = 0;
            pathCalculated = true;
		}
		
        startTime = Timer.getFPGATimestamp();
        done = false;
	}
	
	private double FT_PER_SEC_TO_RPM(double ftps_in) {
		return ftps_in / (2*Math.PI*RobotConstants.WHEEL_RADIUS_FT) * 60;
	}


}