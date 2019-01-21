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
 *    find it educational and informative! 
 *  Please feel free to snag our software for your own use in whatever project
 *    you have going on right now! We'd love to be able to help out! Shoot us 
 *    any questions you may have, all our contact info should be on our website
 *    (listed above).
 *  If you happen to end up using our software to make money, that is wonderful!
 *    Robot Casserole is always looking for more sponsors, so we'd be very appreciative
 *    if you would consider donating to our club to help further STEM education.
 */

 /**
  * Sim/Real drivetrain switchyard. This exists entirely because I currently cannot get CTRE_PhoenixCCI.dll to load
  *  in WPILib simulation. Per their docs, it's not technically supported yet, which kinda stinks. In the mean time,
  *  The drivetrain is abstracted out and this class is the singleton that selects whether we use the real or simulated
  *  implelmentations (ie with or without CTRE libraries). The simulated implementation has a built-in plant model.
  *
  * NOTE: Any new method added to either drivetrain implementation needs to be added to the other implementation, as well as to
  *       this switchyard.
  */
public class Drivetrain implements DrivetrainInterface {

    private static DrivetrainReal dTrainR = null;
    private static DrivetrainSim  dTrainS = null;
    private static Drivetrain dTrain = null;

    
    public static synchronized Drivetrain getInstance() {
        if (dTrain == null){
                dTrain = new Drivetrain();
            }
        return dTrain;
    }

    private Drivetrain(){
        if(System.getProperty("os.name").contains("Windows")){
            dTrainS = new DrivetrainSim(); //TODO make this work on linux laptops
        } else {
            dTrainR = new DrivetrainReal();
        }
    }

    public void setOpenLoopCmd(double forwardReverseCmd_in, double rotaionCmd_in){
        if(dTrainR != null){
            dTrainR.setOpenLoopCmd(forwardReverseCmd_in, rotaionCmd_in);
        } else {
            dTrainS.setOpenLoopCmd(forwardReverseCmd_in, rotaionCmd_in);
        }

    }

    public void setGyroLockCmd(double forwardReverseCmd_in){
        if(dTrainR != null){
            dTrainR.setGyroLockCmd(forwardReverseCmd_in);
        } else {
            dTrainS.setGyroLockCmd(forwardReverseCmd_in);
        }
    }

    public boolean isGyroOnline(){
        if(dTrainR != null){
            return dTrainR.isGyroOnline();
        } else {
            return dTrainS.isGyroOnline();
        }
    }

    public void update(){
        if(dTrainR != null){
            dTrainR.update();
        } else {
            dTrainS.update();
        }
    }

    public double getLeftWheelSpeedRPM(){
        if(dTrainR != null){
            return dTrainR.getSpeedLeftRPM();
        } else {
            return dTrainS.getSpeedLeftRPM();
        }
    }

    public double getRightWheelSpeedRPM(){
        if(dTrainR != null){
            return dTrainR.getSpeedRightRPM();
        } else {
            return dTrainS.getSpeedRightRPM();
        }
    }



}