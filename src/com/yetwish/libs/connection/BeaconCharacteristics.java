package com.yetwish.libs.connection;

import com.estimote.sdk.internal.Objects;

/**
 * beacon基站 特征值类  用于存放voliam beacon基站的所有特征值， 当连接到基站时创建
 * 用来将ble device characteristics 封装为 beacon characteristics
 * Created by yetwish on 2015-04-03
 */
public class BeaconCharacteristics {
    private final Integer batteryPercent;
    private final Byte txPower;
    private final Integer advertisingIntervalMillis;
    private final String softwareVersion;
    private final String firmwareVersion;
    private final String systemId;
    private final String loginId;
    private final String datetime;
    private final String work_on_time;
    private final String majorId;
    private final String minorId;
    private final String name;

    public BeaconCharacteristics(VoliamService voliamService, BatteryService batteryService) {
        this.batteryPercent = batteryService.getBattery();
        this.txPower = voliamService.getTxPowerDBM();
        this.advertisingIntervalMillis = voliamService.getAdvertisingIntervalMillis();
        this.softwareVersion = voliamService.getSoftwareVersion();
        this.firmwareVersion = voliamService.getFirmwareVersion();
        this.systemId = voliamService.getSystemId();
        this.loginId = voliamService.getLoginId();
        this.datetime = voliamService.getDateTime();
        this.work_on_time = voliamService.getWorkOnTime();
        this.majorId = voliamService.getMajorId();
        this.minorId = voliamService.getMinorId();
        this.name = voliamService.getName();
    }

    public Integer getBatteryPercent() {
        return batteryPercent;
    }

    public Byte getTxPower() {
        return txPower;
    }

    public Integer getAdvertisingIntervalMillis() {
        return advertisingIntervalMillis;
    }

    public String getSoftwareVersion() {
        return softwareVersion;
    }

    public String getFirmwareVersion() {
        return firmwareVersion;
    }

    public String getSystemId() {
        return systemId;
    }

    public String getLoginId() {
        return loginId;
    }

    public String getDatetime() {
        return datetime;
    }

    public String getWork_on_time() {
        return work_on_time;
    }

    public String getMajorId() {
        return majorId;
    }

    public String getMinorId() {
        return minorId;
    }

    public String getName() {
        return name;
    }

    public String toString() {
        return Objects.toStringHelper(this).add("batteryPercent", this.batteryPercent)
                .add("txPower", this.txPower).add("advertisingIntervalMillis", this.advertisingIntervalMillis)
                .add("softwareVersion", this.softwareVersion).add("firmwareVersion", this.firmwareVersion)
                .add("systemId",this.systemId).add("loginId",this.loginId).add("datetime",this.datetime)
                .add("workOnTime",this.work_on_time).add("name",this.name).toString();
    }
}

