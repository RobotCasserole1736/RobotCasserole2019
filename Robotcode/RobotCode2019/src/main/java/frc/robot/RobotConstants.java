package frc.robot;

public class RobotConstants {

    
    /////////////////////////////////////////////////////////////////////////////////////
    // RoboRIO Output Ports
    /////////////////////////////////////////////////////////////////////////////////////
    // PWM Motors
    public static final int LED_CONTROLLER_PORT = 2;

    /////////////////////////////////////////////////////////////////////////////////////
    // CAN Device ID's
    /////////////////////////////////////////////////////////////////////////////////////
    public static final int DRIVETRAIN_LEFT_1_CANID = 1;
    public static final int DRIVETRAIN_LEFT_2_CANID = 15;
    public static final int DRIVETRAIN_RIGHT_1_CANID = 2;
    public static final int DRIVETRAIN_RIGHT_2_CANID = 14;
    public static final int INTAKE_MOTOR_CANID = 8;
    public static final int INTAKE_MOTOR_LEFT_CANID = 31;
    public static final int INTAKE_MOTOR_RIGHT_CANID = 32;
    public static final int PNEUMATICS_CONTROL_MODULE_CANID = 0;
    public static final int POWER_DISTRIBUTION_PANEL_CANID = 0;
    public static final int ARM_MOTOR_PORT = 0;


    /////////////////////////////////////////////////////////////////////////////////////
    // RoboRIO Input Ports
    /////////////////////////////////////////////////////////////////////////////////////

    //Analog
    public static final int ANALOG_PRESSURE_SENSOR_PORT = 1;
    public static final int INTAKE_LEFT_POT_PORT = 2;
    public static final int INTAKE_RIGHT_POT_PORT = 3;
    public static final int ULTRASONIC_FRONT_PORT = 4;
    public static final int ULTRASONIC_REAR_PORT = 5;
    



    public static final int BALL_INTAKE_PORT = 1;

    //Digital
    public static final int ARM_UPPER_LIMIT_SWITCH_PORT  = 2;
    public static final int ARM_LOWER_LIMIT_SWITCH_PORT  = 3;
    public static final int LINE_FOLLOWING_SENSOR_1_PORT = 4;
    public static final int LINE_FOLLOWING_SENSOR_2_PORT = 5;
    public static final int LINE_FOLLOWING_SENSOR_3_PORT = 6;
    public static final int LINE_FOLLOWING_SENSOR_4_PORT = 7;
    public static final int LINE_FOLLOWING_SENSOR_5_PORT = 8;
    public static final int PEZ_SOLENOID_LIMIT_SWITCH_PORT = 9;
    public static final int VISON_LED_RING_PORT = 10;

    //Relay
    public static final int PEZ_RELAY_PORT = 0;

    /////////////////////////////////////////////////////////////////////////////////////
    // PDP Ports
    /////////////////////////////////////////////////////////////////////////////////////
    public static final int INTAKE_LEFT_MOTOR_PDP_PORT = 0;
    public static final int INTAKE_RIGHT_MOTOR_PDP_PORT = 1;
    public static final int CLIMBER_RELEASE_MOTOR_PDP_PORT = 2;

    /////////////////////////////////////////////////////////////////////////////////////
    // Solenoid Ports
    /////////////////////////////////////////////////////////////////////////////////////
    public static final int PEZ_SOLENOID_PORT_CARGO = 0;
    public static final int PEZ_SOLENOID_PORT_HATCH = 1;
    public static final int INTAKE_ARM_BAR_PORT = 4;
    public static final int ARM_MECH_BRAKE_SOL_PORT = 5;
    public static final int PEZ_DUB_SOLENOID_MID_STOPPER = 2;
    public static final int PEZ_DUB_SOLENOID_MID_STOPPER2 = 3;


    /////////////////////////////////////////////////////////////////////////////////////
    // Other Constants
    /////////////////////////////////////////////////////////////////////////////////////

    public static final int DRIVER_CONTROLLER_USB_IDX = 0;
    public static final int OPERATOR_CONTROLLER_USB_IDX = 1;
    public static final String CAM_1_STREAM_URL = "http://10.17.36.10:1181/stream.mjpg";
    public static final String CAM_2_STREAM_URL = "http://10.17.36.10:1182/stream.mjpg";
    public static final double MAIN_LOOP_SAMPLE_RATE_S = 0.02; // 20ms update rate
    public static final double ROBOT_TRACK_WIDTH_FT = 22.0/12; // 22 inch  effective track width
    public static final double WHEEL_RADIUS_FT = 8.75/2/12.0; //8.75 inch diameter wheels
    public static final int CLIMBER_SERVO = 7;
    public static final int ClIMBER_WINDOW_MOTOR = 8;

    

}