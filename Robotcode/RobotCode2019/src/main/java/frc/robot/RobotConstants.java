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
 *   find it educational and informative! 
 *  Please feel free to snag our software for your own use in whatever project
 *   you have going on right now! We'd love to be able to help out! Shoot us 
 *   any questions you may have, all our contact info should be on our website
 *   (listed above).
 *  If you happen to end up using our software to make money, that is wonderful!
 *   Robot Casserole is always looking for more sponsors, so we'd be very appreciative
 *   if you would consider donating to our club to help further STEM education.
 */

public class RobotConstants {

    
    /////////////////////////////////////////////////////////////////////////////////////
    // RoboRIO Output Ports
    /////////////////////////////////////////////////////////////////////////////////////
    // PWM Motors
    public static final int ARM_MOTOR_PORT = 0;
    public static final int INTAKE_MOTOR_PORT = 1;

    /////////////////////////////////////////////////////////////////////////////////////
    // CAN Device ID's
    /////////////////////////////////////////////////////////////////////////////////////
    public static final int DRIVETRAIN_LEFT_1_CANID = 14;
    public static final int DRIVETRAIN_LEFT_2_CANID = 15;
    public static final int DRIVETRAIN_RIGHT_1_CANID = 0;
    public static final int DRIVETRAIN_RIGHT_2_CANID = 1;
    public static final int PNEUMATICS_CONTROL_MODULE_CANID = 0;
    public static final int POWER_DISTRIBUTION_PANEL_CANID = 0;


    /////////////////////////////////////////////////////////////////////////////////////
    // RoboRIO Input Ports
    /////////////////////////////////////////////////////////////////////////////////////
    //Analog
    public static final int ANALOG_PRESSURE_SENSOR_PORT = 0;
    public static final int ARM_POS_SENSOR_PORT = 1;
    public static final int WRIST_POS_SENSOR_PORT = 2;

    //Digital
    public static final int LED_PATTERN_OUTPUT_0 = 0;
    public static final int LED_PATTERN_OUTPUT_1 = 1;
    public static final int ARM_UPPER_LIMIT_SWITCH_PORT  = 2;
    public static final int ARM_LOWER_LIMIT_SWITCH_PORT  = 3;
    public static final int LINE_FOLLOWING_SENSOR_1_PORT = 4;
    public static final int LINE_FOLLOWING_SENSOR_2_PORT = 5;
    public static final int LINE_FOLLOWING_SENSOR_3_PORT = 6;
    public static final int LINE_FOLLOWING_SENSOR_4_PORT = 7;
    public static final int LINE_FOLLOWING_SENSOR_5_PORT = 8;
    public static final int BALL_INTAKE_PORT = 9;

    /////////////////////////////////////////////////////////////////////////////////////
    // PDP Ports
    /////////////////////////////////////////////////////////////////////////////////////

    /////////////////////////////////////////////////////////////////////////////////////
    // Solenoid Ports
    /////////////////////////////////////////////////////////////////////////////////////
    public static final int INTAKE_ARM_BAR_PORT = 1;

    /////////////////////////////////////////////////////////////////////////////////////
    // Other Constants
    /////////////////////////////////////////////////////////////////////////////////////

    public static final int LONG_SOLENOID_FORWARD_CHANNEL = 0;
    public static final int LONG_SOLENOID_REVERSE_CHANNEL = 1;
    public static final int SHORT_SOLENOID_FORWARD_CHANNEL = 2;
    public static final int SHORT_SOLENOID_REVERSE_CHANNEL = 3;

    public static final int DRIVER_CONTROLLER_USB_IDX = 0;
    public static final int OPERATOR_CONTROLLER_USB_IDX = 1;
    public static final String CAM_1_STREAM_URL = "http://10.17.36.10:1181/stream.mjpg";
    public static final String CAM_2_STREAM_URL = "http://10.17.36.10:1182/stream.mjpg";
    
}