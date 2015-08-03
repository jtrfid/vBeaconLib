package com.yetwish.libs.connection;

import java.util.UUID;

/**
 * 提供 Voliam的UUID集 ，包括各个service 和 characteristic 
 * Created by yetwish on 2015-03-27
 */


/*
#define IBEACON_SERV_UUID                 0x191A    // Service UUID
#define IBEACON_SYSTEM_ID_UUID            0x2B23    // MAC Address
#define IBEACON_LONGID_UUID               0x2B24    // LONGID
#define IBEACON_DATETIME_UUID             0x2B25    // datetime
#define IBEACON_MAJORID_UUID              0x2B26    // min
#define IBEACON_MINORID_UUID              0x2B27    // nid
#define IBEACON_BTADDRESS_UUID            0x2B28    // Software Revision String
#define IBEACON_FIRMWARE_REV_UUID         0x2B29    // firmware version
#define IBEACON_ADV_INT_UUID              0x2B2A    // adv khz
#define IBEACON_TXPOWER_UUID              0x2B2B    // 0~255  ->  -127 - 10
#define IBEACON_WORK_ONTIME_UUID          0x2B2C    // IDchange time 1min
#define IBEACON_NAME_UUID                 0x2B2E    // Device Name
 */
public class VoliamUuid {
    public final static UUID VOLIAM_UUID = UUID.fromString("FDA50693-A4E2-4FB1-AFCF-C6EB07647825");
    public final static UUID VOLIAM_SERVICE = UUID.fromString("0000191a-0000-1000-8000-00805f9b34fb");
    public final static UUID SYSTEM_ID_UUID = UUID.fromString("00002b23-0000-1000-8000-00805f9b34fb");
    public final static UUID LOGIN_UUID = UUID.fromString("00002b24-0000-1000-8000-00805f9b34fb");
    public final static UUID DATETIME_UUID = UUID.fromString("00002b25-0000-1000-8000-00805f9b34fb");
    public final static UUID MAJOR_UUID = UUID.fromString("00002b26-0000-1000-8000-00805f9b34fb");
    public final static UUID MINOR_UUID = UUID.fromString("00002b27-0000-1000-8000-00805f9b34fb");
    public final static UUID SOFTWARE_REVISION_UUID = UUID.fromString("00002b28-0000-1000-8000-00805f9b34fb");
    public final static UUID FIRMWARE_REVISION_UUID = UUID.fromString("00002b29-0000-1000-8000-00805f9b34fb");
    public final static UUID ADV_INT_UUID = UUID.fromString("00002b2a-0000-1000-8000-00805f9b34fb");
    public final static UUID TXPOWER_UUID= UUID.fromString("00002b2b-0000-1000-8000-00805f9b34fb");
    public final static UUID WORK_TIME_UUID = UUID.fromString("00002b2c-0000-1000-8000-00805f9b34fb");
    public final static UUID NAME_UUID = UUID.fromString("00002b2e-0000-1000-8000-00805f9b34fb");
    public final static UUID BATTERY_SERVICE = UUID.fromString("0000180f-0000-1000-8000-00805f9b34fb");
    public final static UUID BATTERY_UUID = UUID.fromString("00002a19-0000-1000-8000-00805f9b34fb");


}

/*

service uuid:
00001800-0000-1000-8000-00805f9b34fb
0000191a-0000-1000-8000-00805f9b34fb
0000180f-0000-1000-8000-00805f9b34fb
f000ffc0-0451-4000-b000-000000000000

characteristic uuid for service 1
00002a00-0000-1000-8000-00805f9b34fb
00002a01-0000-1000-8000-00805f9b34fb
00002a02-0000-1000-8000-00805f9b34fb
00002a03-0000-1000-8000-00805f9b34fb
00002a04-0000-1000-8000-00805f9b34fb

characteristic uuid for service 2
00002b23-0000-1000-8000-00805f9b34fb
..
00002b2f-0000-1000-8000-00805f9b34fb

characteristic uuid for service 3
00002a19-0000-1000-8000-00805f9b34fb

characteristic uuid for service 4
f000ffc1-0451-4000-b000-000000000000
f000ffc2-0451-4000-b000-000000000000
 */
