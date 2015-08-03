package com.yetwish.libs.connection;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.os.Handler;
import android.util.Log;

import com.estimote.sdk.Beacon;
import com.estimote.sdk.Utils;
import com.estimote.sdk.internal.HashCode;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * 该类封装与ble remote server 建立连接的代码细节，对外提供ConnectionCallback接口，
 * 一旦与remote server建立好连接，会调用connectionCallback.onConnected(BeaconCharacteristic characteristics)方法
 * 开发者通过实现该方法取得remote server 's characteristics,如电量等
 * Created by yetwish on 2015-03-27
 */

public class BeaconConnector {

    private final static String TAG = BeaconConnector.class.getSimpleName();
    /**
     * 允许的txPower的值的集合
     */
    public final static Set<Integer> ALLOWED_POWER_LEVELS = Collections.unmodifiableSet(new HashSet(
            Arrays.asList(new Integer[]{Integer.valueOf(-30), Integer.valueOf(-20), Integer.valueOf(-16),
                    Integer.valueOf(-12), Integer.valueOf(-8), Integer.valueOf(-4), Integer.valueOf(0), Integer.valueOf(4)})));
    private Context context;
    /**
     * 本地蓝牙设备对象
     */
    private BluetoothDevice device;
    /**
     * 连接回调对象，用以传递
     */
    private ConnectionCallback connectionCallback;
    /**
     * 蓝牙管理器，用以获取蓝牙基站的信息（地址与连接状态等）
     */
    private BluetoothManager bluetoothManager;
    /**
     * gatt对象 远程蓝牙设备代理。与远程建立连接后取得该对象
     */
    private BluetoothGatt bluetoothGatt;
    /**
     * gatt回调对象，与远程设备建立连接时，通过传入该对象以实现 连接过程的具体操作
     */
    private BluetoothGattCallback bluetoothGattCallback;
    /**
     * 用以推送/清除 延时操作线程
     */
    private Handler handler;
    /**
     * 延时操作线程
     */
    private Runnable timeOutHandler;
    /**
     * 判断是否已读完beacon的特征值
     */
    private boolean didReadCharacteristic;
    /**
     * VoliamBeacon Service之一
     * 封装Beacon存放 系统版本等特征值的service
     */
    private VoliamService voliamService;
    /**
     * VoliamBeacon Service之一
     * 封装Beacon存放电池信息的service
     */
    private BatteryService batteryService;
    /**
     * 用以存放扫描到的设备 所有符合voliam标准的 characteristics对象
     */
    private LinkedList<BluetoothGattCharacteristic> toFetch;
    /**
     * 存放uuid 到Service的映射关系
     */
    private Map<UUID, BluetoothService> uuidToServiceMap;


    public BeaconConnector(Context context, Beacon beacon, ConnectionCallback callback) {
        this.context = context;
        this.bluetoothManager = (BluetoothManager) this.context.getSystemService(Context.BLUETOOTH_SERVICE);
        this.device = deviceFromBeacon(beacon);
        this.connectionCallback = callback;
        this.handler = new Handler();
        this.timeOutHandler = createTimeOutHandler();
        this.bluetoothGattCallback = createBluetoothCallback();
        this.voliamService = new VoliamService();
        this.batteryService = new BatteryService();
        this.toFetch = new LinkedList<BluetoothGattCharacteristic>();
        this.uuidToServiceMap = new HashMap<UUID, BluetoothService>();
        this.uuidToServiceMap.put(VoliamUuid.VOLIAM_SERVICE, voliamService);
        this.uuidToServiceMap.put(VoliamUuid.BATTERY_SERVICE, batteryService);
    }


    private BluetoothDevice deviceFromBeacon(Beacon beacon) {
        BluetoothAdapter bluetoothAdapter = bluetoothManager.getAdapter();
        return bluetoothAdapter.getRemoteDevice(beacon.getMacAddress());
    }

    /**
     * 判断connector 是否已经与gatt server 建立连接
     * @return
     */
    public boolean isConnected() {
        int connectionState = bluetoothManager.getConnectionState(this.device, BluetoothProfile.GATT);
        return connectionState == BluetoothProfile.STATE_CONNECTED && didReadCharacteristic;
    }

