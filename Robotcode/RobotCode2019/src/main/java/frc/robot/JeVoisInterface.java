package frc.robot;

import edu.wpi.cscore.MjpegServer;
import edu.wpi.cscore.UsbCamera;
import edu.wpi.cscore.VideoMode.PixelFormat;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.SerialPort;
import edu.wpi.first.wpilibj.Timer;
import frc.lib.DataServer.Signal;
import frc.lib.Util.CrashTracker;

public class JeVoisInterface {

    private static JeVoisInterface instance = null;

    public static synchronized JeVoisInterface getInstance() {
		if(instance == null)
			instance = new JeVoisInterface();
        return instance;
    }
    
    // Serial Port Constants 
    private static final int BAUD_RATE = 115200;
    
    // MJPG Streaming Constants 
    private static final int MJPG_STREAM_PORT = 1180;
    
    // Packet format constants 
    private static final String PACKET_START_CHAR = "{";
    private static final String PACKET_END_CHAR = "}";
    private static final String PACKET_DILEM_CHAR = ",";
    private static final int PACKET_NUM_EXPECTED_FIELDS = 3;
    
    
    // Confgure the camera to stream debug images or not.
    private boolean broadcastUSBCam = false;
    
    // When not streaming, use this mapping
    private static final int NO_STREAM_MAPPING = 0;
    
    // When streaming, use this set of configuration
    private static final int STREAM_WIDTH_PX = 352;
    private static final int STREAM_HEIGHT_PX = 288;
    private static final int STREAM_RATE_FPS = 15;
    
    // Serial port used for getting target data from JeVois 
    private SerialPort visionPort = null;
    
    // USBCam and server used for broadcasting a webstream of what is seen 
    private UsbCamera visionCam = null;
    private MjpegServer camServer = null;
    
    // Status variables 
    private boolean dataStreamRunning = false;
    private boolean camStreamRunning = false;
    private boolean visionOnline = false;

    // Packet rate performace tracking
    private double packetRxTime = 0;
    private double prevPacketRxTime = 0;
    private double packetRxRatePPS = 0;

    // Most recently seen target information
    private boolean tgtVisible = false;   //True if a target is seen, false otherwise
    private double  tgtAngle_rad = 0;     //Angle from center that the target appears in the camera. Shows if the robot is pointed at the target, or off to the side.
    private double  tgtGeneralAngle_deg=0;
    private double  tgtXPos_ft = 0;       //Position of the target relative to the robot in Ft
    private double  tgtYPos_ft = 0;       //Position of the target relative to the robot in Ft
    private int     latchCounter = 0;  //Skew of the target - if it's pointed at the robot, or away from the robot. AKA normal vector away from the wall.
    private double  tgtTime = 0;          //Estimated to time of image capture (on the scale of Timer.getFPGATimestamp())
    
    // Info about the JeVois performace & status
    private double jeVoisCpuTempC = 0;
    private double jeVoisCpuLoadPct = 0;
    private double jeVoisFramerateFPS = 0;

    private long frameCounter = 0;

    private Signal tgtVisibleSig;
    private Signal tgtAngleSig;
    private Signal tgtXPosSig;
    private Signal tgtYPosSig;
    private Signal tgtRotationSig;
    private Signal tgtCaptureTimeSig;
    private Signal jevoisCpuTempSig;
    private Signal jevoisCpuLoadSig;
    private Signal jevoisFramerateSig;
    private Signal tgtGeneralAngleSig;
    private Signal jevoisPacketsPerSecSig;
    private Signal framecounterSig;
    private Signal jevoisLatchCounterSig;



    
    
    //=======================================================
    //== BEGIN PUBLIC INTERFACE
    //=======================================================

    /**
     * Constructor (simple). Opens a USB serial port to the JeVois camera, sends a few test commands checking for error,
     * then fires up the user's program and begins listening for target info packets in the background
     */
    private JeVoisInterface() {
        this(false); //Default - stream disabled, just run serial.
    }

