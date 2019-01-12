package frc.robot;

public class lineFolower {

    public boolean Sensor1;
    public boolean Sensor2;
    public boolean Sensor3;
    public boolean Sensor4;
    public boolean Sensor5;
    public boolean Sensor1_Prev;
    public boolean Sensor2_Prev;
    public boolean Sensor3_Prev;
    public boolean Sensor4_Prev;
    public boolean Sensor5_Prev;
    public double Sensor1Pos_In = 4;
    public double Sensor2Pos_In = 2;
    public double Sensor3Pos_In = 0;
    public double Sensor4Pos_In = -2;
    public double Sensor5Pos_In = -4;
    {

        

        if(Sensor1 == true && Sensor1_Prev == false){
            //turn left high
        }
        if(Sensor2 == true && Sensor2_Prev == false) {
            //turn left low
        }
        if(Sensor3 == true && Sensor3_Prev == false){
            //go forward
        }
        if(Sensor4 == true && Sensor4_Prev == false){
            //turn right low
        }
        if(Sensor5 == true && Sensor5_Prev == false){
            //turn right high
        }
        Sensor1_Prev = Sensor1;
        Sensor2_Prev = Sensor2;
        Sensor3_Prev = Sensor3;
        Sensor4_Prev = Sensor4;
        Sensor5_Prev = Sensor5;
    }
}