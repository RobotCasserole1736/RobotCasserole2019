package frc.robot.auto;

import edu.wpi.first.wpilibj.Timer;
import frc.lib.AutoSequencer.AutoEvent;
import frc.robot.Drivetrain;

public class RemoveSlack extends AutoEvent {

    double StartTime = 0;

    double thisLoopTime;

    @Override
    public void userStart() {
        StartTime = Timer.getFPGATimestamp();
    }

    @Override
    public void userUpdate() {
        thisLoopTime = Timer.getFPGATimestamp() - StartTime;
        Drivetrain.getInstance().setOpenLoopCmd(0.1, 0);
    }

    @Override
    public void userForceStop() {
        Drivetrain.getInstance().setOpenLoopCmd(0, 0);
    }

    @Override
    public boolean isTriggered() {
        return true;
    }

    @Override
    public boolean isDone() {
        return thisLoopTime > 0.25;
    }

}