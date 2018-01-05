package com.qb.workstation.util;

public class NumberUtil {

    public static double sum(Double... values){
        if(values == null || values.length == 0)
            return 0;

        double sum = 0;
        for (Double value : values) {
            sum += (value == null ? 0 :value.doubleValue());
        }
        return sum;
    }

}
