![logo](https://user-images.githubusercontent.com/4583662/54397726-78099780-4685-11e9-8c16-db0be2048d60.jpg)

# RobotCasserole2019
Software for Robot Casserole's 2019 FIRST Deep Space competition season

## Contents
1. Driver view
2. Main Source Code
3. Log File Snagger & Log Viewer
4. Autonomous Line Follower

## 1. Driver View Website
The Driver View web site is used to help test and tweak our code, and used to help drive team during match. The driver view is a javascript/HTML based viewer of data logs captured from the robot during operation. These data logs are then used to tweak code before, during, and after competition. 

## 2. Main Source Code
The source code this year, included code for: raising and lowering an arm, intaking and ejecting cargo, grabbing cargo out of the intake and placing it, grabbing and placing hatches, and programming a west coast drive-train to move. If you care to look at it look [here](https://github.com/RobotCasserole1736/RobotCasserole2019/tree/master/Robotcode/RobotCode2019/src/main/java/frc/robot) 

## 3. Log File Snagger & Log Viewer
The log file snagger is a python script used to  communticate with the roborio and grab all csv logs in a certain directory and put them in a log viewer where we can view them when the robot does somthing wacky.

## 4. Autonomous Line Follower
So you know lines, right? It follows them. We have several sensors that sense the line and cause the robot to follow it from the back (of the robot). It also tells us what our angle is to the lines.  