    /**
     * Constructor (more complex). Opens a USB serial port to the JeVois camera, sends a few test commands checking for error,
     * then fires up the user's program and begins listening for target info packets in the background.
     * Pass TRUE to additionaly enable a USB camera stream of what the vision camera is seeing.
     */
    private JeVoisInterface(boolean useUSBStream) {
        int retry_counter = 0;

        // Configure telemetry signals
        tgtVisibleSig = new Signal("Jevois Target Visible", "bool");
        tgtAngleSig = new Signal("Jevois Target Angle", "rad");
        tgtXPosSig = new Signal("Jevois Target X Position", "ft");
        tgtYPosSig = new Signal("Jevois Target Y Position", "ft");
        tgtRotationSig = new Signal("Jevois Target Rotation Angle", "Deg");
        tgtCaptureTimeSig = new Signal("Jevois Image Capture Time", "sec");
        jevoisCpuTempSig = new Signal("Jevois CPU Temp", "C");
        jevoisCpuLoadSig = new Signal("Jevois CPU Load", "pct");
        jevoisFramerateSig = new Signal("Jevois Framerate", "fps");
        jevoisPacketsPerSecSig = new Signal("Jevois Packets Per Sec", "pps");
        framecounterSig = new Signal("Jevois Frame Count", "count");
        tgtGeneralAngleSig = new Signal("Jevois General Angle", "Deg");
        jevoisLatchCounterSig = new Signal("Jevois Latch Count", "count");

        //Retry strategy to get this serial port open.
        //I have yet to see a single retry used assuming the camera is plugged in
        // but you never know.
        while(visionPort == null && retry_counter++ < 3){
            try {
                CrashTracker.logAndPrint("[JeVois Interface] Creating JeVois SerialPort...");
                visionPort = new SerialPort(BAUD_RATE,SerialPort.Port.kUSB);
                CrashTracker.logAndPrint("[JeVois Interface] SUCCESS!!");
            } catch (Exception e) {
                CrashTracker.logAndPrint("[JeVois Interface] FAILED!!");
                e.printStackTrace();
                sleep(500);
                CrashTracker.logAndPrint("[JeVois Interface] Retry " + Integer.toString(retry_counter));
            }
        }

        
        //Report an error if we didn't get to open the serial port
        if(visionPort == null){
            DriverStation.reportError("Cannot open serial port to JeVois. Not starting vision system.", false);
            return;
        }
        
        //Test to make sure we are actually talking to the JeVois
        if(sendPing() != 0){
            DriverStation.reportError("JeVois ping test failed. Not starting vision system.", false);
            return;
        }
        
        //Ensure the JeVois is starting with the stream off.
        stopDataOnlyStream();

        setCameraStreamActive(useUSBStream);

        start();

        //Start listening for packets
        packetListenerThread.setDaemon(true);
        packetListenerThread.start();

    } 

    public void start(){
        if(broadcastUSBCam){
            //Start streaming the JeVois via webcam
            //This auto-starts the serial stream
            startCameraStream(); 
        } else {
            startDataOnlyStream();
        }
    }

    public void stop(){
        if(broadcastUSBCam){
            //Start streaming the JeVois via webcam
            //This auto-starts the serial stream
            stopCameraStream(); 
        } else {
            stopDataOnlyStream();
        }
    }
    
    /**
     * Send commands to the JeVois to configure it for image-processing friendly parameters
     */
    public void setCamVisionProcMode() {
        if (visionPort != null){
            //all should be done on jevois
        }
    }
    
    /**
     * Send parameters to the camera to configure it for a human-readable image
     */
    public void setCamHumanDriverMode() {
        if (visionPort != null){
            //all on jevois
        }
    }

    /*
     * Main getters/setters
     */

    /**
     * Set to true to enable the camera stream, or set to false to stream serial-packets only.
     * Note this cannot be changed at runtime due to jevois constraints. You must stop whatatever processing
     * is going on first.
     */
    public void setCameraStreamActive(boolean active){
        if(dataStreamRunning == false){
            broadcastUSBCam = active;
        } else {
            DriverStation.reportError("Attempt to change cal stream mode while JeVois is still running. This is disallowed.", false);
        }
        

    }

    /**
     * Returns the most recently seen target's X position in the image, converted to an angle from the robot.
     */
    public double getTgtAngle() {
        return tgtAngle_rad;
    }