    /**
     * 程序入口，调用该API实现与gatt server连接
     */
    public void connectToBeacon() {
        Log.d(TAG, "starting connect");
        didReadCharacteristic = false;
        bluetoothGatt = device.connectGatt(context, false, bluetoothGattCallback);
        handler.postDelayed(timeOutHandler, TimeUnit.SECONDS.toMillis(10L));
    }

    /**
     * 调用该API 关闭连接
     */
    public void close() {
        if (bluetoothGatt != null) {
            this.bluetoothGatt.disconnect();
            this.bluetoothGatt.close();
        }
        handler.removeCallbacks(timeOutHandler);
    }

    //已连接，移除 延时报错线程 并调用callback.onConnected(...)方法
    private void onConnected() {
        Log.d(TAG, "connected to beacon");
        handler.removeCallbacks(timeOutHandler);
        didReadCharacteristic = true;
        connectionCallback.onConnected(new BeaconCharacteristics(voliamService, batteryService),
                new BeaconEditor(voliamService));
    }

    //发生连接错误 调用callback.onConnectionError()方法
    private void notifyConnectingError() {
        handler.removeCallbacks(timeOutHandler);
        connectionCallback.onConnectionError();
    }

    //断开连接 调用callback.onDisconnected()方法
    private void notifyDisconnected() {
        connectionCallback.onDisconnected();
    }

    //读取 获取的所有characteristic
    private void readCharacteristics(BluetoothGatt gatt) {
        Log.w(TAG, "start read Characteristic " + gatt.discoverServices());
        if (!toFetch.isEmpty()) {
            BluetoothGattCharacteristic characteristic = toFetch.poll();
            gatt.readCharacteristic(characteristic);
        } else if (bluetoothGatt != null && isCharacteristicsRead()) {
            onConnected();
        }
    }

    //从discovered services 中获取 beacon characteristics
    private void processDiscoveredServices(List<BluetoothGattService> services) {
        voliamService.processGattServices(services);
        batteryService.processGattServices(services);

        this.toFetch.clear();
        this.toFetch.addAll(this.voliamService.getAvailableCharacteristics());
        this.toFetch.addAll(this.batteryService.getAvailableCharacteristics());
        Log.d(TAG, "process Discovered Service complete!");

    }

    //判断是否已读chars
    private boolean isCharacteristicsRead(){
        //if(voliamService.getAdvertisingIntervalMillis() == null) return false;
        if(voliamService.getFirmwareVersion() == null ) return false;
        if(voliamService.getTxPowerDBM() == null) return false;
        if(batteryService.getBattery() == null) return false;
        return true;
    }

    //创建bluetoothCallback 该callback 是 gatt 调用 connect 时返回的回调接口，用以对建立好连接的ble 进行操作
    private BluetoothGattCallback createBluetoothCallback() {
        return new BluetoothGattCallback() {

            /**
             * 当连接状态改变时，回调该API
             * @param gatt
             * @param status
             * @param newState
             */
            @Override
            public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
                super.onConnectionStateChange(gatt, status, newState);
                if (newState == BluetoothProfile.STATE_CONNECTED) {
                    //connected
                    Log.d(TAG, "connected to GATT server,discovering services:" + gatt.discoverServices());
                } else if (newState == BluetoothProfile.STATE_DISCONNECTED && !didReadCharacteristic) {
                    //connectedError
                    Log.w(TAG, "Disconnected from GATT server,there is someting wrong!");
                    notifyConnectingError();
                } else if (newState == BluetoothProfile.STATE_DISCONNECTED && didReadCharacteristic) {
                    //disconnected
                    Log.d(TAG, "Disconnected from GATT server.");
                    notifyDisconnected();
                }
            }

            /**
             * 当发现ble设备时 回调该API
             * @param gatt
             * @param status
             */
            @Override
            public void onServicesDiscovered(BluetoothGatt gatt, int status) {
                super.onServicesDiscovered(gatt, status);
                //if(didReadCharacteristic) return ;
                if (status == BluetoothGatt.GATT_SUCCESS) { //discovered services
                    Log.d(TAG, "Services discovered");
                    //process discovered service
                    synchronized (this) {
                        processDiscoveredServices(gatt.getServices());
                        onBeginReadingCharacteristics(gatt);
                    }

                } else {
                    Log.d(TAG, "Couldn't discover service");
                    notifyConnectingError();
                }
            }

            /**
             * 当bluetoothGatt读取 ble 设备特征值时，回调该API
             * @param gatt
             * @param characteristic
             * @param status
             */
            @Override
            public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
                super.onCharacteristicRead(gatt, characteristic, status);
                Log.w(TAG, "onCharacteristic read" + "  status:" + status);
                if (status == BluetoothGatt.GATT_SUCCESS) {
                    uuidToServiceMap.get(characteristic.getService().getUuid()).update(characteristic);
//                    Log.d(TAG, "chars uuid: " + characteristic.getUuid().toString());
//                    Log.d(TAG, "chars value: " + characteristic.getValue());
                    readCharacteristics(gatt);
                } else {
                    Log.w(TAG, "Failed to read characteristic");
                    toFetch.clear();
                    notifyConnectingError();
                }
            }

