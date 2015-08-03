package com.yetwish.libs.distance;


import java.util.List;

import com.estimote.sdk.Beacon;
import com.yetwish.libs.distance.altbeacon.DistanceCalculator;
import com.yetwish.libs.distance.altbeacon.ModelSpecificDistanceCalculator;
import com.yetwish.libs.util.Constants;
import com.yetwish.libs.util.ContextUtil;
import com.yetwish.libs.util.LogUtil;
import com.yetwish.libs.util.VoliamBeacon;

/**
 * 工具类，用以获取单个Beacon设备的距离，以及获取一组Beacons中距离最小的那个Beacon
 * Created by yetwish on 2015-03-26
 */

public class DistanceUtils {
    //LOG TAG
    public final static String TAG = DistanceUtils.class.getSimpleName();

    protected static String distanceModelUpdateUrl = "http://data.altbeacon.org/android-distance.json";

    /**
     * 接受广播的距离，默认是 1.0
     */
    private static double broadcastDistance = 1.0D;

    
    /**
     * 调用该方法，改变接收广播距离
     * @param distance : double
     */
    public static void setBroadcastDistance(double distance){
        DistanceUtils.broadcastDistance = distance;
    }

    /**
     * 获取接收广播的距离
     * @return the distance to receive the beacon
     */
    public static double getBroadcastDistance(){
        return broadcastDistance;
    }

    /**
     * 获取一组Beacons 中距离最小的Beacon，通过获取N次最小，取N次中出现最多次数的Beacon
     * @param beacons 一组beacons
     * @return beacon 一组中距离最短的Beacon
     */
    public static Beacon getMinDistanceBeacon(List<Beacon> beacons){
        if (beacons.size()==0) return null;
        int index = 0;
        double min = Constants.INIT_DISTANCE;
        for (int j = 0; j < beacons.size(); j++){
            // to comfirm that the beacons are voliam beacon
            if (!VoliamBeacon.isVoliamBeacon(beacons.get(j))) continue;
            double distance = calculateDistance(beacons.get(j));
            LogUtil.w(""+distance);
            if( distance < min){
                min = distance;
                index = j;
            }
        }
        return beacons.get(index);
    }

    /**
     * altBeacon 定位算法器
     */

    private static DistanceCalculator defaultDistanceCalculator = null;

    /**
     * 设置测距算法器
     * @param dc 要设置的测距算法器
     */
    public static void setDistanceCalculator(DistanceCalculator dc){
        defaultDistanceCalculator = dc;
    }

    /**
     * 调用altBeacon中的算法器的calculateDistance
     * @param beacon the device to be computed distance;
     * @return double : the distance of beacon
     */
    public static double calculateDistance(Beacon beacon){
    	if(beacon == null) return -1;
        if(defaultDistanceCalculator == null)
            defaultDistanceCalculator = getCalculator();
        double distance = defaultDistanceCalculator.calculateDistance(beacon.getMeasuredPower(),beacon.getRssi());
        //LogUtil.w("Get Distance:: "+beacon.getName() + " ,RSSI: " + beacon.getRssi() + " ,distance: " + distance);
        return distance;
    }

    private static DistanceCalculator getCalculator(){
        return new ModelSpecificDistanceCalculator(ContextUtil.getGlobalApplicationContext(),distanceModelUpdateUrl);
    }


}