    /**
     * Returns the estimated disatance to the target in ft.
     */
    public double getTgtPositionX() {
        return tgtXPos_ft;
    }

    /**
     * Returns the estimated disatance to the target in ft.
     */
    public double getTgtPositionY() {
        return tgtYPos_ft;
    }



    /* Returns the angle from the camera to the robot in degrees*/
    public double getTgtGeneralAngle(){
        return tgtGeneralAngle_deg;
    }

    /**
     * Returns the most recently seen target's skew rotation (aka Normal Vector from the wall)
     */
    public int getLatchCounter() {
        return latchCounter;
    }
    
    /**
     * Get the estimated timestamp of the most recent target observation.
     * This is calculated based on the FPGA timestamp at packet RX time, minus the reportetd vision pipeline delay.
     * It will not currently account for serial hardware or other delays.
     */
    public double getTgtTime() {
        return tgtTime;
    }
    
    /**
     * Returns true when the roboRIO is recieving packets from the JeVois, false if no packets have been recieved.
     * Other modules should not use the vision processing results if this returns false.
     */
    public boolean isVisionOnline() {
        return visionOnline;
    }
    
    /**
     * Returns true when the JeVois sees a target and is tracking it, false otherwise.
     */
    public boolean isTgtVisible() {
        return tgtVisible;
    }
    
    /**
     * Returns the JeVois's most recently reported CPU Temperature in deg C
     */
    public double getJeVoisCPUTemp_C(){
        return jeVoisCpuTempC;
    }

    /**
     * Returns the JeVois's most recently reported CPU Load in percent of max
     */
    public double getJeVoisCpuLoad_pct(){
        return jeVoisCpuLoadPct;
    }

    /**
     * Returns the JeVois's most recently reported pipline framerate in Frames per second
     */
    public double getJeVoisFramerate_FPS(){
        return jeVoisFramerateFPS;
    }

    /**
     * Returns the number of serial packets we've recieved from the JeVois
     * @return
     */
    public long getFrameRXCount(){
        return frameCounter;
    }

    /**
     * Returns the roboRIO measured serial packet recieve rate in packets per second
     */
    public int getPacketRxRate_PPS(){
        if(visionOnline){
            return (int)Math.round(packetRxRatePPS);
        } else {
            return 0;
        }
    }

    /**
     * Indicates to the Camera that it should lock on to whatever vision target it sees in the middle of the screen right now
     */
    public void latchTarget(){
        sendCmd("latch");
    }

    //=======================================================
    //== END PUBLIC INTERFACE
    //=======================================================

    
    /**
     * This is the main perodic update function for the Listener. It is intended
     * to be run in a background task, as it will block until it gets packets. 
     */
    private void backgroundUpdate(){
        
        // Grab packets and parse them.
        String packet;
        
        prevPacketRxTime = packetRxTime;
        packet = blockAndGetPacket(2.0);
        
        
        if(packet != null){
            packetRxTime = Timer.getFPGATimestamp();
            if( parsePacket(packet, packetRxTime) == 0){
                visionOnline = true;
                packetRxRatePPS = 1.0/(packetRxTime - prevPacketRxTime);
            } else {
                visionOnline = false;
            }
            
        } else {
            visionOnline = false;
            DriverStation.reportWarning("Cannot get packet from JeVois Vision Processor", false);
        }
        
    }

    /**
     * Send the ping command to the JeVois to verify it is connected
     * @return 0 on success, -1 on unexpected response, -2 on timeout
     */
    private int sendPing() {
        int retval = -1;
        if (visionPort != null){
            retval = sendCmdAndCheck("ping");
        }
        return retval;
    }

    private void startDataOnlyStream(){
        //Send serial commands to start the streaming of target info
        sendCmdAndCheck("setmapping " + Integer.toString(NO_STREAM_MAPPING));
        sendCmdAndCheck("streamon");
        dataStreamRunning = true;
    }

    private void stopDataOnlyStream(){
        //Send serial commands to stop the streaming of target info
        sendCmdAndCheck("streamoff");
        dataStreamRunning = false;
    }
    

