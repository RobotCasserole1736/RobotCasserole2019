clear;



function result = deg2rad(input)
  result = input * pi/180;
endfunction

function result = rad2deg(input)
  result = input * 180/pi;
endfunction


%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%% Simulation Constants - edit here
global Ts = 0.001; %1ms sample rate for solver
global SIM_END_TIME_SEC = 2.0;

%Mechanical Constraints
ARM_MASS_LENGTH_FT = 2.0;
ARM_MASS_LBS = 25.0;
FRICTION_CONSTANT = 0.100;

%Motor & electrical Constraints
BATTERY_VOLTAGE_V = 12.6;
GEARBOX_RATIO = 300.0/1.0; %Motor spins 100x faster than arm
MOTOR_MAX_SPEED_RPM = 5676;
MOTOR_STALL_TORQUE_NM = 2.6;
MOTOR_STALL_CURRENT_A = 105;
MOTOR_FREE_CURRENT_A = 1.8;

% Position Command
POS_INITIAL = -45;
TARGET_POS = 45;

% Controller Tuning
K_GRAVITY_COMP = 0.08;

%% Display Constants
MASS_DRAW_RAD_FT = 0.1;

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%Calculated constants
ARM_MASS_LENGTH_M = ARM_MASS_LENGTH_FT*0.3048;
GRAVITY_M_PER_SEC_2 = 9.81;
ARM_MASS_KG = ARM_MASS_LBS*0.453592;

MOTOR_KI = MOTOR_STALL_TORQUE_NM/MOTOR_STALL_CURRENT_A; %NM per amp
MOTOR_WINDING_RESISTANCE_OHM = 12/MOTOR_STALL_CURRENT_A;
MOTOR_MAX_SPEED_DEG_PER_SEC = MOTOR_MAX_SPEED_RPM*6.0;
MOTOR_KV = (12-MOTOR_FREE_CURRENT_A*MOTOR_WINDING_RESISTANCE_OHM)/MOTOR_MAX_SPEED_DEG_PER_SEC; %Volts per deg/sec


i = 1; %simulation step

%internal Variables
time = [0:SIM_END_TIME_SEC/Ts].*Ts;
motor_cmd = zeros([1,SIM_END_TIME_SEC/Ts+1]);
motor_torque = zeros([1,SIM_END_TIME_SEC/Ts+1]);
gravity_torque = zeros([1,SIM_END_TIME_SEC/Ts+1]);
arm_torque = zeros([1,SIM_END_TIME_SEC/Ts+1]);
arm_theta_doubledot = zeros([1,SIM_END_TIME_SEC/Ts+1]);
arm_theta_dot = zeros([1,SIM_END_TIME_SEC/Ts+1]);
arm_theta = zeros([1,SIM_END_TIME_SEC/Ts+1]);
friction_torque = zeros([1,SIM_END_TIME_SEC/Ts+1]);
motor_current = zeros([1,SIM_END_TIME_SEC/Ts+1]);

%Initial conditions
arm_theta(1) = POS_INITIAL;
arm_theta_dot(1) = 0;
arm_theta_doubledot(1) = 0;


% All torques in NM

close("all")


%Sample controller impelmentations

function cmd = bangBang(idx, actual_pos, des_pos)
  if(actual_pos - des_pos > 1.0)
    cmd = -1;
  elseif(actual_pos - des_pos < -1.0)
    cmd = 1;
  else
    cmd = 0;
  endif
endfunction