            /**
             * 当bluetoothGatt 写 ble 设备特征值时，回调该API
             * @param gatt
             * @param characteristic
             * @param status
             */
            @Override
            public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
                super.onCharacteristicWrite(gatt, characteristic, status);
                if (VoliamUuid.VOLIAM_SERVICE.equals(characteristic.getService().getUuid()))
                    voliamService.onCharacteristicWrite(characteristic, status);
            }
        };
    }

    //开始读取characteristic
    private void onBeginReadingCharacteristics(final BluetoothGatt gatt) {
        handler.postDelayed(new Runnable() {
            public void run() {
                BeaconConnector.this.readCharacteristics(gatt);
            }
        }, 500L);
    }

    //创建延时报错线程
    private Runnable createTimeOutHandler() {
        return new Runnable() {
            @Override
            public void run() {
                Log.w(TAG, "time out while connecting");
                if (!didReadCharacteristic) {
                    if (bluetoothGatt != null) {
                        bluetoothGatt.disconnect();
                        bluetoothGatt.close();
                    }
                    notifyConnectingError();
                }
            }
        };
    }


    /**
     * 连接回调接口
     */
    public static interface ConnectionCallback {
        /**
         * 成功连接到Beacon设备时 回调该API，并向外传递两个参数
         *
         * @param characteristics ：存储beacon所有的Characteristic 可通过调用BeaconCharacteristics类中的方法获取各个特征值
         * @param beaconEditor    ：beacon编辑类对象， 可调用该对象的各个API对建立好连接的beacon基站写入特征值
         */
        public void onConnected(BeaconCharacteristics characteristics, BeaconEditor beaconEditor);

        /**
         * 连接发生错误时，Connector会调用该API
         */
        public void onConnectionError();

        /**
         * 断开连接时，Connector会调用该API
         */
        public void onDisconnected();
    }

    /**
     * 写回调接口，写特征值时调用接口中的方法
     * @author Administrator
     */
    public static interface WriteCallback {

        /**
         * 写入成功时 回调
         */
        public void onWriteSuccess();

        /**
         * 写入失败时
         */
        public void onWriteError();
    }

    /**
     * Beacon gatt server特征值编辑器，建立连接后取得
     * 用以修改Beacon的特征值
     * 还可用以向Beacon写入数据 fIxme
     */
    public class BeaconEditor {

        private VoliamService voliamService;

        public BeaconEditor(VoliamService voliamService) {
            this.voliamService = voliamService;
        }

        /**
         * update uuid
         * @param proximityUuid
         * @param writeCallback
         */
        public void writeProximityUuid(String proximityUuid, BeaconConnector.WriteCallback writeCallback) {
            if (!(BeaconConnector.this.isConnected()) || (!voliamService.hasCharacteristic(VoliamUuid.VOLIAM_UUID))) {
                Log.w(TAG, "Not connected to beacon. Discarding changing proximity UUID.");
                writeCallback.onWriteError();
                return;
            }
            byte[] uuidAsBytes = HashCode.fromString(proximityUuid.replaceAll("-", "").toLowerCase()).asBytes();
            BluetoothGattCharacteristic uuidChar = voliamService.beforeCharacteristicWrite(VoliamUuid.VOLIAM_UUID, writeCallback);

            uuidChar.setValue(uuidAsBytes);
            BeaconConnector.this.bluetoothGatt.writeCharacteristic(uuidChar);
        }

        /**
         * update AdvertisingInterval
         * @param intervalMillis
         * @param writeCallback
         */
        public void writeAdvertisingInterval(int intervalMillis, BeaconConnector.WriteCallback writeCallback) {
            if ((!(isConnected())) || (!(this.voliamService.hasCharacteristic(VoliamUuid.ADV_INT_UUID)))) {
                Log.w(TAG, "Not connected to beacon. Discarding changing advertising interval.");
                writeCallback.onWriteError();
                return;
            }
            intervalMillis = Math.max(0, Math.min(2000, intervalMillis));
            int correctedInterval = (int) (intervalMillis / 0.625D);
            BluetoothGattCharacteristic intervalChar = this.voliamService.beforeCharacteristicWrite(VoliamUuid.ADV_INT_UUID, writeCallback);
            if(intervalChar == null) return ;
            intervalChar.setValue(correctedInterval, 18, 0);
            BeaconConnector.this.bluetoothGatt.writeCharacteristic(intervalChar);
        }

        /**
         * update broadcast power
         * @param powerDBM
         * @param writeCallback
         */
        public void writeBroadcastingPower(int powerDBM, BeaconConnector.WriteCallback writeCallback) {
            if ((!(isConnected())) || (!(this.voliamService.hasCharacteristic(VoliamUuid.TXPOWER_UUID)))) {
                Log.w(TAG, "Not connected to beacon. Discarding changing broadcasting power.");
                writeCallback.onWriteError();
                return;
            }
            if (!(ALLOWED_POWER_LEVELS.contains(Integer.valueOf(powerDBM)))) {
                Log.w(TAG, "Not allowed power level. Discarding changing broadcasting power.");
                writeCallback.onWriteError();
                return;
            }
            BluetoothGattCharacteristic powerChar = this.voliamService.beforeCharacteristicWrite(VoliamUuid.TXPOWER_UUID, writeCallback);

            powerChar.setValue(powerDBM, 17, 0);
            BeaconConnector.this.bluetoothGatt.writeCharacteristic(powerChar);
        }

        /**
         * update major
         * @param major
         * @param writeCallback
         */
        public void writeMajor(int major, WriteCallback writeCallback) {
            if (!(isConnected())) {
                Log.w(TAG, "Not connected to beacon. Discarding changing major.");
                writeCallback.onWriteError();
                return;
            }
            major = Utils.normalize16BitUnsignedInt(major);
            BluetoothGattCharacteristic majorChar = voliamService.beforeCharacteristicWrite(VoliamUuid.MAJOR_UUID, writeCallback);

            majorChar.setValue(major, 18, 0);
            BeaconConnector.this.bluetoothGatt.writeCharacteristic(majorChar);
        }

        /**
         * update minor
         * @param minor
         * @param writeCallback
         */
        public void writeMinor(int minor, BeaconConnector.WriteCallback writeCallback) {
            if (!(isConnected())) {
                Log.w(TAG, "Not connected to beacon. Discarding changing minor.");
                writeCallback.onWriteError();
                return;
            }
            minor = Utils.normalize16BitUnsignedInt(minor);
            BluetoothGattCharacteristic minorChar = voliamService.beforeCharacteristicWrite(VoliamUuid.MINOR_UUID, writeCallback);

            minorChar.setValue(minor, 18, 0);
            BeaconConnector.this.bluetoothGatt.writeCharacteristic(minorChar);
        }
    }


}
