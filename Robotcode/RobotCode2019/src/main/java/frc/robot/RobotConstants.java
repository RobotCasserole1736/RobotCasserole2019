package frc.robot;

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
    public static final int DRIVETRAIN_LEFT_1_CANID = 1;
    public static final int DRIVETRAIN_LEFT_2_CANID = 15;
    public static final int DRIVETRAIN_RIGHT_1_CANID = 2;
    public static final int DRIVETRAIN_RIGHT_2_CANID = 14;
    public static final int PNEUMATICS_CONTROL_MODULE_CANID = 0;
    public static final int POWER_DISTRIBUTION_PANEL_CANID = 0;


    /////////////////////////////////////////////////////////////////////////////////////
    // RoboRIO Input Ports
    /////////////////////////////////////////////////////////////////////////////////////
    public static final int ANALOG_PRESSURE_SENSOR_PORT = 0;
    public static final int ARM_POS_SENSOR_PORT = 1;

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
    public static final int LONG_SOLENOID_FORWARD_CHANNEL = 0;
    public static final int LONG_SOLENOID_REVERSE_CHANNEL = 1;
    public static final int SHORT_SOLENOID_FORWARD_CHANNEL = 2;
    public static final int SHORT_SOLENOID_REVERSE_CHANNEL = 3;
    public static final int INTAKE_ARM_BAR_PORT = 4;
    public static final int ARM_MECH_BRAKE_SOL_PORT = 5;

    /////////////////////////////////////////////////////////////////////////////////////
    // Other Constants
    /////////////////////////////////////////////////////////////////////////////////////

    public static final int DRIVER_CONTROLLER_USB_IDX = 0;
    public static final int OPERATOR_CONTROLLER_USB_IDX = 1;
    public static final String CAM_1_STREAM_URL = "http://10.17.36.10:1181/stream.mjpg";
    public static final String CAM_2_STREAM_URL = "http://10.17.36.10:1182/stream.mjpg";
    
}