    /**
     * Open an Mjpeg streamer from the JeVois camera
     */
    private void startCameraStream(){
        try{
            System.out.print("Starting JeVois Cam Stream...");
            visionCam = new UsbCamera("VisionProcCam", 0);
            visionCam.setVideoMode(PixelFormat.kBGR, STREAM_WIDTH_PX, STREAM_HEIGHT_PX, STREAM_RATE_FPS);
            camServer = new MjpegServer("VisionCamServer", MJPG_STREAM_PORT);
            camServer.setSource(visionCam);
            camStreamRunning = true;
            dataStreamRunning = true;
            CrashTracker.logAndPrint("[JeVois Interface] SUCCESS!!");
        } catch (Exception e) {
            DriverStation.reportError("Cannot start camera stream from JeVois", false);
            e.printStackTrace();
        }
    }
    
    /**
     * Cease the operation of the camera stream. Unknown if needed.
     */
    private void stopCameraStream(){
        if(camStreamRunning){
            camServer.free();
            visionCam.free();
            camStreamRunning = false;
            dataStreamRunning = false;
        }
    }
    
    /**
     * Sends a command over serial to JeVois and returns immediately.
     * @param cmd String of the command to send (ex: "ping")
     * @return number of bytes written
     */
    private int sendCmd(String cmd){
        int bytes;
        bytes = visionPort.writeString(cmd + "\n");
        CrashTracker.logAndPrint("[JeVois Interface] wrote " +  bytes + "/" + (cmd.length()+1) + " bytes, cmd: " + cmd);
        return bytes;
    }
    
    /**
     * Sends a command over serial to the JeVois, waits for a response, and checks that response
     * Automatically ends the line termination character.
     * @param cmd String of the command to send (ex: "ping")
     * @return 0 if OK detected, -1 if ERR detected, -2 if timeout waiting for response
     */
    public int sendCmdAndCheck(String cmd){
        int retval = 0;
        sendCmd(cmd);
        retval = blockAndCheckForOK(1.0);
        if(retval == -1){
            CrashTracker.logAndPrint(cmd + " Produced an error");
        } else if (retval == -2) {
            CrashTracker.logAndPrint(cmd + " timed out");
        }
        return retval;
    }

    //Persistent but "local" variables for getBytesPeriodic()
    private String getBytesWork = "";
    private int loopCount = 0;
    /**
     * Read bytes from the serial port in a non-blocking fashion
     * Will return the whole thing once the first "OK" or "ERR" is seen in the stream.
     * Returns null if no string read back yet.
     */
    private String getCmdResponseNonBlock() {
        String retval =  null;
        if (visionPort != null){
            if (visionPort.getBytesReceived() > 0) {
                String rxString = visionPort.readString();
                CrashTracker.logAndPrint("[JeVois Interface] Waited: " + loopCount + " loops, Rcv'd: " + rxString);
                getBytesWork += rxString;
                if(getBytesWork.contains("OK") || getBytesWork.contains("ERR")){
                    retval = getBytesWork;
                    getBytesWork = "";
                    CrashTracker.logAndPrint("[Jevois]" + retval);
                }
                loopCount = 0;
            } else {
                ++loopCount;
            }
        }
        return retval;
    }
    
