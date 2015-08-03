package com.yetwish.libs;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.util.Log;
import android.widget.Toast;

import com.estimote.sdk.Beacon;
import com.estimote.sdk.BeaconManager;
import com.estimote.sdk.Region;
import com.estimote.sdk.utils.L;
import com.yetwish.libs.distance.CustomerCalculator;
import com.yetwish.libs.distance.DistanceUtils;
import com.yetwish.libs.util.Constants;
import com.yetwish.libs.util.LogUtil;


/**
 * beacon基站搜索类， 用以搜索beacon基站，开发者 通过实现callback回调接口，实现对获取到最小距离beacon基站后的操作
 * 
 * Created by yetwish on 2015-03-31
 *<pre>
 *{@code
 *public class FollowGuideFragment extends Fragment implements OnRangingListener{
 *	private BeaconSearcher beaconSearcher;
 *	protected void onCreate(Bundle savedInstanceState) {
 *		super.onCreate(savedInstanceState);
 *		setContentView(R.layout.activity_main);
 *		//获取beaconSearcher 实例
 *		beaconSearcher = BeaconSearcher.getInstance(activity);
 *		//设置最小停留时间  如果不设 则默认为5
 *		BeaconSearcher.setMinStayTime(5);
 *		//打开beacon搜索器
 *		beaconSearcher.openSearcher();
 *		//设置beacon监听器
 *		beaconSearcher.setBeaconRangingListener(this);
 *	}
 *
 *	public void onRangeIn(Beacon beacon) {
 *		//自定义进入区域逻辑		
 *	}
 *	
 *	public void onRangeOut(Beacon beacon) {
 *		//自定义退去区域逻辑
 *	}
 *	
 *	//when the activity is on the top of the stack ,start ranging the beacon
 *	public void onResume() {
 *		super.onResume();
 *		//开始搜索
 *		if(beaconSearcher.prepareBluetooth())
 *			beaconSearcher.startRanging();
 *	}
 *
 *	//实现onActivityResult回调方法，并调用beaconSearcher的onBluetoothResult()方法
 *	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
 *		super.onActivityResult(requestCode,resultCode,intent);
 *		if(beaconSearcher.onBluetoothResult(requestCode, resultCode))
 *			beaconSearcher.startRanging();
 *	}
 *	
 *	//when the activity is not on the top of the stack , stop ranging the beacon
 *	public void onPause() {
 *		super.onPause();
 *		//停止搜索
 *		beaconSearcher.stopRanging();
 *	}
 *	
 *	//destroy the fragment, close the beaconSearcher.
 *	public void onDestroy() {
 *		super.onDestroy();
 *		//关闭beacon搜索器
 *		beaconSearcher.closeSearcher();
 *	}
 *}
 * <pre>
 * 
 */
public class BeaconSearcher {

    private final static String TAG = BeaconSearcher.class.getSimpleName();
    /**
     * bluetooth请求码
     */
    public static final int REQUEST_ENABLE_BT = 1234;
    
    /**
     * 所有VoliamBeacon的注册信息对象
     */
    private final static Region ALL_VOLIAM_BEACONS_REGION = new Region("voliam", null, null, null);

    /**
     * beacon监听者对象
     */
    private OnRangingListener mOnRangingListener;

    /**
     * appContext对象，用以创建BeaconManager
     */
    private Context mApplicationContext;

    /**
     *
     */
    private Activity mContext;
    /**
     * BeaconManager对象，用以搜索beacon设备
     */
    private BeaconManager mBeaconManager;

    /**
     * Beacon对象，用以存放上次进入的Beacon区域的对象
     */
    private Beacon mLastBeacon;

    /**
     * 用以存放上次的最短距离
     */
    private double mLastMinDistance;

    
    /**
     * 用以存放beacon对象的数组
     */
    private List<Beacon> mBeaconsList;

    /**
     * 最短距离beacon
     */
    private Beacon mNearestBeacon;

    /**
     * 监控状态 
     * @author yetwish
     * @date 2015-4-25
     */
    private static enum BeaconMonitoringState {
        unMonitored, Monitored
    }

    /**
     * 存储现在的状态
     */
    private BeaconMonitoringState mState = BeaconMonitoringState.unMonitored;

    /**
     * 默认最小进入区域停留时间
     */
    public static final int DEFAULT_MIN_ENTER_TIME = 6;
    
    /**
     * 最小进入区域停留时间 ，以秒为单位，默认为6s
     */
    private static int minEnterStayTime = DEFAULT_MIN_ENTER_TIME;
    
    
    /**
     * 获取最小进入区域停留时间
     * @return
     */
    public static int getMinEnterStayTime(){
    	return minEnterStayTime;
    }
    
