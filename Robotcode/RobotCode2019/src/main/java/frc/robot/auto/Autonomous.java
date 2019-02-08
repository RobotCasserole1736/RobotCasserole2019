package frc.robot.auto;

import edu.wpi.first.wpilibj.Timer;
import frc.lib.AutoSequencer.AutoEvent;
import frc.lib.AutoSequencer.AutoSequencer;
import frc.lib.SignalMath.MathyCircularBuffer;
import frc.robot.JeVoisInterface;
import frc.robot.LEDController;
import frc.robot.OperatorController;
import frc.robot.Superstructure;
import frc.robot.Arm.ArmPos;
import frc.robot.LEDController.LEDPatterns;
import frc.robot.PEZControl.PEZPos;
import frc.robot.Superstructure.OpMode;

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

public class Autonomous {
    //State Variables
    boolean prevAutoMoveRequested = false;
    boolean isForward;

    private double visionAngle = 0;

    AutoSequencer seq;

    AutoSeqDistToTgtEst distEst;

    //Jevois over-sample stuff
    MathyCircularBuffer tgtXPosBuf;
    MathyCircularBuffer tgtYPosBuf;
    MathyCircularBuffer tgtAngleBuf;
    final int NUM_AVG_JEVOIS_SAMPLES = 3;
    final double VISION_TIMEOUT_SEC =3.0;
    long prevFrameCounter = 0;
    final double MAX_ALLOWABLE_DISTANCE_STANDARD_DEV = 50; //these are probably way too big, but can be tuned down on actual robot.
    final double MAX_ALLOWABLE_ANGLE_STANDARD_DEV = 100;

    //Blinky auto failed constants
    final double BLINK_RATE_SEC = 0.25;
    double nextBlinkTransitionTime = 0;
    boolean blinkState =false;

    //Jevois & auto startup state
    int jeVoisSampleCounter = 0;
    int jeVoisPreLatchCount = 0;
    boolean stableTargetFound = false;
    double autoStartTimestamp = 0;
    boolean autoFailed = false;

    LEDController ledController;


    // name and "empty" with a variable name
    private static Autonomous empty = null;

    public static synchronized Autonomous getInstance() {
        if (empty == null)
            empty = new Autonomous();
        return empty;
    }

    // This is the private constructor that will be called once by getInstance() and
    // it should instantiate anything that will be required by the class

    // Name for Autonomous enum is AutoState. Welcome to change if need be.
    public enum AutoState {
        Idle(0), Path_Plan_Approach_Vector(1), Move_Along_Approach_Vector(2), Place_Gamepiece_Low_Level(3), 
        Place_Gamepiece_Mid_Level(4), Place_Gamepiece_Top_Level(5), Backup(6);

        public final int value;

        private AutoState(int value){
            this.value = value;
        }
    }

    //needs something inside of the parentheses, probably
    private Autonomous() {
        distEst = AutoSeqDistToTgtEst.getInstance();
        ledController = LEDController.getInstance();

        tgtXPosBuf  = new MathyCircularBuffer(NUM_AVG_JEVOIS_SAMPLES);
        tgtYPosBuf  = new MathyCircularBuffer(NUM_AVG_JEVOIS_SAMPLES);
        tgtAngleBuf = new MathyCircularBuffer(NUM_AVG_JEVOIS_SAMPLES);
    }

    double xTargetOffset = 0;
    double yTargetOffset = 0;
    double targetPositionAngle = 0;