    /** 
     * Blocks thread execution till we get a response from the serial line
     * or timeout. 
     * Return values:
     *  0 = OK in response
     * -1 = ERR in response
     * -2 = No token found before timeout_s
     */
    private int blockAndCheckForOK(double timeout_s){
        int retval = -2;
        double startTime = Timer.getFPGATimestamp();
        String testStr = "";
        if (visionPort != null){
            while(Timer.getFPGATimestamp() - startTime < timeout_s){
                if (visionPort.getBytesReceived() > 0) {
                    testStr += visionPort.readString();
                    CrashTracker.logAndPrint("[JeVois] " + testStr); //debug only 
                    if(testStr.contains("OK")){
                        retval = 0;
                        break;
                    }else if(testStr.contains("ERR")){
                        DriverStation.reportError("JeVois reported error:\n" + testStr, false);
                        retval = -1;
                        break;
                    }

                } else {
                    sleep(10);
                }
            }
        }
        return retval;
    }
    
    
    // buffer to contain data from the port while we gather full packets 
    private StringBuffer packetBuffer = new StringBuffer(100);
    /** 
     * Blocks thread execution till we get a valid packet from the serial line
     * or timeout. 
     * Return values:
     *  String = the packet 
     *  null = No full packet found before timeout_s
     */
    private String blockAndGetPacket(double timeout_s){
        String retval = null;
        double startTime = Timer.getFPGATimestamp();
        int endIdx = -1;
        int startIdx = -1;
        
        if (visionPort != null){
            while(Timer.getFPGATimestamp() - startTime < timeout_s){
                // Keep trying to get bytes from the serial port until the timeout expires.
                
                
                if (visionPort.getBytesReceived() > 0) {
                    // If there are any bytes available, read them in and 
                    //  append them to the buffer.
                    packetBuffer = packetBuffer.append(visionPort.readString());

                    // Attempt to detect if the buffer currently contains a complete packet
                    if(packetBuffer.indexOf(PACKET_START_CHAR) != -1){
                        endIdx = packetBuffer.lastIndexOf(PACKET_END_CHAR);
                        if(endIdx != -1){
                            // Buffer also contains at least one start & end character.
                            // But we don't know if they're in the right order yet.
                            // Start by getting the most-recent packet end character's index
                             
                            
                            // Look for the index of the start character for the packet
                            //  described by endIdx. Note this line of code assumes the 
                            //  start character for the packet must come _before_ the
                            //  end character.
                            startIdx = packetBuffer.lastIndexOf(PACKET_START_CHAR, endIdx);
                            
                            if(startIdx == -1){
                                // If there was no start character before the end character,
                                //  we can assume that we have something a bit wacky in our
                                //  buffer. For example: ",abc}garbage{1,2".
                                // Since we've started to receive a good packet, discard 
                                //  everything prior to the start character.
                                startIdx = packetBuffer.lastIndexOf(PACKET_START_CHAR);
                                packetBuffer.delete(0, startIdx);
                            } else {
                                // Buffer contains a full packet. Extract it and clean up buffer
                                retval = packetBuffer.substring(startIdx+1, endIdx);
                                packetBuffer.delete(0, endIdx+1);
                                break;
                            } 
                        } else {
                          // In this case, we have a start character, but no end to the buffer yet. 
                          //  Do nothing, just wait for more characters to come in.
                          sleep(5);
                        }
                    } else {
                        // Buffer contains no start characters. None of the current buffer contents can 
                        //  be meaningful. Discard the whole thing.
                        packetBuffer.delete(0, packetBuffer.length());
                        sleep(5);
                    }
                } else {
                    sleep(5);
                }
            }
        }
        return retval;
    }
    
    /**
     * Private wrapper around the Thread.sleep method, to catch that interrupted error.
     * @param time_ms
     */
    private void sleep(int time_ms){
        try {
            Thread.sleep(time_ms);
        } catch (InterruptedException e) {
            CrashTracker.logAndPrint("[JeVois Interface] DO NOT WAKE THE SLEEPY BEAST");
            e.printStackTrace();
        }
    }
    
    /**
     * Mostly for debugging. Blocks execution forever and just prints all serial 
     * characters to the console. It might print a different message too if nothing
     * comes in.
     */
    public void blockAndPrintAllSerial(){
        if (visionPort != null){
            while(!Thread.interrupted()){
                if (visionPort.getBytesReceived() > 0) {
                    System.out.print(visionPort.readString());
                } else {
                    CrashTracker.logAndPrint("[JeVois Interface] Nothing Rx'ed");
                    sleep(100);
                }
            }
        }

    }
    
