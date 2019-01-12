package frc.robot;

import edu.wpi.first.wpilibj.XboxController;
import edu.wpi.first.wpilibj.GenericHID;
import com.ctre.phoenix.motorcontrol.can.WPI_TalonSRX;

public class Drivetrain {
   private static Drivetrain dTrain = null;
   
   XboxController driverController;

   WPI_TalonSRX rightTalon1;
   WPI_TalonSRX rightTalon2;
   WPI_TalonSRX leftTalon1;
   WPI_TalonSRX leftTalon2;

   public static synchronized Drivetrain getInstance() {
	  if ( dTrain == null)
       dTrain = new Drivetrain();
     return dTrain;
   }
   
   private Drivetrain() {
     rightTalon1 = new WPI_TalonSRX(RobotConstants.DRIVETRAIN_RIGHT_1_CANID);
     rightTalon2 = new WPI_TalonSRX(RobotConstants.DRIVETRAIN_RIGHT_2_CANID);
     leftTalon1 = new WPI_TalonSRX(RobotConstants.DRIVETRAIN_LEFT_1_CANID);
     leftTalon2 = new WPI_TalonSRX(RobotConstants.DRIVETRAIN_LEFT_2_CANID);

     driverController = new XboxController(0);
   }

   public double getdirectionCMD() {
     double joyVal = Math.pow(driverController.getY(GenericHID.Hand.kLeft),3);
     return joyVal;
   }
    
   public double getturnCMD() {
     double joyVal = Math.pow(driverController.getX(GenericHID.Hand.kRight),3);
     return joyVal;
   }

   public void setMotorCMD(double command){
     rightTalon1.set(-1*command);
     rightTalon2.set(-1*command);
     leftTalon1.set(command);
     leftTalon2.set(command);
     //System.out.println("command:" + Double.toString(command));
   }

    public void update() {
      double directionCMD;
      double turnCMD;
      double motorSpeedLeftCMD = 0;
      double motorSpeedRightCMD = 0;
  
      directionCMD = getdirectionCMD();
      turnCMD = getturnCMD();
        
      if(Math.abs(directionCMD) < 0.15){
         directionCMD = 0;
      }
  
      if(Math.abs(turnCMD) < 0.15){
         turnCMD = 0;
      }

      motorSpeedLeftCMD = directionCMD - turnCMD;
      motorSpeedRightCMD = -1 * (directionCMD + turnCMD);
  
      rightTalon1.set(motorSpeedRightCMD);
      rightTalon2.set(motorSpeedRightCMD);
      leftTalon1.set(motorSpeedLeftCMD);
      leftTalon2.set(motorSpeedLeftCMD);
   }
}