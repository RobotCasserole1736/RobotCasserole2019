package frc.robot;

public class Arm {

    private static Arm  singularInstance = null;

    public static synchronized Arm getInstance() {
		if ( singularInstance == null)
			singularInstance = new Arm();
        return singularInstance;
    }
    
    private Arm() {

    } 

}