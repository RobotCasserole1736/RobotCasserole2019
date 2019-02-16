package frc.robot;

import edu.wpi.first.wpilibj.DigitalInput;
import edu.wpi.first.wpilibj.DoubleSolenoid;
import edu.wpi.first.wpilibj.PowerDistributionPanel;
import edu.wpi.first.wpilibj.Solenoid;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.DoubleSolenoid.Value;
import frc.lib.Calibration.Calibration;
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
    DoubleSolenoid pezMidPosStopper;

    DriverController dController;
    OperatorController opController;

    PEZPos posReq;

    TopLevelState opState;
    
    boolean posStable;
    int posStableCounter;
    final int POS_STABLE_DEBOUNCE_LOOPS = 5;

    Signal posReqSig;
    Signal posStableSig;
    Signal retractedLimSwSig;
    Signal stopperCylCmdSig;
    Signal gripperCylCmdSig;

    Calibration manualOvrdCal;

    double transitionTimeStart; 

    DigitalInput limitSwitch;
    boolean limitSwitchVal;

    //Physical mechanism conversion contstants
    final DoubleSolenoid.Value SOL_GRIPPER_EXTEND = DoubleSolenoid.Value.kForward;
    final DoubleSolenoid.Value SOL_GRIPPER_RETRACT = DoubleSolenoid.Value.kReverse;
    final DoubleSolenoid.Value SOL_POS_CARGO_GRAB = SOL_GRIPPER_RETRACT;
    final DoubleSolenoid.Value SOL_POS_CARGO_RELEASE = SOL_GRIPPER_EXTEND;
    final DoubleSolenoid.Value SOL_POS_HATCH_GRAB = SOL_GRIPPER_EXTEND;
    final DoubleSolenoid.Value SOL_POS_HATCH_RELEASE = SOL_GRIPPER_RETRACT;
    final DoubleSolenoid.Value SOL_POS_STOPPER_ENGAGE = DoubleSolenoid.Value.kForward;
    final DoubleSolenoid.Value SOL_POS_STOPPER_RELEASE = DoubleSolenoid.Value.kReverse;

    //Timing constants - related to how fast the piston-attached mechanisms actuate
    // Tune to taste.
    final double MAX_GRIPPER_SOL_TRANSITION_TIME = 0.500;
    final double MAX_STOPPER_SOL_TRANSITION_TIME = 0.200;


    public static synchronized PEZControl getInstance() {
        if(pezCtrl == null)
            pezCtrl = new PEZControl();
        return pezCtrl;
    }

    public enum PEZPos {
        CargoGrab(0), HatchGrab(1), CargoRelease(2), HatchRelease(3), None(6);
        public final int value;

        private PEZPos(int value) {
            this.value = value;
        }
        public int toInt(){
            return this.value;
        }
    }

    private enum TopLevelState {
        Cargo(0), CargoToHatch1(1), CargoToHatch2(2), HatchToCargo1(3), HatchToCargo2(4), Hatch(5), Init(6);

        public final int value;

        private TopLevelState(int value) {
            this.value = value;
        }
        public int toInt(){
            return this.value;
        }
    }

    // This is the private constructor that will be called once by getInstance() and it should instantiate anything that will be required by the class
    private  PEZControl() {
        pezPneumaticCyl = new DoubleSolenoid(RobotConstants.PEZ_SOLENOID_PORT_CARGO, RobotConstants.PEZ_SOLENOID_PORT_HATCH);
        pezMidPosStopper = new DoubleSolenoid(RobotConstants.PEZ_DUB_SOLENOID_MID_STOPPER, RobotConstants.PEZ_DUB_SOLENOID_MID_STOPPER2);
        dController = DriverController.getInstance();
        opController = OperatorController.getInstance();
        limitSwitch = new DigitalInput(RobotConstants.PEZ_SOLENOID_LIMIT_SWITCH_PORT); 
        limitSwitchVal = false;
        posReq = PEZPos.None;

        opState = TopLevelState.Init;

        transitionTimeStart = 0;
        
        manualOvrdCal = new Calibration("Gripper Position Manual Override", 0, 0, 1);

        posReqSig =new Signal("Gripper Position Requested", "pos");
        posStableSig =new Signal("Gripper Position Stable", "bool");
        retractedLimSwSig =new Signal("Gripper Retracted Switch", "bool");
        stopperCylCmdSig = new Signal("Gripper Stopper Cylinder Command", "bool");
        gripperCylCmdSig = new Signal("Gripper Main Cylinder Command", "bool");
    }

    public void setInitCargo(){
        opState = TopLevelState.Cargo;
        posReq = PEZPos.CargoGrab;
    }

    public void setInitHatch(){
        opState = TopLevelState.Hatch;
        posReq = PEZPos.HatchGrab;
    }

    public void update() {

        TopLevelState nextOpState = opState;

        //TODO - switch is mounted in the wrong side. Uncomment this when it's remounted.
        //limitSwitchVal = limitSwitch.get(); 
        limitSwitchVal = false;

        switch(opState){
            case Cargo:
                pezMidPosStopper.set(SOL_POS_STOPPER_RELEASE);
                if(posReq == PEZPos.CargoGrab){
                    pezPneumaticCyl.set(SOL_POS_CARGO_GRAB);
                } else if (posReq == PEZPos.CargoRelease){
                    pezPneumaticCyl.set(SOL_POS_CARGO_RELEASE);
                } else if(posReq == PEZPos.HatchGrab || posReq == PEZPos.HatchRelease){
                    transitionTimeStart = Timer.getFPGATimestamp();
                    nextOpState = TopLevelState.CargoToHatch1;
                }
            break;

            case CargoToHatch1:
                pezPneumaticCyl.set(SOL_GRIPPER_EXTEND);
                if(limitSwitchVal == true || (Timer.getFPGATimestamp() - transitionTimeStart) > MAX_GRIPPER_SOL_TRANSITION_TIME){
                    transitionTimeStart = Timer.getFPGATimestamp();
                    nextOpState = TopLevelState.CargoToHatch2;
                }
            break;

            case CargoToHatch2:
                pezMidPosStopper.set(SOL_POS_STOPPER_ENGAGE);
                if((Timer.getFPGATimestamp() - transitionTimeStart) > MAX_STOPPER_SOL_TRANSITION_TIME){
                    nextOpState = TopLevelState.Hatch;
                }
            break;

            case Hatch:
                pezMidPosStopper.set(SOL_POS_STOPPER_ENGAGE);
                if(posReq == PEZPos.HatchGrab){
                    pezPneumaticCyl.set(SOL_POS_HATCH_GRAB);
                } else if (posReq == PEZPos.HatchRelease){
                    pezPneumaticCyl.set(SOL_POS_HATCH_RELEASE);
                } else if(posReq == PEZPos.CargoGrab || posReq == PEZPos.CargoRelease){
                    transitionTimeStart = Timer.getFPGATimestamp();
                    nextOpState = TopLevelState.HatchToCargo1;
                }
            break;

            case HatchToCargo1:
                pezPneumaticCyl.set(SOL_GRIPPER_EXTEND);
                if(limitSwitchVal == true || (Timer.getFPGATimestamp() - transitionTimeStart) > MAX_GRIPPER_SOL_TRANSITION_TIME){
                    transitionTimeStart = Timer.getFPGATimestamp();
                    nextOpState = TopLevelState.HatchToCargo2;
                }
            break;

            case HatchToCargo2:
                pezMidPosStopper.set(SOL_POS_STOPPER_RELEASE);
                if((Timer.getFPGATimestamp() - transitionTimeStart) > MAX_STOPPER_SOL_TRANSITION_TIME){
                    nextOpState = TopLevelState.Cargo;
                }
            break;

            case Init:
                //Do nothing till we're actually init'ed into the desired state.
            break;

            default:
                System.out.println("ERROR: Software Team made an oppsie whoopsie.");
            break;
        }

        //Handle calculation of whether we've settled in a final state or not.
        posStable = false;
        boolean opModeTransitioning = (opState != TopLevelState.Hatch && opState != TopLevelState.Cargo) || (opState != nextOpState);
        if(opModeTransitioning){
            posStableCounter = POS_STABLE_DEBOUNCE_LOOPS;
        } else {
            if(posStableCounter > 0){
                posStableCounter--;
            } else {
                if(opState == TopLevelState.Hatch || opState == TopLevelState.Cargo){
                    posStable = true;
                }
            }
        }

        

        double sampleTimeMS = LoopTiming.getInstance().getLoopStartTimeSec() * 1000.0;
        posReqSig.addSample(sampleTimeMS, posReq.toInt());
        posStableSig.addSample(sampleTimeMS, posStable);
        retractedLimSwSig.addSample(sampleTimeMS, limitSwitchVal);
        stopperCylCmdSig.addSample(sampleTimeMS, convStopperSolPos(pezMidPosStopper.get()));
        gripperCylCmdSig.addSample(sampleTimeMS, convGripperSolPos(pezPneumaticCyl.get()));

        opState = nextOpState;
    }

    double convGripperSolPos(DoubleSolenoid.Value in){
        if(in == SOL_GRIPPER_EXTEND){
            return 1;
        } else {
            return 0;
        }
    }

    double convStopperSolPos(DoubleSolenoid.Value in){
        if(in == SOL_POS_STOPPER_ENGAGE){
            return 1;
        } else {
            return 0;
        }
    }

    public void setPositionCmd(PEZPos cmd_in){
        if(manualOvrdCal.get() != 1.0){
            //Use external command
            if(cmd_in != PEZPos.None){
                posReq = cmd_in;
            }// otherwise keep posReq unchanged.
        } else {
            //Use override command straight from driver controller
            int cmd = DriverController.getInstance().xb.getPOV();
            if(cmd == 0){
                posReq = PEZPos.CargoGrab;
            } else if(cmd == 90){
                posReq = PEZPos.CargoRelease;
            } else if(cmd == 270){
                posReq = PEZPos.HatchRelease;
            } else if(cmd == 180){
                posReq =PEZPos.HatchGrab;
            }
        }

    }

    public boolean isAtDesPos(){
        return posStable;
    }
}