    /**
     * 设置最小进入区域停留时间
     * @param time
     */
    public static void setMinEnterStayTime(int time){
    	minEnterStayTime = time;
    }
    
    /**
     * 默认最小切换区域停留时间
     */
    public static final int DEFAULT_MIN_CHANGE_TIME = 4;
    
    /**
     * 最小切换区域停留时间 ,以秒为单位,默认为4s
     */
    private static int minChangeStayTime = DEFAULT_MIN_CHANGE_TIME;
    
    /**
     * 获取最小进入区域停留时间
     * @return
     */
    public static int getMinChangeStayTime(){
    	return minChangeStayTime;
    }
    
    /**
     * 设置最小进入区域停留时间
     * @param time
     */
    public static void setMinChangeStayTime(int time){
    	minChangeStayTime = time;
    }
    
    /**
     * 判断是否第一次获取minBeacon，判断是否设置延时操作线程
     */
    private boolean isFirst = true;
    
    /**
     * 延时操作线程
     */
    private Runnable timeoutHandler = new Runnable() {
        @Override
        public void run() {
            didGetBeacons = true;
            isFirst = true;
        }
    };

    /**
     * 用以存放多次获得的minBeacon
     */
    private static ArrayList<Beacon> minBeaconList = new ArrayList<Beacon>();

    /**
     * 用以存放minBeacon 与 出现次数的对应关系
     */
    private static Map<Beacon,Integer> beaconMap  = new HashMap<Beacon, Integer>();

    /**
     * 表示是否已获取规定次数的minBeacon
     */
    private boolean didGetBeacons = false;

    private final static int MSG_DISCOVERED_BEACON = 101;
    
