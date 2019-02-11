package frc.robot.auto;

import frc.lib.DataServer.Signal;
import frc.lib.SignalMath.MathyCircularBuffer;
import frc.robot.JeVoisInterface;
import frc.robot.OperatorController;

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
        waitForLatln(2),   /* wait for latln */
        sampleFromJCV(3),   /* sample from JCV */
        pathPlanner(4),   /* Path-Planner*/
        addLineFollower(5),   /* add Line follower*/
        addAllAutoEvents(6),   /* Add all auto events*/
        autoSeqUpdate(7);   /* AutoSeq .update()*/

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

    boolean stableTargetFound;
    boolean sendJevoislatch;

    MathyCircularBuffer tgtXPosBuf;
    MathyCircularBuffer tgtYPosBuf;
    MathyCircularBuffer tgtAngleBuf;

    int jeVoisPreLatchCount = 0;
    int jeVoisSampleCounter = 0;

    long prevFrameCounter = 0;

    final int NUM_AVG_JEVOIS_SAMPLES = 3;

    final double MAX_ALLOWABLE_DISTANCE_STANDARD_DEV = 50; //these are probably way too big, but can be tuned down on actual robot.
    final double MAX_ALLOWABLE_ANGLE_STANDARD_DEV = 100;

    public Autonomous(){
        curState = INITAL_STATE;
        prevState = INITAL_STATE;

    }

    public void update(){

        boolean autoMoveRequested = OperatorController.getInstance().getAutoMove();
        boolean visionAvailable = JeVoisInterface.getInstance().isTgtVisible() && JeVoisInterface.getInstance().isVisionOnline();

        //Main update loop
        StateEnum nextState = curState;

        //Step 0 - save previous state
        prevState = curState;

        //Do different things depending on what state you are in
        switch(curState){
            case Inactive:
                if(autoMoveRequested == true){
                    nextState = StateEnum.SendJevoislatch;
                    sendJevoislatch = true;
                } else {
                    nextState = StateEnum.Inactive;
                }

            break;

            case SendJevoislatch:

                if(sendJevoislatch == true){
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
                    nextState = StateEnum.waitForLatln;
                } else {
                    nextState = StateEnum.Inactive;
                }

            break;

            case waitForLatln:
    
                nextState = StateEnum.Inactive;
            
            break;

            case sampleFromJCV:
                
                nextState = StateEnum.Inactive; 
            
            break;

            case pathPlanner:
                //Step 1 - Read inputs relevant to this state
                //TODO - call methods to read inputs as needed

                //Step 2 - Set outputs for the current state
                //TODO - perform actions and set outputs as needed

                //Step 3 - Detremine if we need to transition to a different state, and set nextState to that one.
                nextState = StateEnum.Inactive; //TODO - create logic to populate nextState with a reasonable value.
            break;

            case addLineFollower:
                //Step 1 - Read inputs relevant to this state
                //TODO - call methods to read inputs as needed

                //Step 2 - Set outputs for the current state
                //TODO - perform actions and set outputs as needed

                //Step 3 - Detremine if we need to transition to a different state, and set nextState to that one.
                nextState = StateEnum.Inactive; //TODO - create logic to populate nextState with a reasonable value.
            break;

            case addAllAutoEvents:
                //Step 1 - Read inputs relevant to this state
                //TODO - call methods to read inputs as needed

                //Step 2 - Set outputs for the current state
                //TODO - perform actions and set outputs as needed

                //Step 3 - Detremine if we need to transition to a different state, and set nextState to that one.
                nextState = StateEnum.Inactive; //TODO - create logic to populate nextState with a reasonable value.
            break;

            case autoSeqUpdate:
                //Step 1 - Read inputs relevant to this state
                //TODO - call methods to read inputs as needed

                //Step 2 - Set outputs for the current state
                //TODO - perform actions and set outputs as needed

                //Step 3 - Detremine if we need to transition to a different state, and set nextState to that one.
                nextState = StateEnum.Inactive; //TODO - create logic to populate nextState with a reasonable value.
            break;

            default:
                System.out.println("ERROR: unhandled CurState. Tell SW team they wrote bad code!");
                nextState = INITAL_STATE;
            break;
        }

        curState = nextState;
    }

    public StateEnum getState(){
        return curState;
    }

}