package frc.robot;



public class Utils {

    public static double ctrlAxisScale(double input, double exp_scale, double deadzone){
        boolean inputIsNegative = false;
        double output = 0;

        // Track if the input was less than zero
        if(input < 0){
            inputIsNegative = true;
            input *= -1;
        }

        if(input < deadzone){
            //Input within deadzone range, set output to zero.
            output = 0;
        } else {
            //Input is outside deadzon range, calcualte output

            //Rescale input to 0-1 range
            input -= (1-deadzone);
            input *= 1/(1-deadzone);

            //Raise input to desired power
            input = Math.pow(input, exp_scale);
        }

        output = input;

        // re-apply input sign
        if(inputIsNegative){
            output *= -1;
        }

        return output;
    }

}