    /**
     * handler 对象，处理从discoverdBeacon(List<Beacon>)传来的beacons 数据
     */
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case MSG_DISCOVERED_BEACON:
                    synchronized (this) { //保证线程同步
                    	mNearestBeacon = DistanceUtils.getMinDistanceBeacon(mBeaconsList);
                    	double currentDistance = DistanceUtils.calculateDistance(mNearestBeacon);
                        switch (mState) {
                            case unMonitored://未监控任何beacon
                            	//在第一次进入一个新的区域时（即从未监控状态转换到监控状态时）,获取多次minBeacon(获取的次数由最小停留时间决定)存入minBeaconList中
                            	//最后在minBeaconList中获取出现个数最多的Beacon，即为最小距离beacon 
                            	LogUtil.d("Monitoring!");
                            	if(!didGetBeacons){
                            		//获取minBeacon列表
                            		getMinBeaconsList(mNearestBeacon,minEnterStayTime);
                            		if(!didGetBeacons) break;
                            	}
                                mNearestBeacon = getMinDistanceBeacon();
                                currentDistance = DistanceUtils.calculateDistance(mNearestBeacon);
                                LogUtil.w("getMontiored, "+"currentDistance:"+currentDistance);
                                if (currentDistance <= DistanceUtils.getBroadcastDistance()) {
                                    //建立监听
                                    notifyOnRangeIn(mNearestBeacon);
                                    //更新上次最短距离
                                    mLastMinDistance = currentDistance;
                                }
                                break;
                            case Monitored://已监听某beacon
                            	//若当前已在某一Beacon区域内，则判断当前获取的minBeacon是否为已进入的beacon，
                            	//若不是，则根据最小切换停留时间，获取一段时间内的minBeacon列表，再更新最小beacon.
                                if (currentDistance <= DistanceUtils.getBroadcastDistance()) {
//                                    LogUtil.t("Monitored");
                                    if (!mNearestBeacon.equals(mLastBeacon) && currentDistance <= mLastMinDistance) {
                                        //如果该nearestBeacon 非正在监听的beacon,且距离基站的距离变短了， 表示已经进入另一区域
                                    	//获取minBeacon数组
                                    	if(!didGetBeacons){
                                    		getMinBeaconsList(mNearestBeacon, minChangeStayTime);
                                    		if(!didGetBeacons) break;
                                    	}
                                    	mNearestBeacon = getMinDistanceBeacon();
                                    	if(!mNearestBeacon.equals(mLastBeacon)){//如果minBeacon改变了，则通知进入另一区域
                                    		notifyOnRangeOut(mLastBeacon);//通知最后进入的区域 已经离开其范围
                                    		notifyOnRangeIn(mNearestBeacon);
                                        }else {//如果还在原区域，则更新beacon信息
                                        	mLastBeacon = mNearestBeacon;
                                        }
                                    } else if (mNearestBeacon.equals(mLastBeacon)) {
                                        //如果该nearestBeacon 还是正在监听的beacon 表示还在当前区域 更新该beacon数据
                                        mLastBeacon = mNearestBeacon;
                                    }
                                    mLastMinDistance = currentDistance;
                                } else if (mNearestBeacon.equals(mLastBeacon)) {//离开某一区域
                                    notifyOnRangeOut(mNearestBeacon);
                                    clearState();
                                }
                                break;
                        }
                    }
                    break;

            }
        }
    };
    
    /**
     * 获取一段时间内的最小Beacon数组
     * @param minBeacon
     */
    private void getMinBeaconsList(Beacon minBeacon,int minStayTime){
    	if(isFirst){ //不存储第一次的minBeacon
    		//设置延迟处理
    		mHandler.postDelayed(timeoutHandler,TimeUnit.SECONDS.toMillis(minStayTime*2));
    		isFirst = false;
    	}
    	else {
    		minBeaconList.add(minBeacon);
        }
        if(minBeaconList.size() >= minStayTime-1) {
            didGetBeacons = true;
            mHandler.removeCallbacks(timeoutHandler);
            isFirst = true;
        }
    }
    
    
    /**
     * 清理所有获取minBeacon的状态
     */
    private void clearState(){
    	mState = BeaconMonitoringState.unMonitored;
    	minBeaconList.clear();
    	beaconMap.clear();
    	didGetBeacons = false;
    	mHandler.removeCallbacks(timeoutHandler);
    }

    /**
     *  获得出现次数最多的minBeacon
     * @return 获得出现次数最多的minBeacon
     */
    private Beacon getMinDistanceBeacon(){
        Beacon minBeacon = null;
        for(Beacon beacon: minBeaconList){ //获取各个minBeacon 出现次数
            if(beaconMap.containsKey(beacon)) beaconMap.put(beacon,beaconMap.get(beacon)+1);
            else  beaconMap.put(beacon,1);
        }

        // 经多次比较 获取出现次数最多的minBeacon
        int max = 0;
        for (Beacon beacon : beaconMap.keySet()) {
            int count = beaconMap.get(beacon);
            if(max<count) {
                max = count;
                minBeacon = (Beacon)beacon;
            }
        }
        clearState();
        return minBeacon;
    }


    /**
     * make the constructor private to ensure that there is only one beaconSearcher instance.
     */
    private BeaconSearcher(Activity context) {
        this.mContext = context;
        this.mApplicationContext = context.getApplicationContext();
        L.enableDebugLogging(true);
        LogUtil.enableDebugLogging(true);
        //LogUtil.enableFileLogging(true);
        this.mBeaconManager = new BeaconManager(mApplicationContext);
        DistanceUtils.setDistanceCalculator(new CustomerCalculator());
    }

    /**
     * the single instance beaconSearcher
     */
    private static BeaconSearcher instance = null;

    /**
     * use this method to get the beacon instance
     *
     * @return beacon instance
     * @param context 传入一个Activity对象
     */
    public static BeaconSearcher getInstance(Activity context) {
        if (instance == null) {
            synchronized (BeaconSearcher.class) {
                if (instance == null) {
                    instance = new BeaconSearcher(context);
                }
            }
        }
        return instance;
    }

    /**
     * 调用这个方法 设置onRangingListener,通过设置onRangingListener，实现进入/离开Beacon区域时的操作。
     * @param listener, 要配置的onRangingListener对象
     */
    public void setBeaconRangingListener(OnRangingListener listener) {
        this.mOnRangingListener = listener;
    }

    /**
     * 通知Listener 已进入Beacon区域
     */
    private void notifyOnRangeIn(Beacon beacon) {
        if (mOnRangingListener != null)
            mOnRangingListener.onRangeIn(beacon);
        mLastBeacon = mNearestBeacon;
        mState = BeaconMonitoringState.Monitored;
    }

    /**
     *通知Listener 已离开Beacon区域
     */
    private void notifyOnRangeOut(Beacon beacon) {
        if (mOnRangingListener != null)
            mOnRangingListener.onRangeOut(beacon);
        mLastBeacon = null;
        mState = BeaconMonitoringState.unMonitored;
    }

    /**
     * 设置前台扫描时间间隔，默认是(1000,0),即无休眠扫描，每次扫描周期为1s，
     * @param scanPeriodMills: 扫描时间
     * @param waitTimeMillis: 等待时间
     */
    public void setForegroundScanPeriod(long scanPeriodMills,long waitTimeMillis){
    	mBeaconManager.setForegroundScanPeriod(scanPeriodMills, waitTimeMillis);
    }
    
    /**
     * 设置后台扫描时间间隔，默认是(5000,30000)，即扫描5s,暂停30s
     * @param scanPeriodMills: 扫描时间
     * @param waitTimeMillis: 等待时间
     */
    public void setBackgroundScanPeriod(long scanPeriodMills,long waitTimeMills){
    	mBeaconManager.setBackgroundScanPeriod(scanPeriodMills, waitTimeMills);
    }

    /**
     * 该方法用以查询终端设备是否已开启BLE（Bluetooth low energy）
     * 若设备未开启，会自动弹出一个选项框提示是否开启蓝牙
     *
     * @return boolean:手机支持BLE，true 表示已开启蓝牙；false表示蓝牙未开启
     */
    public boolean prepareBluetooth() {
        // Check if device supports Bluetooth Low Energy.
        if (!mBeaconManager.hasBluetooth()) {
            Toast.makeText(mContext,
                    "Device does not have Bluetooth Low Energy",
                    Toast.LENGTH_SHORT).show();
            System.exit(0);
        }
        // If Bluetooth is not enabled, let user enable it.
        if (!mBeaconManager.isBluetoothEnabled()) {
            Intent enableBtIntent = new Intent(
                    BluetoothAdapter.ACTION_REQUEST_ENABLE);
            mContext.startActivityForResult(enableBtIntent,REQUEST_ENABLE_BT);
            return false;
        }
        return true;
    }

    /**
     * 调用该方法 使BeaconSearcher开始工作（搜索beacon基站）
     */
    public void openSearcher() {
        mBeaconManager.setRangingListener(new BeaconManager.RangingListener() {
            /*
                beaconManager提供的接口，当发现beacon设备时 会调用该方法
             */
            @Override
            public void onBeaconsDiscovered(Region region, List<Beacon> beacons) {
                Log.d(TAG, "Discovered beacons" + beacons.size() + beacons.toString());
                //process the beacons
               if(beacons.size()>0){
                   mBeaconsList = beacons;
                   mHandler.sendEmptyMessage(MSG_DISCOVERED_BEACON);
               }
            }
        });
        //调用beaconManager.connect()
    }

    /**
     * 调用该方法 以关闭搜索Beacon基站
     */
    public void closeSearcher() {

    	mBeaconManager.disconnect();
    }

    /**
     * 调用该方法，使searcher恢复工作
     */
    public void startRanging() {
        Log.d(TAG, "start ranging");
        mBeaconManager.connect(new BeaconManager.ServiceReadyCallback() {
            @Override
            public void onServiceReady() {
                try {
                    Log.d(TAG, "start ranging beacons");
                    mBeaconManager.startRanging(ALL_VOLIAM_BEACONS_REGION);
                } catch (RemoteException e) {
                    Log.w(TAG, "Error while starting ranging", e);
                }
            }
        });
    }

    /**
     * 调用该方法，使searcher暂停工作
     */
    public void stopRanging() {
        mBeaconsList = null;
        mLastBeacon = null;
        mState = BeaconMonitoringState.unMonitored;
        try {
            mBeaconManager.stopRanging(ALL_VOLIAM_BEACONS_REGION);
            Log.d(TAG, "stop ranging beacons");
        } catch (RemoteException e) {
            Log.w(TAG, "Error while stopping ranging", e);
        }
        LogUtil.enableFileLogging(false);
    }

    /**
     * 在Activity的onActivityResult方法中调用此方法，检测是否打开了蓝牙
     * @param requestCode,int intent请求码
     * @param resultCode,int 结果代码
     * 
     */
    public boolean onBluetoothResult(int requestCode, int resultCode) {
        if (requestCode == REQUEST_ENABLE_BT) {
            if (resultCode == Activity.RESULT_OK) {
                return true;
            } else {
                Toast.makeText(mContext, "Bluetooth not enabled",
                        Toast.LENGTH_SHORT).show();
                this.prepareBluetooth();
                return false;
            }
        }
        return false;
    }

    /**
     * 监听者接口， 外部实现该接口 以实现与beacon库的通信。
     * @author yetwish
     */
    public interface OnRangingListener {

        /**
         * 实现该方法，以实现当进入beacon基站区域时 需要进行的操作
         *
         * @param beacon : 进入的区域的beacon基站 的对象
         */
        public void onRangeIn(Beacon beacon);

        /**
         * 实现该方法，以实现当离开beacon基站区域时 需要进行的操作
         *
         * @param beacon : 离开的区域的beacon基站 的对象
         */
        public void onRangeOut(Beacon beacon);
    }


}