    //Commands called from other parts of the code need to be inputed into the parentheses, I think
    public void update(){
        boolean autoMoveRequested = OperatorController.getInstance().getAutoMove();

        OpMode curOpMode = Superstructure.getInstance().getActualOpMode();
        boolean opModeAllowsAuto = (curOpMode == OpMode.CargoCarry || curOpMode == OpMode.Hatch);
        boolean visionAvailable = JeVoisInterface.getInstance().isTgtVisible() && JeVoisInterface.getInstance().isVisionOnline();

        
        if(autoMoveRequested){

            if(!prevAutoMoveRequested){
                //We got a rising edge on the auto move request
                
                if(opModeAllowsAuto && visionAvailable){
                    //We are in an operational mode where auto align is supported, and have a vison target available.
                    // We're good to go!
                    autoStartTimestamp = Timer.getFPGATimestamp();

                    //Initialize autoMove sequence on rising edge of auto move request from operator, and only if operational mode is not in transistion.
                    xTargetOffset = 0;
                    yTargetOffset = 0;
                    targetPositionAngle = 0;
                    jeVoisSampleCounter = 0;
                    autoFailed = false;
                    tgtXPosBuf.reset();
                    tgtYPosBuf.reset();
                    tgtAngleBuf.reset();

                    jeVoisPreLatchCount = JeVoisInterface.getInstance().getLatchCounter();
                    stableTargetFound = false;
                    JeVoisInterface.getInstance().latchTarget();

                } else {
                    autoFailed = true;
                }
            }

            if(JeVoisInterface.getInstance().getLatchCounter() > jeVoisPreLatchCount && visionAvailable){
                //Jevois latched a new target. Start collecting samples

                long frameCounter = JeVoisInterface.getInstance().getFrameRXCount();
                if(frameCounter != prevFrameCounter){
                    //A new sample has come in from the vision camera
                    tgtXPosBuf.pushFront(JeVoisInterface.getInstance().getTgtPositionX());
                    tgtYPosBuf.pushFront(JeVoisInterface.getInstance().getTgtPositionY());
                    tgtAngleBuf.pushFront(JeVoisInterface.getInstance().getTgtAngle());
                    prevFrameCounter = frameCounter;
                    jeVoisSampleCounter++;
                }

                if(jeVoisSampleCounter >= NUM_AVG_JEVOIS_SAMPLES){
                    //We've got enough vision samples to start evaluating if the target is stable
                    if( (tgtXPosBuf.getStdDev() < MAX_ALLOWABLE_DISTANCE_STANDARD_DEV) &&
                        (tgtYPosBuf.getStdDev() < MAX_ALLOWABLE_DISTANCE_STANDARD_DEV) &&
                        (tgtAngleBuf.getStdDev() < MAX_ALLOWABLE_ANGLE_STANDARD_DEV) 
                      ){
                        //Target is declared "Stable"
                        xTargetOffset = tgtXPosBuf.getAverage();
                        yTargetOffset = tgtYPosBuf.getAverage();
                        targetPositionAngle = tgtAngleBuf.getAverage();
                        stableTargetFound = true; 
                    }
                }
            }


            if(!stableTargetFound && Timer.getFPGATimestamp() > (autoStartTimestamp + VISION_TIMEOUT_SEC) ){
                //took too long to get stable results from the Jevois. Fail.
                autoFailed = true;
            }

            if(stableTargetFound){

                if(OperatorController.getInstance().getAutoAlignHighReq()){
                    isForward = false;
                } else {
                    isForward = true;
                }

                AutoEvent parent;  
                if(OperatorController.getInstance().getAutoAlignHighReq()){
                    //If we're placing top, we can only start at the final alignment step going backward
                    parent = new TopPlaceFinalAlign();
                } else {
                    //If we're placing mid/low, we use Jevois to path plan up to the target location
                    parent = new AutoSeqPathPlan(xTargetOffset, yTargetOffset, targetPositionAngle); //TODO - handle the case where the path planner can't create a path based on current angle constraints with "getPathAvailable()"
                    seq.addEvent(parent);
                    parent = new AutoSeqFinalAlign();
                }

                if(OperatorController.getInstance().getAutoAlignLowReq()){
                    parent.addChildEvent(new MoveArmLowPos(curOpMode));
                    parent.addChildEvent(new MoveGripper(PEZPos.Release));
                } else if(OperatorController.getInstance().getAutoAlignMidReq()){
                    parent.addChildEvent(new MoveArmMidPos(curOpMode));
                    parent.addChildEvent(new MoveGripper(PEZPos.Release));
                } else if(OperatorController.getInstance().getAutoAlignHighReq()){
                    parent.addChildEvent(new MoveArmTopPos(curOpMode));
                    parent.addChildEvent(new MoveGripper(PEZPos.Release));
                } else {
                    System.out.println("Error invalid Autostate.");
                }

                seq.addEvent(parent);

                if(isForward){
                    parent.addChildEvent(new BackupHigh());
                } else {
                    parent.addChildEvent(new Backup());
                }

                seq.start();
            }

            if(autoFailed){
                double curTime = Timer.getFPGATimestamp();
                //Blink a driver station LED while failed
                if(curTime >= nextBlinkTransitionTime){
                    nextBlinkTransitionTime = curTime + BLINK_RATE_SEC;
                    blinkState = !blinkState;
                }
            } else {
                blinkState = false;
            }
        }

        if(seq != null){
            seq.update();
            if(!autoMoveRequested && seq.isRunning()){
                //Cancel sequence
                seq.stop();
            }
        }

        prevAutoMoveRequested = autoMoveRequested;
    }

    public boolean getAutoFailed(){
        return autoFailed;
    }
    public boolean getAutoFailedLEDState(){
        return blinkState;
    }
}