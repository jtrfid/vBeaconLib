package com.yetwish.libs.connection;

import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

/**
 * 用以获取电池信息 from service
 * Created by yetwish on 2015-03-27
 */

public class BatteryService implements BluetoothService {

    private final static String TAG = BatteryService.class.getSimpleName();
    private HashMap<UUID, BluetoothGattCharacteristic> characteristicsMap;
    private HashMap<UUID,byte[]> valuesMap;
    public BatteryService()
    {
        this.characteristicsMap = new HashMap<UUID, BluetoothGattCharacteristic>();
        this.valuesMap = new HashMap<UUID,byte[]>();
    }

    /**
     * 将电池属性存起来
     */
    public void processGattServices(List<BluetoothGattService> services)
    {
        for(BluetoothGattService service:services){
            if(VoliamUuid.BATTERY_SERVICE.equals(service.getUuid())){
                Log.w(TAG,"service uuid: "+service.getUuid().toString());
                //获取battery service
                for(BluetoothGattCharacteristic chars:service.getCharacteristics()){
                    if(VoliamUuid.BATTERY_UUID.equals(chars.getUuid())){
                        //获取 特征值 battery
                        characteristicsMap.put(chars.getUuid(),chars);
                        Log.w(TAG,"chars uuid: "+chars.getUuid().toString());
                    }
                }
            }
        }
    }

    /**
     * 获取电池量
     * @return
     */
    public Integer getBattery(){
        return valuesMap.containsKey(VoliamUuid.BATTERY_UUID)?
                Integer.valueOf(VoliamService.getUnsignedByte(valuesMap.get(VoliamUuid.BATTERY_UUID))):null;
    }

    /**
     * 更新各个chars 的值
     */
    public void update(BluetoothGattCharacteristic characteristic)
    {
        this.characteristicsMap.put(characteristic.getUuid(), characteristic);
        this.valuesMap.put(characteristic.getUuid(),characteristic.getValue());
    }

    /**
     * 获取所有可用的chars
     * @return
     */
    public Collection<BluetoothGattCharacteristic> getAvailableCharacteristics() {
        List chars = new ArrayList(this.characteristicsMap.values());
        chars.removeAll(Collections.singleton(null));
        return chars;
    }

}
