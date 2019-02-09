package frc.robot;

import edu.wpi.first.wpilibj.DigitalInput;
import edu.wpi.first.wpilibj.DoubleSolenoid;
import edu.wpi.first.wpilibj.PowerDistributionPanel;
import edu.wpi.first.wpilibj.Relay;
import edu.wpi.first.wpilibj.Solenoid;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.Relay.Value;
import frc.lib.DataServer.Signal;
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

    DoubleSolenoid pezPneumaticCyl;
    Relay pezMidPosStopper;

    DriverController dController;
    OperatorController opController;

    GamePiece curGamePiece;

    PEZPos posReq;
    PEZPos prevPosReq; 
    PEZPos posEst;
    
    boolean posStable;
    int posStableCounter;
    final int POS_STABLE_DEBOUNCE_LOOPS = 5;

    Signal posReqSig;
    Signal posEstSig;
    Signal retractedLimSwSig;


    double retractTimeStart; 

    DigitalInput limitSwitch;
    boolean limitSwitchVal;

    //Physical mechanism conversion contstants
    final DoubleSolenoid.Value SOL_POS_CARGO = DoubleSolenoid.Value.kForward;
    final DoubleSolenoid.Value SOL_POS_HATCH = DoubleSolenoid.Value.kReverse;
    final DoubleSolenoid.Value SOL_POS_RELEASE = SOL_POS_HATCH;

    final Relay.Value REL_POS_EXTEND = Relay.Value.kForward;
    final Relay.Value REL_POS_RETRACT = Relay.Value.kOff;

    final double MAX_EXTEND_DUR_SEC = 0.500;


    public static synchronized PEZControl getInstance() {
        if(pezCtrl == null)
            pezCtrl = new PEZControl();
        return pezCtrl;
    }

    public enum PEZPos {
        CargoGrab(0), Release(1), HatchGrab(2), None(3), InTransit(4);
        public final int value;

        private PEZPos(int value) {
            this.value = value;
        }
        public int toInt(){
            return this.value;
        }
    }

    public enum GamePiece {
        Nothing(0), Cargo(1), Hatch(2);
        public final int value;

        private GamePiece(int value) {
            this.value = value;
        }
        public int toInt(){
            return this.value;
        }
    }

    // This is the private constructor that will be called once by getInstance() and it should instantiate anything that will be required by the class
    private  PEZControl() {
        pezPneumaticCyl = new DoubleSolenoid(RobotConstants.PEZ_SOLENOID_PORT_CARGO, RobotConstants.PEZ_SOLENOID_PORT_HATCH);
        pezMidPosStopper = new Relay(RobotConstants.PEZ_RELAY_PORT);
        dController = DriverController.getInstance();
        opController = OperatorController.getInstance();
        limitSwitch = new DigitalInput(RobotConstants.PEZ_SOLENOID_LIMIT_SWITCH_PORT); 
        posEst = PEZPos.None;
        limitSwitchVal = false;
        posReq = PEZPos.None;

        posReqSig =new Signal("Gripper Position Requested", "pos");
        posEstSig =new Signal("Gripper Position Estimate", "pos");
        retractedLimSwSig =new Signal("Gripper Retracted Switch", "bool");
    }

    private boolean checkExtended(){ 
        boolean  extended = true;
        if (limitSwitchVal == true){
            //The limit switch was pressed, we are no longer extended
            extended = false;
        } else if ( (Timer.getFPGATimestamp() - retractTimeStart) > MAX_EXTEND_DUR_SEC) {
            //The timer expired. We are probably no longer extended.
            extended = false;
        } else {
            //No prior case is true. We must still be extended.
            extended = true; 
        }
        return extended;
    }

    public void update() {
        limitSwitchVal = limitSwitch.get();

        if(posReq == PEZPos.CargoGrab){
            posEst = posReq;
            pezPneumaticCyl.set(SOL_POS_CARGO);
            pezMidPosStopper.set(Value.kOff);

        } else if(posReq == PEZPos.HatchGrab){
            posEst = posReq;
            pezPneumaticCyl.set(SOL_POS_HATCH);
            pezMidPosStopper.set(Value.kOff);

        }else if(posReq == PEZPos.Release){
            if (prevPosReq != PEZPos.Release){
                retractTimeStart = Timer.getFPGATimestamp();
                pezPneumaticCyl.set(SOL_POS_CARGO);
                pezMidPosStopper.set(Value.kOff);
            }

            boolean isExtended = checkExtended();
            if (isExtended == false) {
                posEst = posReq;
                pezPneumaticCyl.set (SOL_POS_RELEASE);
                pezMidPosStopper.set(Value.kForward); 
            } else {
                // waiting for cylinder to retract
                posEst = PEZPos.InTransit;
            } 
        }

        if(posEst != prevPosReq){
            posStableCounter = POS_STABLE_DEBOUNCE_LOOPS;
        }

        if(posStableCounter > 0){
            posStableCounter--;
        }

        if(posStableCounter == 0 && posEst != PEZPos.InTransit){
            posStable = true;
        } else {
            posStable = false;
        }

        prevPosReq = posReq;

        double sampleTimeMS = LoopTiming.getInstance().getLoopStartTimeSec() * 1000.0;
        posReqSig.addSample(sampleTimeMS, posReq.toInt());
        posEstSig.addSample(sampleTimeMS, posEst.toInt());
        retractedLimSwSig.addSample(sampleTimeMS, limitSwitchVal);
    }

    public void setPositionCmd(PEZPos cmd_in){
        posReq = cmd_in;
    }

    public boolean isAtDesPos(){
        return posStable;
    }
}
