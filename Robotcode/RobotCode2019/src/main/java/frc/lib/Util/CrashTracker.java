package frc.lib.Util;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import java.util.UUID;

import edu.wpi.first.wpilibj.DriverStation;

public class CrashTracker {
	private static final UUID RUN_INSTANCE_UUID = UUID.randomUUID();
	
	static boolean hasLogged = false;
	
    private static String getMatchString() {
    	String retval= "";
        switch (DriverStation.getInstance().getMatchType()) {
	        case Practice:
	        	retval += "P";
	        case Qualification:
	        	retval += "Q";
	        case Elimination:
	        	retval += "E";
	        default:
	        	retval += "N";
        }
        retval += Integer.toString(DriverStation.getInstance().getMatchNumber());
        retval += "R";
        retval += Integer.toString(DriverStation.getInstance().getReplayNumber());
        
        return retval;
    }
    
	public static void logMatchInfo() {
		logMarker("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ START MATCH " + getMatchString() + "~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
	}
	
	public static void logRobotConstruction() {
        logMarker("robot construction - " + RUN_INSTANCE_UUID.toString());
    }
	
	public static void logRobotInit() {
		logMarker("robot init");
	}
	
	public static void logDisabledInit() {
		logMarker("disabled init");
		hasLogged = false;
	}
	
	public static void logDisabledPeriodic() {
		if(hasLogged = false) {
			logMarker("disabled periodic");
			hasLogged = true;
		}
	}
	
	public static void logAutoInit() {
		logMarker("auto init");
		hasLogged = false;
	}
	
	public static void logAutoPeriodic() {
		if(hasLogged = false) {
			logMarker("auto periodic");
			hasLogged = true;
		}
	}
	
	public static void logTeleopInit() {
		logMarker("teleop init");
		hasLogged = false; 
	}
	
	public static void logTeleopPeriodic() {
		if(hasLogged = false) {
			logMarker("teleop periodic");
			hasLogged = true;
		}
	}
	
	public static void logClassInitStart(Class class_in) {
		logMarker("[Class Init] Starting "+(class_in.getSimpleName()));
	}
	
	public static void logClassInitEnd(Class class_in) {
		logMarker("[Class Init] Finished "+(class_in.getSimpleName()));
	}

	public static void logAndPrint(String message) {
		System.out.println(message);
		logMarker(message);
	}

	public static void logGenericMessage(String message) {
			logMarker(message);
	}
	
	 public static void logThrowableCrash(Throwable throwable) {
	        logMarker("Exception", throwable);
	}
	
	private static void logMarker(String mark) {
	        logMarker(mark, null);
	}

	private static void logMarker(String mark, Throwable nullableException) {
		
	        try (PrintWriter writer = new PrintWriter(new FileWriter("/home/lvuser/crash_tracking.txt", true))) {
	        	writer.print("[" + getDateTimeString() + "]");
	            writer.print(" ");
	            writer.print(mark);
	            

	            if (nullableException != null) {
	                writer.print(", ");
	                nullableException.printStackTrace(writer);
	            }

	            writer.println();
	            writer.close();
	        } catch (IOException e) {
	            e.printStackTrace();
	        }
	}

	private static String getDateTimeString() {
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        df.setTimeZone(TimeZone.getTimeZone("US/Central"));
        return df.format(new Date());
    }
}
