package frc.robot.auto;

import edu.wpi.first.wpilibj.Timer;
import frc.lib.AutoSequencer.AutoEvent;
import frc.lib.AutoSequencer.AutoSequencer;
import frc.lib.SignalMath.MathyCircularBuffer;
import frc.lib.Util.CrashTracker;
import frc.robot.JeVoisInterface;
import frc.robot.LoopTiming;
import frc.robot.OperatorController;
import frc.robot.PEZControl.PEZPos;
import frc.robot.Superstructure.OpMode;
import frc.robot.Superstructure;

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


public class Autonomous {

    /* All possible states for the state machine */
    public enum StateEnum {
        Inactive(0),   /* Inactive */
        SendJevoislatch(1),   /* Send jevois latch */
        waitForLatch(2),   /* wait for latln */
        sampleFromJEV(3),   /* sample from JCV */
        pathPlanner(4),   /* Path-Planner*/
        addLineFollower(5),   /* add Line follower*/
        addAllAutoEvents(6),   /* Add all auto events*/
        autoSeqUpdate(7),  /* AutoSeq .update()*/
        autoError(8); /*EEEERRRROOORRRRR*/

        public final int value;

        private StateEnum(int value) {
            this.value = value;
        }

        public int toInt() {
            return this.value;
        }
    }

    StateEnum curState;
    StateEnum prevState;

    //Define the state you should start in
    final StateEnum INITAL_STATE = StateEnum.Inactive;

    double xTargetOffset;
    double yTargetOffset;
    double targetPositionAngle;
    double autoStartTimestamp;
    double nextBlinkTransitionTime = 0;
    final double BLINK_RATE_MSEC = 250;

    boolean stableTargetFound;
    boolean sendJevoislatch;
    boolean autoFailed = false;
    boolean isForward;
    boolean blinkState = false;

    MathyCircularBuffer tgtXPosBuf;
    MathyCircularBuffer tgtYPosBuf;
    MathyCircularBuffer tgtAngleBuf;

    int jeVoisPreLatchCount = 0;
    int jeVoisSampleCounter = 0;

    long prevFrameCounter = 0;

    final int NUM_AVG_JEVOIS_SAMPLES = 3;

    final double MAX_ALLOWABLE_DISTANCE_STANDARD_DEV = 50; //these are probably way too big, but can be tuned down on actual robot.
    final double MAX_ALLOWABLE_ANGLE_STANDARD_DEV = 100;
    final double VISION_TIMEOUT_SEC =3.0;

    AutoSequencer seq;

    AutoEvent parent; 
    
    OpMode curOpMode = Superstructure.getInstance().getActualOpMode();

    private static Autonomous empty = null;

   
    public static synchronized Autonomous getInstance() {
        if (empty == null)
            empty = new Autonomous();
        return empty;
    }

    public Autonomous(){
        curState = INITAL_STATE;
        prevState = INITAL_STATE;
    }

    public void update(){

        //Sample Inputs
        boolean autoMoveRequested = OperatorController.getInstance().getAutoMove();
        boolean visionAvailable = JeVoisInterface.getInstance().isTgtVisible() && JeVoisInterface.getInstance().isVisionOnline();

        OpMode curOpMode = Superstructure.getInstance().getActualOpMode();
        boolean opModeAllowsAuto = (curOpMode == OpMode.CargoCarry || curOpMode == OpMode.Hatch);

        //Main update loop
        StateEnum nextState = curState;

        //Step 0 - save previous state
        prevState = curState;

        //Do different things depending on what state you are in
        switch(curState){
            case Inactive:
                if(autoMoveRequested == true){
                    if(opModeAllowsAuto && visionAvailable){
                        nextState = StateEnum.SendJevoislatch;
                        sendJevoislatch = true;
                    } else {
                        nextState = StateEnum.autoError;
                    }
                } else {
                    nextState = StateEnum.Inactive;
                }

            break;

            case SendJevoislatch:

                if(autoMoveRequested == true){
                    jeVoisPreLatchCount = JeVoisInterface.getInstance().getLatchCounter();
                    JeVoisInterface.getInstance().latchTarget();
                        
                    nextState = StateEnum.waitForLatch;
                } else {
                    nextState = StateEnum.Inactive;
                }    
                
            break;

            case waitForLatch:
                if(autoMoveRequested == true){
                    if(JeVoisInterface.getInstance().getLatchCounter() > jeVoisPreLatchCount && visionAvailable){
                        //Jevois latched a new target. Start collecting samples
                        nextState = StateEnum.sampleFromJEV;
                    } else if(!visionAvailable){                 
                        nextState = StateEnum.autoError;
                    }
                } else {
                    nextState = StateEnum.Inactive;
                }

            break;

            case sampleFromJEV:
                
                if(autoMoveRequested == true){
                    if(Timer.getFPGATimestamp() > (autoStartTimestamp + VISION_TIMEOUT_SEC) ){
                        //took too long to get stable results from the Jevois. Fail.
                        nextState = StateEnum.autoError;
                    } else {
                        if(visionAvailable){
                            long frameCounter = JeVoisInterface.getInstance().getFrameRXCount();
                            
                            if(frameCounter != prevFrameCounter){
                                //A new sample has come in from the vision camera
                                tgtXPosBuf.pushFront(JeVoisInterface.getInstance().getTgtPositionX());
                                tgtYPosBuf.pushFront(JeVoisInterface.getInstance().getTgtPositionY());
                                tgtAngleBuf.pushFront(JeVoisInterface.getInstance().getTgtAngle());
                                prevFrameCounter = frameCounter;
                                jeVoisSampleCounter++;
    
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
                                        nextState = StateEnum.pathPlanner; 
                                    } 
                                }                    
                            }
                        }
                    }
                } else {
                    nextState = StateEnum.Inactive;
                }