    /**
     * Parse individual numbers from a packet
     * @param pkt
     */
    public int parsePacket(String pkt, double rx_Time){
        //Parsing constants. These must be aligned with JeVois code.
        final int TGT_VISIBLE_TOKEN_IDX = 0;
        final int ANGLE_TO_TGT_TOKEN_IDX = 1;
        final int TGT_X_LOCATION_TOKEN_IDX  = 2;
        final int TGT_Y_LOCATION_TOKEN_IDX  = 3;
        final int LATCH_COUNTER_TOKEN_IDX = 4;
        final int JV_FRMRT_TOKEN_IDX = 5;
        final int JV_CPULOAD_TOKEN_IDX = 6;
        final int JV_CPUTEMP_TOKEN_IDX = 7;
        final int JV_PIPLINE_DELAY_TOKEN_IDX = 8;
        final int TGT_ANGLE_TOKEN_IDX = 9;
        final int NUM_EXPECTED_TOKENS = 10;

        //Split string into many substrings, presuming those strings are separated by commas
        String[] tokens = pkt.split(",");

        //Check there were enough substrings found
        if(tokens.length < NUM_EXPECTED_TOKENS){
            DriverStation.reportError("Got malformed vision packet. Expected " + NUM_EXPECTED_TOKENS + " tokens, but only found " + Integer.toString(tokens.length) + ". Packet Contents: " + pkt, false);
            return -1;
        }

        //Convert each string into the proper internal value
        try {
            
            //Boolean values should only have T or F characters
            if(tokens[TGT_VISIBLE_TOKEN_IDX].equals("F")){
                tgtVisible = false;
            } else if (tokens[TGT_VISIBLE_TOKEN_IDX].equals("T")) {
                tgtVisible = true;
            } else {
                DriverStation.reportError("Got malformed vision packet. Expected only T or F in " + Integer.toString(TGT_VISIBLE_TOKEN_IDX) + ", but got " + tokens[TGT_VISIBLE_TOKEN_IDX], false);
                return -1;
            }

            //Use Java built-in double to string conversion on most of the rest
            tgtAngle_rad    = Double.parseDouble(tokens[ANGLE_TO_TGT_TOKEN_IDX]);
            tgtXPos_ft    = Double.parseDouble(tokens[TGT_X_LOCATION_TOKEN_IDX]);
            tgtYPos_ft    = Double.parseDouble(tokens[TGT_Y_LOCATION_TOKEN_IDX]);
            latchCounter = (int)Math.round(Double.parseDouble(tokens[LATCH_COUNTER_TOKEN_IDX]));
            jeVoisFramerateFPS = Double.parseDouble(tokens[JV_FRMRT_TOKEN_IDX]);
            tgtTime  = rx_Time - Double.parseDouble(tokens[JV_PIPLINE_DELAY_TOKEN_IDX])/1000000.0;
            jeVoisCpuTempC   = Double.parseDouble(tokens[JV_CPUTEMP_TOKEN_IDX]);
            jeVoisCpuLoadPct = Double.parseDouble(tokens[JV_CPULOAD_TOKEN_IDX]);
            tgtGeneralAngle_deg = Double.parseDouble(tokens[TGT_ANGLE_TOKEN_IDX]);
            frameCounter++;

        } catch (Exception e) {
            DriverStation.reportError("Unhandled exception while parsing Vision packet: " + e.getMessage() + "\n" + e.getStackTrace(), false);
            return -1;
        }

        double sample_time_ms = Timer.getFPGATimestamp()*1000;
        tgtVisibleSig.addSample(sample_time_ms, tgtVisible);
        tgtAngleSig.addSample(sample_time_ms, tgtAngle_rad);
        tgtXPosSig.addSample(sample_time_ms, tgtXPos_ft);
        tgtYPosSig.addSample(sample_time_ms, tgtYPos_ft);
        tgtRotationSig.addSample(sample_time_ms, latchCounter);
        tgtCaptureTimeSig.addSample(sample_time_ms, tgtTime);
        jevoisCpuTempSig.addSample(sample_time_ms, jeVoisCpuTempC);
        jevoisCpuLoadSig.addSample(sample_time_ms, jeVoisCpuLoadPct);
        jevoisFramerateSig.addSample(sample_time_ms, jeVoisFramerateFPS);
        jevoisPacketsPerSecSig.addSample(sample_time_ms, packetRxRatePPS);
        framecounterSig.addSample(sample_time_ms, frameCounter);
        tgtGeneralAngleSig.addSample(sample_time_ms, tgtGeneralAngle_deg);
        jevoisLatchCounterSig.addSample(sample_time_ms, latchCounter);

        return 0;
    }
    
    
    /**
     * This thread runs a periodic task in the background to listen for vision camera packets.
     */
    Thread packetListenerThread = new Thread(new Runnable(){
        public void run(){
            while(!Thread.interrupted()){
                backgroundUpdate();   
            }
        }
    });
    
}