function cmd = trapezoid_piv(idx, actual_pos, des_pos, actual_vel, actual_accel)

  global Ts;
  global SIM_END_TIME_SEC;
  
  %init
  persistent  error = zeros([1,SIM_END_TIME_SEC/Ts+1]);
  persistent  pos_cmd = ones([1,SIM_END_TIME_SEC/Ts+1]).*des_pos;  
  persistent  vel_cmd = zeros([1,SIM_END_TIME_SEC/Ts+1]); 
  persistent  accel_cmd = zeros([1,SIM_END_TIME_SEC/Ts+1]); 
   

  KP = 1.0;
  KD = 0.01;
  KV = 0.01;
  KA = 0.00;

  MAX_VEL_DEG_PER_SEC = 110;
  MAX_ACCEL_DEG_PER_SEC2 = MAX_VEL_DEG_PER_SEC*4;
  
  TIME_TO_MAX_VEL = MAX_VEL_DEG_PER_SEC/MAX_ACCEL_DEG_PER_SEC2;
  DISTANCE_TRAVERSED_DURING_ACCEL = 0.5*TIME_TO_MAX_VEL*MAX_VEL_DEG_PER_SEC;

  if(idx == 2)

    %Calc Profile
    pos_cmd(1) = actual_pos;
    vel_cmd(1) = 0;
    accel_cmd(1) = MAX_ACCEL_DEG_PER_SEC2;
    
    Taccel =  TIME_TO_MAX_VEL;
    Ttravel = Taccel + ((abs(actual_pos - des_pos) - 2*DISTANCE_TRAVERSED_DURING_ACCEL)/MAX_VEL_DEG_PER_SEC);
    Tdecel =  Ttravel + (TIME_TO_MAX_VEL);
    
    if(Ttravel > 0)
    
      %Accel portion
      for i=2:round(Taccel/Ts)
        accel_cmd(i) = MAX_ACCEL_DEG_PER_SEC2;
        vel_cmd(i) = vel_cmd(i-1) + accel_cmd(i)*Ts;
        pos_cmd(i) = pos_cmd(i-1) + vel_cmd(i)*Ts;
      endfor
      
       %Travel portion
      for i=round(Taccel/Ts+1):round(Ttravel/Ts)
        accel_cmd(i) = 0;
        vel_cmd(i) = vel_cmd(i-1) + accel_cmd(i)*Ts;
        pos_cmd(i) = pos_cmd(i-1) + vel_cmd(i)*Ts;
      endfor
      
      %Decel portion
      for i=round(Ttravel/Ts+1):round(Tdecel/Ts)
        accel_cmd(i) = -MAX_ACCEL_DEG_PER_SEC2;
        vel_cmd(i) = vel_cmd(i-1) + accel_cmd(i)*Ts;
        pos_cmd(i) = pos_cmd(i-1) + vel_cmd(i)*Ts;
      endfor
      
      %Account for numeric errors ??
      for i=round(Tdecel/Ts+1):round((Tdecel+1.0)/Ts)
        accel_cmd(i) = 0;
        vel_cmd(i) = 0;
        pos_cmd(i) = pos_cmd(i-1) + (des_pos - pos_cmd(i-1))*(Ts/1.0);
      endfor
      
    else
      # TODO, what happens if you need a triange?
    endif
    
  endif
  
  if(idx==400)
    figure(2)
    plot(pos_cmd)
  endif
  
  error(idx) = (pos_cmd(idx) - actual_pos);
  
  cmd = KP * (error(idx)) + KD * (((error(idx) - error(idx-1))/Ts)-vel_cmd(idx)) + KV * vel_cmd(idx) + KA * accel_cmd(idx);

endfunction


