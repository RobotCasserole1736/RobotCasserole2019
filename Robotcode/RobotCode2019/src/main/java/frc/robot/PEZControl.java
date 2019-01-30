package frc.robot;

import edu.wpi.first.wpilibj.DigitalInput;
import edu.wpi.first.wpilibj.DoubleSolenoid;
import edu.wpi.first.wpilibj.Solenoid;
import edu.wpi.first.wpilibj.Timer;
import frc.lib.WebServer.CasseroleDriverView;

/*
 *******************************************************************************************
 * Copyright (C) 2019 FRC Team 1736 Robot Casserole - www.robotcasserole.org
 ***
 ****************************************************************************************
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

public class PEZControl {
    
    private static PEZControl pezCtrl = null;

    Solenoid pezSolenoid; 
    Solenoid electricSolenoid;

    Timer solenoidTimer;

    DriverController dController;
    OperatorController opController;

    GamePiece curGamePiece;

    PEZPos posReq;
    PEZPos prevPosReq; 

    double retractTimeStart; 

    DigitalInput limitSwitch;


    public static synchronized PEZControl getInstance() {
        if(pezCtrl == null)
            pezCtrl = new PEZControl();
        return pezCtrl;
    }

    public enum PEZPos {
        CargoGrab(0), Release(1), HatchGrab(2), None(3);
        public final int value;

        private PEZPos(int value) {
            this.value = value;
        }
    }

    public enum GamePiece {
        Nothing(0), Cargo(1), Hatch(2);
        public final int value;

        private GamePiece(int value) {
            this.value = value;
        }
    }

    // This is the private constructor that will be called once by getInstance() and it should instantiate anything that will be required by the class
    private  PEZControl() {
        pezSolenoid = new Solenoid (RobotConstants.PEZ_SOLENOID_CHANNEL);
        electricSolenoid = new Solenoid(RobotConstants.ELECTRIC_SOLENOID_CHANNEL);
        dController = DriverController.getInstance();
        opController = OperatorController.getInstance();
        solenoidTimer = new Timer();
        limitSwitch = new DigitalInput(2); // we'll find out real port later ;) //

        solenoidTimer.start();
    }
    private boolean getPezSolenoidState(){ 
        boolean  extended = true;
        if (limitSwitch.get() == true){
            extended = false;
        }   else if ((limitSwitch.get() == false) && (solenoidTimer.getFPGATimestamp() - retractTimeStart < 250)) {
            extended = true;
        }   else if ((limitSwitch.get() == false) && (solenoidTimer.getFPGATimestamp() - retractTimeStart >= 250)) {
            extended = false; 
        }
        return extended;
    

    }

    public void update() {
        if(MatchState.getInstance().GetPeriod() == MatchState.Period.OperatorControl ||
           MatchState.getInstance().GetPeriod() == MatchState.Period.Autonomous){
            //Update Gripper Control
            if(opController.getBallPickupReq()){
                //Cargo Pickup is requested
                setPositionCmd(PEZPos.CargoGrab);
            } else if(opController.getHatchPickupReq()){
                //Hatch Pickup Requested
                setPositionCmd(PEZPos.HatchGrab);
            } else if(opController.getReleaseReq()) {
                //Release whatever we currently have in our gripper
                if(getHeldGamePiece() == GamePiece.Cargo || getHeldGamePiece() == GamePiece.Hatch){
                    setPositionCmd(PEZPos.Release);
    
                } else {
                    setPositionCmd(PEZPos.None);
                }
            } else {
                setPositionCmd(PEZPos.None);
            }
        }
        else{
            //Update Gripper Control - pull position command from driver view interface.
            String gripStart = CasseroleDriverView.getAutoSelectorVal("Starting Gamepiece");
            if(gripStart.compareTo(GamePiece.Cargo.toString())==0){
                setPositionCmd(PEZPos.CargoGrab);
            } else if(gripStart.compareTo(GamePiece.Hatch.toString())==0){
                setPositionCmd(PEZPos.HatchGrab);
            } else {
                setPositionCmd(PEZPos.Release);
            }
        }

        if(posReq == PEZPos.CargoGrab){
            pezSolenoid.set(false);
            electricSolenoid.set(false);
            prevPosReq = PEZPos.CargoGrab;
        } else if(posReq == PEZPos.HatchGrab){
            pezSolenoid.set(true);
            electricSolenoid.set(false);
            prevPosReq = PEZPos.HatchGrab;
        }else if(posReq == PEZPos.Release){
            if (prevPosReq != PEZPos.Release){
                retractTimeStart = solenoidTimer.getFPGATimestamp();
                pezSolenoid.set (false);
                electricSolenoid.set (false);
            }
            boolean isExtended = getPezSolenoidState();
            if (isExtended == false) {
                pezSolenoid.set (true);
                electricSolenoid.set (true);
            } else {} // waiting for cylinder to retract- do nothing //
            prevPosReq = PEZPos.Release;
        }

        if(posReq == PEZPos.HatchGrab){
            curGamePiece = GamePiece.Hatch;
        } else if(posReq == PEZPos.CargoGrab) {
            curGamePiece = GamePiece.Hatch;
        } else if(posReq == PEZPos.Release) {
            curGamePiece = GamePiece.Nothing;
        }
    }

    public void setPositionCmd(PEZPos cmd_in){
        posReq = cmd_in;
    }

    public GamePiece getHeldGamePiece(){
        return curGamePiece;           
    }
}
