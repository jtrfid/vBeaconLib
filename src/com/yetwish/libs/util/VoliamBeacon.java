package com.yetwish.libs.util;

import com.estimote.sdk.Beacon;
import com.yetwish.libs.connection.VoliamUuid;


/**
 * 判断是否是voliam Beacon
 * Created by yetwish on 2015-04-16
 */

public class VoliamBeacon {

    public static boolean isVoliamBeacon(Beacon beacon){
        return beacon.getName().contains(Constants.PRE_NAME) || beacon.getProximityUUID().equalsIgnoreCase(VoliamUuid.VOLIAM_UUID.toString());
    }

}
