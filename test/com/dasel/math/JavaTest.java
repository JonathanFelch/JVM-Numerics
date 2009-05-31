package com.dasel.math;

import java.util.List;
import java.util.ArrayList;
import java.util.Collections;

/**
 * Created by IntelliJ IDEA.
 * User: Jonathan
 * Date: May 29, 2009
 * Time: 10:27:21 PM
 * To change this template use File | Settings | File Templates.
 */
public class JavaTest {

    public static List<Double> createrDistribution(int size) {
        List<Double> values = new ArrayList<Double>();
        double step = 1.0 / (size + 1);
        double draw = step;

        while (draw <= 0.50) {
          double value = StatUtil.getInvCDF(draw,false);
          values.add(value);
          values.add(-value);  
          draw += step;
        }

        return values;
    }

    public static Double test(List<Double> values) {
        long start = System.currentTimeMillis();
        double drift = 0.05 - 0.5 * 0.15 * 0.15;
        for (Double value : values) {
            double diffusion = 0.15 * value;
            double result = 100.0 * Math.exp(drift + diffusion);
        }
        double time = (System.currentTimeMillis() - start) / 1000.0;
        System.out.println("Time = " + time + " seconds");
        return time;
    }

    public static void main(String[] args) {
        List<Double> values = createrDistribution(1000000);
        long start = System.currentTimeMillis();
        List<Double> benchmarks = new ArrayList<Double>();
        for (int i = 0; i < 100; i++) {
            benchmarks.add(test(values));
        }
        double avg = (System.currentTimeMillis() - start) / 1000.0 / 100.0;
        double min = Collections.min(benchmarks);
        double max = Collections.max(benchmarks);
        System.out.println("Min: " + min + " Max: " + max + " Avg: " + avg);

    }
}


