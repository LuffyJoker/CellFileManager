package com.xgimi.dlna.center;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;

import com.blankj.utilcode.util.LogUtils;
import com.xgimi.dlna.proxy.AllShareProxy;
import com.xgimi.dlna.upnp.ControlPoint;
import com.xgimi.dlna.upnp.Device;
import com.xgimi.dlna.upnp.device.DeviceChangeListener;
import com.xgimi.dlna.upnp.device.SearchResponseListener;
import com.xgimi.dlna.upnp.ssdp.SSDPPacket;
import com.xgimi.dlna.utils.CommonUtil;

/**
 * author : joker.peng
 * e-mail : joker.peng@xgimi.com
 * date   : 2020/8/4 10:31
 * desc   :
 */
public class DlnaService extends Service implements IBaseEngine,
        DeviceChangeListener,
        ControlCenterWorkThread.ISearchDeviceListener {

    public static final String SEARCH_DEVICES = "com.geniusgithub.allshare.search_device";
    public static final String RESET_SEARCH_DEVICES = "com.geniusgithub.allshare.reset_search_device";

    private static final int NETWORK_CHANGE = 0x0001;
    private boolean firstReceiveNetworkChangeBR = true;
    private NetworkStatusChangeBR mNetworkStatusChangeBR;


    private ControlPoint mControlPoint;
    private ControlCenterWorkThread mCenterWorkThread;
    private AllShareProxy mAllShareProxy;
    private Handler mHandler;


    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        LogUtils.e("dlna_framework", "DlnaService onCreate");
        init();
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if (intent != null && intent.getAction() != null) {
            String action = intent.getAction();
            if (DlnaService.SEARCH_DEVICES.equals(action)) {
                startEngine();
            } else if (DlnaService.RESET_SEARCH_DEVICES.equals(action)) {
                restartEngine();
            }
        } else {
            LogUtils.e("dlna_framework", "intent = " + intent);
        }

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        LogUtils.e("dlna_framework", "DlnaService onDestroy");
        unInit();
        super.onDestroy();
    }


    private void init() {
        mAllShareProxy = AllShareProxy.getInstance(this);

        mControlPoint = new ControlPoint();
        //		FileManagerApplication.getInstance().setControlPoint(mControlPoint);
        mControlPoint.addDeviceChangeListener(this);
        mControlPoint.addSearchResponseListener(new SearchResponseListener() {
            public void deviceSearchResponseReceived(SSDPPacket ssdpPacket) {
            }
        });


        mCenterWorkThread = new ControlCenterWorkThread(this, mControlPoint);
        mCenterWorkThread.setSearchListener(this);

        mHandler = new Handler() {

            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case NETWORK_CHANGE:
                        mAllShareProxy.resetSearch();
                        break;
                }
            }

        };

        registerNetworkStatusBR();

        boolean ret = CommonUtil.openWifiBrocast(this);
        LogUtils.e("dlna_framework", "openWifiBrocast = " + ret);
    }

    private void unInit() {
        unRegisterNetworkStatusBR();
        //		FileManagerApplication.getInstance().setControlPoint(null);
        mCenterWorkThread.setSearchListener(null);
        mCenterWorkThread.exit();
    }


    @Override
    public boolean startEngine() {
        awakeWorkThread();
        return true;
    }


    @Override
    public boolean stopEngine() {
        exitWorkThread();
        return true;
    }


    @Override
    public boolean restartEngine() {
        mCenterWorkThread.reset();
        return true;
    }


    @Override
    public void deviceAdded(Device dev) {
        mAllShareProxy.addDevice(dev);
    }


    @Override
    public void deviceRemoved(Device dev) {
        LogUtils.e("dlna_framework", "deviceRemoved dev = " + dev.getUDN());
        mAllShareProxy.removeDevice(dev);
    }


    private void awakeWorkThread() {

        if (mCenterWorkThread.isAlive()) {
            mCenterWorkThread.awakeThread();
        } else {
            mCenterWorkThread.start();
        }
    }

    private void exitWorkThread() {
        if (mCenterWorkThread != null && mCenterWorkThread.isAlive()) {
            mCenterWorkThread.exit();
            long time1 = System.currentTimeMillis();
            while (mCenterWorkThread.isAlive()) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            long time2 = System.currentTimeMillis();
            LogUtils.e("dlna_framework", "exitCenterWorkThread cost time:" + (time2 - time1));
            mCenterWorkThread = null;
        }
    }


    @Override
    public void onSearchComplete(boolean searchSuccess) {

        if (!searchSuccess) {
            sendSearchDeviceFailBrocast(this);
        }
    }

    public static final String SEARCH_DEVICES_FAIL = "com.geniusgithub.allshare.search_devices_fail";

    public static void sendSearchDeviceFailBrocast(Context context) {
        LogUtils.e("dlna_frameowrk", "sendSearchDeviceFailBrocast");
        Intent intent = new Intent(SEARCH_DEVICES_FAIL);
        context.sendBroadcast(intent);
    }

    private class NetworkStatusChangeBR extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {

            if (intent != null) {
                String action = intent.getAction();
                if (action != null) {
                    if (action.equalsIgnoreCase(ConnectivityManager.CONNECTIVITY_ACTION)) {
                        sendNetworkChangeMessage();
                    }
                }
            }
        }
    }

    private void registerNetworkStatusBR() {
        if (mNetworkStatusChangeBR == null) {
            mNetworkStatusChangeBR = new NetworkStatusChangeBR();
            registerReceiver(mNetworkStatusChangeBR, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
        }
    }

    private void unRegisterNetworkStatusBR() {
        if (mNetworkStatusChangeBR != null) {
            unregisterReceiver(mNetworkStatusChangeBR);
        }
    }

    private void sendNetworkChangeMessage() {
        if (firstReceiveNetworkChangeBR) {
            LogUtils.e("dlna_framework", "first receive the NetworkChangeMessage, so drop it...");
            firstReceiveNetworkChangeBR = false;
            return;
        }

        mHandler.removeMessages(NETWORK_CHANGE);
        mHandler.sendEmptyMessageDelayed(NETWORK_CHANGE, 500);
    }

}

