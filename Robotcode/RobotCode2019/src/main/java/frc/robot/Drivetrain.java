package frc.robot;

import edu.wpi.first.wpilibj.XboxController;
import edu.wpi.first.wpilibj.GenericHID;
import com.ctre.phoenix.motorcontrol.can.WPI_TalonSRX;

public class Drivetrain {
    private static Drivetrain  dTrain = null;
    
    XboxController driverController;
    double Direction = 0;
    double Turn = 0;
    double motorSpeedLeft = 0;
    double motorSpeedRight = 0;

    WPI_TalonSRX R1;
    WPI_TalonSRX R2;
    WPI_TalonSRX L1;
    WPI_TalonSRX L2;

    public static synchronized Drivetrain getInstance() {
		if ( dTrain == null)
         dTrain = new Drivetrain();
		return dTrain;
	}
    private Drivetrain() {

        R1 = new WPI_TalonSRX(0);
        R2 = new WPI_TalonSRX(1);
        L1 = new WPI_TalonSRX(14);
        L2 = new WPI_TalonSRX(15);

        driverController = new XboxController(0);
    }

    public double getDirectionCMD() {
        double joyVal = Math.pow(driverController.getY(GenericHID.Hand.kLeft),3);
        return joyVal;
     }
     
     public double getTurnCMD() {
        double joyVal = Math.pow(driverController.getX(GenericHID.Hand.kRight),3);
        return joyVal;
     }

     public void setMotorCMD(double command){
        R1.set(-1*command);
        R2.set(-1*command);
        L1.set(command);
        L2.set(command);
        System.out.println("command:" + Double.toString(command));
     }

     public void update() {
        
        double DirectionCMD;
        double TurnCMD;
        double motorSpeedLeftCMD = 0;
        double motorSpeedRightCMD = 0;
  
        DirectionCMD = getDirectionCMD();
        TurnCMD = getTurnCMD();
         
        if(Math.abs(DirectionCMD) < 0.15){
           DirectionCMD = 0;
        }
  
        if(Math.abs(TurnCMD) < 0.15){
           TurnCMD = 0;
        }

        motorSpeedLeftCMD = DirectionCMD - TurnCMD;
        motorSpeedRightCMD = -1 * (DirectionCMD + TurnCMD);
  
        R1.set(motorSpeedRightCMD);
        R2.set(motorSpeedRightCMD);
        L1.set(motorSpeedLeftCMD);
        L2.set(motorSpeedLeftCMD);
    }
}