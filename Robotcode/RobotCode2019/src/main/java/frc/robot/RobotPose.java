package frc.robot;

import java.awt.geom.Point2D;

import frc.lib.DataServer.Signal;


public class RobotPose {

	public double leftVelosity_RPM;
	public double rightVelosity_RPM;
	public final double wheelRadius_Ft = 0.24;
	public final double robotRadius_Ft  = 0.9;
	public double poseX = 0;
	public double poseY = 0;
	public double poseThaddeus = 90;
	public double velosityX = 0;
	public double velosityY = 0;

	Signal DesX;
	Signal DesY;
	Signal DesT;
	Signal ActX;
	Signal ActY;
	Signal ActT;

	public RobotPose() {
		DesX = new Signal("botDesPoseX", "in");
		DesY = new Signal("botDesPoseY", "in");
		DesT = new Signal("botDesPoseT", "deg");
		ActX = new Signal("botActPoseX", "in");
		ActY = new Signal("botActPoseY", "in");
		ActT = new Signal("botActPoseT", "deg");
	}

	public void setLeftMotorSpeed(double speed) {
		leftVelosity_RPM = speed;
	}

	public void setRightMotorSpeed(double speed){
		rightVelosity_RPM = speed;
	}
	
	public double getRobotVelocity_ftpersec(){
		return velosityX;
	}
	
	public void update() {
		double leftVelosity_FPS = leftVelosity_RPM * (2*3.14*wheelRadius_Ft / 60);
		double rightVelosity_FPS = rightVelosity_RPM * (2*3.14*wheelRadius_Ft / 60);
		double robotAngle_DPS = ((rightVelosity_FPS-leftVelosity_FPS)/(2*robotRadius_Ft) * 180/3.14);
		double X_dot = (rightVelosity_FPS+leftVelosity_FPS)/2; 
		
		velosityX = 0.02 * (X_dot*Math.cos(poseThaddeus*(3.14/180)));
		velosityY = 0.02 * (X_dot*Math.sin(poseThaddeus*(3.14/180)));
		
		if(poseY < 0) { 
			velosityY = 0;
			velosityX = 0;
			
		}
		if(poseY > 54){
			velosityY = 0;
			velosityX = 0;
			
		}
		if(poseX < -13.47){
			velosityX = 0;
			velosityY = 0;
		}
		if(poseX > 13.47){
			velosityX = 0;
			velosityY = 0;
		}
		
		poseX += velosityX;
		poseY += velosityY;
		poseThaddeus += 0.02 * robotAngle_DPS;
		//CasseroleRobotPoseView.setRobotPose(poseX, poseY, poseTheta - 90);

		double sample_time_ms = LoopTiming.getInstance().getLoopStartTime_sec()*1000.0;
		DesX.addSample(sample_time_ms,0);
		DesY.addSample(sample_time_ms,0);
		DesT.addSample(sample_time_ms,0);
		ActX.addSample(sample_time_ms,poseX);
		ActY.addSample(sample_time_ms,poseY);
		ActT.addSample(sample_time_ms,poseThaddeus);
		}
	
	public void reset() {
		poseX = 0;
		poseY = 0;
		poseThaddeus = 90;
		leftVelosity_RPM = 0;
		rightVelosity_RPM = 0;
	}
	
}