            break;

            case pathPlanner:
                
                if(autoMoveRequested == true){
                    if(OperatorController.getInstance().getAutoAlignHighReq()){
                        //If we're placing top, we can only start at the final alignment step going backward
                        nextState = StateEnum.addLineFollower;
                    } else {
                        //If we're placing mid/low, we use Jevois to path plan up to the target location
                        AutoSeqPathPlan pp = new AutoSeqPathPlan(xTargetOffset, yTargetOffset, targetPositionAngle); //TODO - handle the case where the path planner can't create a path based on current angle constraints with "getPathAvailable()"
                        if(pp.getPathAvailable()){
                            nextState = StateEnum.autoError;
                        }
                        parent = pp;
                        seq.addEvent(parent);
                        parent = new AutoSeqFinalAlign();
                        nextState = StateEnum.addAllAutoEvents;
                    }
                } else {
                    nextState = StateEnum.Inactive;
                }

            break;

            case addLineFollower:
                
                if(OperatorController.getInstance().getAutoAlignHighReq()){
                    parent = new TopPlaceFinalAlign();
                    nextState = StateEnum.addAllAutoEvents;
                } else {
                    nextState = StateEnum.pathPlanner;
                }

            break;

            case addAllAutoEvents:

                //By Default, go to update
                nextState = StateEnum.autoSeqUpdate;
                
                if(OperatorController.getInstance().getAutoAlignLowReq()){
                    parent.addChildEvent(new MoveArmLowPos(curOpMode));
                } else if(OperatorController.getInstance().getAutoAlignMidReq()){
                    parent.addChildEvent(new MoveArmMidPos(curOpMode));
                } else if(OperatorController.getInstance().getAutoAlignHighReq()){
                    parent.addChildEvent(new MoveArmTopPos(curOpMode));
                } else {
                    CrashTracker.logAndPrint("[Autonomous] Error invalid Autostate.");
                    nextState = StateEnum.Inactive;
                }

                if(curOpMode == OpMode.CargoCarry){
                    parent.addChildEvent(new MoveGripper(PEZPos.CargoRelease));
                } else {
                    parent.addChildEvent(new MoveGripper(PEZPos.HatchRelease));
                }
                
                seq.addEvent(parent);

                if(isForward){
                    parent.addChildEvent(new Backup());
                } else {
                    parent.addChildEvent(new BackupHigh());
                }

                seq.start();
            
            break;

            case autoSeqUpdate:

                seq.update();

                if(!autoMoveRequested && seq.isRunning()){
                    //Cancel sequence
                    seq.stop();
                    nextState = StateEnum.Inactive; 
                }
            
            break;

            case autoError:
                
                double sampleTimeMS = LoopTiming.getInstance().getLoopStartTimeSec() * 1000.0;
                double curTime = sampleTimeMS;
                //Blink a driver station LED while failed
                if(curTime >= nextBlinkTransitionTime){
                    nextBlinkTransitionTime = curTime + BLINK_RATE_MSEC;
                    blinkState = !blinkState;
                }
                seq.stop();

                autoFailed = true;
            break;

            default:
                System.out.println("ERROR: unhandled CurState. Tell SW team they wrote bad code!");
                nextState = StateEnum.Inactive;
            break;
        }

        curState = nextState;
    }

    public StateEnum getState(){
        return curState;
    }

    public boolean getAutoFailed(){
        return autoFailed;
    }

    public boolean getAutoFailedLEDState(){
        return blinkState;
    }

    public boolean getAutoSeqActive(){
        return curState != StateEnum.Inactive;
    }

}