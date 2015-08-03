package com.yetwish.libs.distance;

import com.yetwish.libs.distance.altbeacon.DistanceCalculator;

/**
 * 自定义Calculator
 * Created by yetwish on 2015-04-16
 */

public class CustomerCalculator implements DistanceCalculator{

    @Override
    public double calculateDistance(int txPower, double rssi) {
        if (rssi == 0.0D)
            return  -1.0D;
        double ratio = rssi * 1.0D / txPower;

        if (ratio < 1.0D)
            return Math.pow(ratio, 10.0D);
        return 0.111D + 0.89976D * Math.pow(ratio, 7.7095D);
    }
}
