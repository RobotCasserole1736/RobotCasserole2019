package frc.robot.auto;

import frc.lib.DataServer.Signal;
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

    public Autonomous(){
        curState = INITAL_STATE;
        prevState = INITAL_STATE;

        boolean autoMoveRequested = OperatorController.getInstance().getAutoMove();
    }

    public void update(){
        //Main update loop
        StateEnum nextState = curState;

        //Step 0 - save previous state
        prevState = curState;

        //Do different things depending on what state you are in
        switch(curState){
            case Inactive:
                //Step 1 - Read inputs relevant to this state
                //TODO - call methods to read inputs as needed

                //Step 2 - Set outputs for the current state
                //TODO - perform actions and set outputs as needed

                //Step 3 - Detremine if we need to transition to a different state, and set nextState to that one.
                nextState = StateEnum.Inactive; //TODO - create logic to populate nextState with a reasonable value.
            break;

            case SendJevoislatch:
                //Step 1 - Read inputs relevant to this state
                //TODO - call methods to read inputs as needed

                //Step 2 - Set outputs for the current state
                //TODO - perform actions and set outputs as needed

                //Step 3 - Detremine if we need to transition to a different state, and set nextState to that one.
                nextState = StateEnum.Inactive; //TODO - create logic to populate nextState with a reasonable value.
            break;

            case waitForLatln:
                //Step 1 - Read inputs relevant to this state
                //TODO - call methods to read inputs as needed

                //Step 2 - Set outputs for the current state
                //TODO - perform actions and set outputs as needed

                //Step 3 - Detremine if we need to transition to a different state, and set nextState to that one.
                nextState = StateEnum.Inactive; //TODO - create logic to populate nextState with a reasonable value.
            break;

            case sampleFromJCV:
                //Step 1 - Read inputs relevant to this state
                //TODO - call methods to read inputs as needed

                //Step 2 - Set outputs for the current state
                //TODO - perform actions and set outputs as needed

                //Step 3 - Detremine if we need to transition to a different state, and set nextState to that one.
                nextState = StateEnum.Inactive; //TODO - create logic to populate nextState with a reasonable value.
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