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
 * get Characteristic from Discovered Services and get services' characteristics:
 * battery、 major、 minor、power 、advertising_interval char and so on.
 * supposed an api for developer to update the characteristics of the connected beacon.
 * Created by yetwish on 2015-03-27
 */


public class VoliamService implements BluetoothService {

    private final static String TAG = VoliamService.class.getSimpleName();

    /**
     * 存储UUID 与 characteristic 的对应关系
     */
    private HashMap<UUID, BluetoothGattCharacteristic> characteristicsMap;
    /**
     * 存储UUID 与 writeCallBack的对应关系
     */
    private HashMap<UUID, BeaconConnector.WriteCallback> writeCallbacksMap;
    /**
     * 存储characteristic UUID 与 value间对应的关系
     */
    private HashMap<UUID, byte[]> valuesMap;

    public VoliamService() {
        characteristicsMap = new HashMap<UUID, BluetoothGattCharacteristic>();
        writeCallbacksMap = new HashMap<UUID, BeaconConnector.WriteCallback>();
        valuesMap = new HashMap<UUID, byte[]>();
    }

    /**
     * get characteristics from discovered services and process the service.
     */
    public void processGattServices(List<BluetoothGattService> services) {
        for (BluetoothGattService service : services) {
            if (VoliamUuid.VOLIAM_SERVICE.equals(service.getUuid())) {
                //Log.d(TAG,"service uuid: "+service.getUuid().toString());
//                characteristicsMap.put(VoliamUuid.SYSTEM_ID_UUID, service.getCharacteristic(VoliamUuid.SYSTEM_ID_UUID));
//                characteristicsMap.put(VoliamUuid.LOGIN_UUID, service.getCharacteristic(VoliamUuid.LOGIN_UUID));
//                characteristicsMap.put(VoliamUuid.DATETIME_UUID, service.getCharacteristic(VoliamUuid.DATETIME_UUID));
//                characteristicsMap.put(VoliamUuid.MAJOR_UUID, service.getCharacteristic(VoliamUuid.MAJOR_UUID));
//                characteristicsMap.put(VoliamUuid.MINOR_UUID, service.getCharacteristic(VoliamUuid.MINOR_UUID));
//                characteristicsMap.put(VoliamUuid.SOFTWARE_REVISION_UUID, service.getCharacteristic(VoliamUuid.SOFTWARE_REVISION_UUID));
                characteristicsMap.put(VoliamUuid.FIRMWARE_REVISION_UUID, service.getCharacteristic(VoliamUuid.FIRMWARE_REVISION_UUID));
                characteristicsMap.put(VoliamUuid.ADV_INT_UUID, service.getCharacteristic(VoliamUuid.ADV_INT_UUID));
                characteristicsMap.put(VoliamUuid.TXPOWER_UUID, service.getCharacteristic(VoliamUuid.TXPOWER_UUID));
//                characteristicsMap.put(VoliamUuid.WORK_TIME_UUID, service.getCharacteristic(VoliamUuid.WORK_TIME_UUID));
//                characteristicsMap.put(VoliamUuid.NAME_UUID, service.getCharacteristic(VoliamUuid.NAME_UUID));
            }
        }
    }

    /**
     * 判断voliamService是否含有 指定UUID对应的characteristic
     *
     * @param uuid ,characteristic 的uuid 
     * @return boolean ,是否存在
     */
    public boolean hasCharacteristic(UUID uuid) {
        return characteristicsMap.containsKey(uuid);
    }

    /**
     * @return String: beacon名称
     */
    public String getName() {
        Log.w(TAG, "NAME: " + valuesMap.get(VoliamUuid.NAME_UUID));
        return this.valuesMap.containsKey(VoliamUuid.NAME_UUID) ?
                getStringValue(valuesMap.get(VoliamUuid.NAME_UUID)) : null;
    }

    /**
     * @return String :工作时间段
     */
    public String getWorkOnTime() {
        Log.w(TAG, "WORK ON TIME: " + valuesMap.get(VoliamUuid.WORK_TIME_UUID));
        return this.valuesMap.containsKey(VoliamUuid.WORK_TIME_UUID) ?
                bytesToHex(valuesMap.get(VoliamUuid.WORK_TIME_UUID)): null;

    }

    /**
     * @return String :minor id
     */
    public String getMinorId() {
        Log.w(TAG, "MINOR: " + bytesToHex(valuesMap.get(VoliamUuid.MINOR_UUID)));
        return this.valuesMap.containsKey(VoliamUuid.MINOR_UUID) ?
                valuesMap.get(VoliamUuid.MINOR_UUID).toString() : null;
    }

    /**
     * @return String :major id
     */
    public String getMajorId() {
        Log.w(TAG, "MAJOR: " + bytesToHex(valuesMap.get(VoliamUuid.MAJOR_UUID)));
        return this.valuesMap.containsKey(VoliamUuid.MAJOR_UUID) ?
                valuesMap.get(VoliamUuid.MAJOR_UUID).toString(): null;
    }

    /**
     * @return String :当前时间
     */
    public String getDateTime() {
        Log.w(TAG, "DATETIME " + valuesMap.get(VoliamUuid.DATETIME_UUID));
        return this.valuesMap.containsKey(VoliamUuid.DATETIME_UUID) ?
                valuesMap.get(VoliamUuid.DATETIME_UUID).toString() : null;
    }

