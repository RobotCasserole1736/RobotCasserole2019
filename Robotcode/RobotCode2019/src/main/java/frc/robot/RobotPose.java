package frc.robot;

import java.awt.geom.Point2D;


public class RobotPose {

	public double leftVelosity_RPM;
	public double rightVelosity_RPM;
	public final double wheelRadius_Ft = 0.24;
	public final double robotRadius_Ft  = 0.9;
	public double poseX = 0;
	public double poseY = 0;
	public double poseTheta = 90;
	public double velosityX = 0;
	public double velosityY = 0;
	
	public void setLeftMotorSpeed(double speed) {
		leftVelosity_RPM = speed;
	}

	public void setRightMotorSpeed(double speed){
		rightVelosity_RPM = speed;
    }
	
	public void update() {
		double leftVelosity_FPS = leftVelosity_RPM * (2*3.14*wheelRadius_Ft / 60);
		double rightVelosity_FPS = rightVelosity_RPM * (2*3.14*wheelRadius_Ft / 60);
		double robotAngle_DPS = ((rightVelosity_FPS-leftVelosity_FPS)/(2*robotRadius_Ft) * 180/3.14);
		double X_dot = (rightVelosity_FPS+leftVelosity_FPS)/2; 
		
		velosityX = 0.02 * (X_dot*Math.cos(poseTheta*(3.14/180)));
		velosityY = 0.02 * (X_dot*Math.sin(poseTheta*(3.14/180)));
		
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
		poseTheta += 0.02 * robotAngle_DPS;
		//CasseroleRobotPoseView.setRobotPose(poseX, poseY, poseTheta - 90);
	
		System.out.println("x");
		System.out.println(poseX);
		System.out.println("y");
		System.out.println(poseY);
		System.out.println("Thadus");
		System.out.println(poseTheta);
		}
	
	public void reset() {
		poseX = 0;
		poseY = 0;
		poseTheta = 90;
		leftVelosity_RPM = 0;
		rightVelosity_RPM = 0;
	}
	
}
