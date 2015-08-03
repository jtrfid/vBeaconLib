package com.yetwish.libs.connection;

import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;

import java.util.List;

/**
 * Created by yetwish on 2015-03-27
 */

public interface BluetoothService {

    /**
     * 获取services各特征值
     * @param services
     */
    public void processGattServices(List<BluetoothGattService> services);
   
    /**
     * update the data of beacon's characteristic
     * @param characteristic
     */
    public void update(BluetoothGattCharacteristic characteristic);
}