    /**
     * @return String :login id
     */
    public String getLoginId() {
        Log.w(TAG, "LOGIN_UUID" + valuesMap.get(VoliamUuid.LOGIN_UUID));
        return this.valuesMap.containsKey(VoliamUuid.LOGIN_UUID) ?
                bytesToHex(valuesMap.get(VoliamUuid.LOGIN_UUID)): null;
    }

    /**
     * @return String :system id
     */
    public String getSystemId() {
        Log.w(TAG, "SYSTEM ID: " + valuesMap.get(VoliamUuid.SYSTEM_ID_UUID));
        return this.valuesMap.containsKey(VoliamUuid.SYSTEM_ID_UUID) ?
                bytesToHex(valuesMap.get(VoliamUuid.SYSTEM_ID_UUID)) : null;
    }

    /**
     * @return String : software revision
     */
    public String getSoftwareVersion() {
        Log.w(TAG, "SOFT VERSION: " + valuesMap.get(VoliamUuid.SOFTWARE_REVISION_UUID));
        return this.valuesMap.containsKey(VoliamUuid.SOFTWARE_REVISION_UUID) ?
                bytesToHex(valuesMap.get(VoliamUuid.SOFTWARE_REVISION_UUID)) : null;
    }

    /**
     * @return String : firmware revision
     */
    public String getFirmwareVersion() {
        Log.w(TAG, "FIRMWARE VERSION: " + valuesMap.get(VoliamUuid.FIRMWARE_REVISION_UUID));
        return this.valuesMap.containsKey(VoliamUuid.FIRMWARE_REVISION_UUID) ?
                bytesToHex(valuesMap.get(VoliamUuid.FIRMWARE_REVISION_UUID)) : null;
    }


    /**
     * @return byte : txpower dbm
     */
    public Byte getTxPowerDBM() {
        Log.w(TAG, "power: " + valuesMap.get(VoliamUuid.TXPOWER_UUID));
        return this.valuesMap.containsKey(VoliamUuid.TXPOWER_UUID) ?
                Byte.valueOf(valuesMap.get(VoliamUuid.TXPOWER_UUID)[0]) : null;
    }

    /**
     * @return String : advertising_interval
     */
    public Integer getAdvertisingIntervalMillis() {
        Log.w(TAG, "adv_int: " + valuesMap.get(VoliamUuid.ADV_INT_UUID));
        return this.valuesMap.containsKey(VoliamUuid.ADV_INT_UUID) ?
                Integer.valueOf(Math.round(getUnsignedInt16(valuesMap.get(VoliamUuid.ADV_INT_UUID)) * 0.625F)) : null;
    }

    /**
     * 更新特征值，给特征值写入值
     *
     * @param characteristic 要更新的特征值
     */
    public void update(BluetoothGattCharacteristic characteristic) {
        Log.d(TAG, "-----update " + characteristic.getUuid().toString());
        this.characteristicsMap.put(characteristic.getUuid(), characteristic);
        this.valuesMap.put(characteristic.getUuid(), characteristic.getValue());
        Log.w(TAG, characteristic.getValue().toString());

    }

    /**
     * @return 返回voliam service的所有characteristic的集合
     */
    public Collection<BluetoothGattCharacteristic> getAvailableCharacteristics() {
        List chars = new ArrayList(this.characteristicsMap.values());
        chars.removeAll(Collections.singleton(null));
        return chars;
    }


    private static int getUnsignedInt16(byte[] bytes) {
        return (unsignedByteToInt(bytes[0]) + (unsignedByteToInt(bytes[1]) << 8));
    }

    public BluetoothGattCharacteristic beforeCharacteristicWrite(UUID uuid, BeaconConnector.WriteCallback callback) {
        this.writeCallbacksMap.put(uuid, callback);
        return characteristicsMap.get(uuid);
    }

    public void onCharacteristicWrite(BluetoothGattCharacteristic characteristic, int status) {
        BeaconConnector.WriteCallback writeCallback = writeCallbacksMap.remove(characteristic.getUuid());
        if (status == 0)
            writeCallback.onWriteSuccess();
        else
            writeCallback.onWriteError();
    }


    public static int getUnsignedByte(byte[] bytes) {
        return unsignedByteToInt(bytes[0]);
    }

    private static int unsignedByteToInt(byte value) {
        return (value & 0xFF);
    }


    private static String getStringValue(byte[] bytes) {
        int indexOfFirstZeroByte = 0;
        while (indexOfFirstZeroByte<bytes.length && bytes[indexOfFirstZeroByte] != 0) {
            ++indexOfFirstZeroByte;
        }

        byte[] strBytes = new byte[indexOfFirstZeroByte];
        for (int i = 0; i != indexOfFirstZeroByte; ++i) {
            strBytes[i] = bytes[i];
        }

        return new String(strBytes);
    }

    public static String bytesToHex(byte[] paramArrayOfByte) {
        if (paramArrayOfByte == null) return null;
        char[] arrayOfChar2;
        char[] arrayOfChar1 = "0123456789ABCDEF".toCharArray();
        arrayOfChar2 = new char[2 * paramArrayOfByte.length];
        if (arrayOfChar2 != null)
            for (int i = 0; ; ++i) {
                if (i >= paramArrayOfByte.length)
                    return new String(arrayOfChar2);
                int j = 0xFF & paramArrayOfByte[i];
                arrayOfChar2[(i * 2)] = arrayOfChar1[(j >>> 4)];
                arrayOfChar2[(1 + i * 2)] = arrayOfChar1[(j & 0xF)];
            }
        return arrayOfChar2.toString();
    }

}