%calculate trajectory until terminal case (ball hits floor or ball hits castle wall)
while( time(i) < SIM_END_TIME_SEC)

  i = i + 1;
  
  %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
  %% CONTROLLER
  %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
  
  %% Open Loop Step Input
  %motor_cmd(i) = interp1([0,0.1,0.2,0.3,0.4,0.5,SIM_END_TIME_SEC],[0,0,0.1,0.5,0.9,1,1],time(i),'cubic');
  
  %% Bang-bang controller
  motor_cmd(i) = bangBang(i, arm_theta(i-1), TARGET_POS);
  
  %% Trappezoid and PIV
  %motor_cmd(i) = trapezoid_piv(i, arm_theta(i-1), TARGET_POS, arm_theta_dot(i-1), arm_theta_doubledot(i-1));
  
  
  
  % Gravity Compensation
  motor_cmd(i) = motor_cmd(i) + K_GRAVITY_COMP * abs(cos(deg2rad(arm_theta(i-1))));
  
  
  %Cap motor command at physical limits
  if(motor_cmd(i) > 1)
    motor_cmd(i) = 1;
  elseif(motor_cmd(i) < -1)
    motor_cmd(i) = -1;
  endif



  
  %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
  %% PLANT
  %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
  

  %Calculate motor (applied control) torque
  vMotor = (BATTERY_VOLTAGE_V*(motor_cmd(i)));
  motor_vel = arm_theta_dot(i-1)*GEARBOX_RATIO;
  motor_current(i) = ((vMotor) - MOTOR_KV*motor_vel)/MOTOR_WINDING_RESISTANCE_OHM;
  motor_torque(i) = motor_current(i)*MOTOR_KI*GEARBOX_RATIO;
  
  %Calculate other torques
  friction_torque(i) = -1*FRICTION_CONSTANT*arm_theta_dot(i-1);
  
  gravity_force_N = -1*ARM_MASS_KG*GRAVITY_M_PER_SEC_2;
  gravity_torque(i) = (ARM_MASS_LENGTH_M*gravity_force_N*cos(deg2rad(arm_theta(i-1))));
  
  %Angular Acceleration from sum of torques
  arm_torque(i) = gravity_torque(i) + motor_torque(i) + friction_torque(i);
  arm_theta_doubledot(i) = rad2deg((arm_torque(i))/((ARM_MASS_LENGTH_M**2)*ARM_MASS_KG));
  
  
  %Integrate Acceleration to get velocity & angle
  arm_theta_dot(i) = arm_theta_dot(i-1) + arm_theta_doubledot(i)*Ts;
  arm_theta(i) = arm_theta(i-1) + arm_theta_dot(i)*Ts;

endwhile


%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%% Generate output plots
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%



figure(1, 'position',[100,100,1400,900]);
subplot(2,3,1)
hold on
title('Position Error (deg)');
plot(time, arm_theta .- TARGET_POS );
hold off

subplot(2,3,2)
hold on
title('Motor Cmd');
plot(time, motor_cmd);
hold off

subplot(2,3,3)
hold on
title('Torque (Nm)');
plot(time, arm_torque, time, motor_torque, time, gravity_torque);
legend('Arm Torque', 'Motor Torque', 'Gravity Torque');
hold off

subplot(2,3,4)
hold on
title('Motor Current (A)');
plot(time, motor_current);
hold off

subplot(2,3,5)
hold on
title('Motor Speed (RPM)');
plot(time, arm_theta_dot.*(GEARBOX_RATIO/6));
hold off

%Comment me out to get animation
return

figure();
%draw arm mass & line

for i = floor(linspace(1, SIM_END_TIME_SEC/Ts))
  clf;
  axis([-ARM_MASS_LENGTH_FT,ARM_MASS_LENGTH_FT,-ARM_MASS_LENGTH_FT,ARM_MASS_LENGTH_FT].*1.5)
  axis equal

  arm_mass_x = ARM_MASS_LENGTH_FT*cos(deg2rad(arm_theta(i)));
  arm_mass_y = ARM_MASS_LENGTH_FT*sin(deg2rad(arm_theta(i)));
  line ([0, arm_mass_x], [0 , arm_mass_y], "linewidth", 2, "color", "b");
  rectangle('Position', [arm_mass_x-MASS_DRAW_RAD_FT,arm_mass_y-MASS_DRAW_RAD_FT,2*MASS_DRAW_RAD_FT,2*MASS_DRAW_RAD_FT], 'FaceColor', [0.5, 0.5, 0.5], 'Curvature', [1, 1]);

  pause(0.00001)
end



