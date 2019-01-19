package frc.lib.SignalMath;

import java.util.Arrays;

/*
 *******************************************************************************************
 * Copyright (C) 2017 FRC Team 1736 Robot Casserole - www.robotcasserole.org
 *******************************************************************************************
 *
 * This software is released under the MIT Licence - see the license.txt
 *  file in the root of this repo.
 *
 * Non-legally-binding statement from Team 1736:
 *  Thank you for taking the time to read through our software! We hope you
 *   find it educational and informative! 
 *  Please feel free to snag our software for your own use in whatever project
 *   you have going on right now! We'd love to be able to help out! Shoot us 
 *   any questions you may have, all our contact info should be on our website
 *   (listed above).
 *  If you happen to end up using our software to make money, that is wonderful!
 *   Robot Casserole is always looking for more sponsors, so we'd be very appreciative
 *   if you would consider donating to our club to help further STEM education.
 */


/**
 * Class which implements a simple average filter. After initialization, the "filter()" method can
 * simply be called with the newest input, and the most recent filtered output will be returned.
 *
 */
public class AveragingFilter {

    private int N; // length of filter
    private double[] circ_buffer; // circular buffer to hold all values
    private double sum; // hold sum of all numbers in the buffer at all times.
    private int index; // "pointer" to the starting index in the buffer


    /**
     * Initialize all things needed for the averaging filter.
     * 
     * @param length The number of previous points to consider in the averaged output. Larger values
     *        will reduce noise more aggressively, but also induce more delay in the signal.
     * @param init_val The initial value the filter should output. Usually zero, but might be
     *        something else if you know more about your system than we do.
     */
    public AveragingFilter(int length, double init_val) {
        index = 0;
        N = length;
        // Allocate buffer
        circ_buffer = new double[N];
        // Fill the buffer with the initial value
        Arrays.fill(circ_buffer, init_val);
        // Calculate initial sum
        sum = N * init_val;
    }


    /**
     * Add a new input to the filter and get the current output from the filter
     * 
     * @param input new value to add to the filter
     * @return the present output filtered value
     */
    public double filter(double input) {
        // use the running-sum method to compute the average. Better cuz it's O(1) time and O(n)
        // memory.
        // Computing the sum from scratch is O(n) for both.
        sum -= circ_buffer[index];

        circ_buffer[index] = input;
        sum += circ_buffer[index];
        // Update index (circularly due to buffer)
        index = (index + 1) % N;
        // Return average = sum/length
        return sum / N;
    }
    
    public void reset(){
    	Arrays.fill(circ_buffer, 0.0);
    }

}