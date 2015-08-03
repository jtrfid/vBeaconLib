package com.yetwish.libs.distance;

import com.yetwish.libs.distance.altbeacon.DistanceCalculator;

/**
 * 使用Estimote测距算法的Calculator
 * Created by yetwish on 2015-04-16
 */

public class EstimoteCalculator implements DistanceCalculator {

    @Override
    public double calculateDistance(int txPower, double rssi) {
        if (rssi == 0) {
            return -1.0D;
        }

        double ratio = rssi / txPower;
        double rssiCorrection = 0.96D + Math.pow(Math.abs(rssi), 3.0D) % 10.0D / 150.0D;

        if (ratio <= 1.0D) {
            return (Math.pow(ratio, 9.98D) * rssiCorrection);
        }
        return ((0.103D + 0.89978D * Math.pow(ratio, 7.71D)) * rssiCorrection);
    }
}
