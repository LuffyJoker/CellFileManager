package com.xgimi.samba.search;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import com.blankj.utilcode.util.LogUtils;
import com.xgimi.samba.SmbDevice;
import com.xgimi.samba.utils.ShareUtils;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * author : joker.peng
 * e-mail : joker.peng@xgimi.com
 * date   : 2020/8/4 10:59
 * desc   : 搜索 Samba 服务
 */
public class SmbSearcher {
    private static final String TAG = "SmbSearcher";
    private static final String LOCAL_HOST = "127.0.0.1";
    private static final int MIX_IP_COUNT = 7;
    private List deviceList = Collections.synchronizedList(new ArrayList<SmbDevice>());
    private int timeOut = 300;
    private boolean logEnable = false;
    private boolean isScanning = false;
    private AtomicInteger scanCompleted = new AtomicInteger(0);
    private volatile boolean stopRun = false;
    private static final int MAX_THREAD_COUNT = 8;
    private OnSmbSearchListener onSmbSearchListener;
    private Context mContext;

    public SmbSearcher(Context context, OnSmbSearchListener onSmbSearchListener) {
        this.mContext = context.getApplicationContext();
        this.onSmbSearchListener = onSmbSearchListener;
    }

    /**
     * Start searching for Samba devices
     */
    public void startSearchSmbDevice() {
        this.stopRun = false;
        this.deviceList.clear();
        String smbUrl = getLocalIP();
        LogUtils.i(TAG, "startSearchSmbDevice ip:" + smbUrl);
        if (smbUrl != null && smbUrl.length() >= MIX_IP_COUNT) {
            if (!smbUrl.contentEquals(LOCAL_HOST) && onSmbSearchListener != null && !smbUrl.contains(":")) {
                this.scanCompleted.set(0);

                for (int i = 0; i < MAX_THREAD_COUNT; ++i) {
                    if (this.stopRun) {
                        this.isScanning = false;
                        break;
                    }
                    int subCount = 256 / MAX_THREAD_COUNT;
                    new Thread(new PingRunnable(
                            smbUrl,
                            i + 1,
                            i * subCount + 1,
                            i * subCount + subCount,
                            onSmbSearchListener)
                    ).start();
                    isScanning = true;
                }
            } else {
                this.isScanning = false;
                if (logEnable) {
                    Log.e(TAG, "no ip address or recv listener! ");
                }
                if (onSmbSearchListener != null) {
                    onSmbSearchListener.onSearch(OnSmbSearchListener.UPDATE_DEVICE_CANCEL, null);
                }

            }
        } else {
            this.isScanning = false;
            if (logEnable) {
                Log.e(TAG, "no ip address!");
            }
            if (onSmbSearchListener != null) {
                onSmbSearchListener.onSearch(OnSmbSearchListener.UPDATE_DEVICE_CANCEL, null);
            }

        }
    }

    /**
     * 获取本机局域网 IP
     *
     * @return
     */
    private String getLocalIP() {
        try {
            int type = getNetworkType(mContext);
            Enumeration en = NetworkInterface.getNetworkInterfaces();
            while (en.hasMoreElements()) {
                NetworkInterface intf = (NetworkInterface) en.nextElement();
                if (intf.getName().startsWith((type == ConnectivityManager.TYPE_ETHERNET ? "eth" : "wlan"))) {
                    Enumeration ipAddr = intf.getInetAddresses();
                    while (ipAddr.hasMoreElements()) {
                        InetAddress inetAddress = (InetAddress) ipAddr.nextElement();
                        if (!inetAddress.isLoopbackAddress() && isIPv4Address(inetAddress.getHostAddress())) {
                            return inetAddress.getHostAddress();
                        }
                    }
                }
            }
        } catch (Exception var5) {
            var5.printStackTrace();
        }
        return "";
    }

    /**
     * 获取网络类型
     *
     * @param context
     * @return
     */
    private int getNetworkType(Context context) {
        ConnectivityManager mConnectivityManager = (ConnectivityManager) context.getApplicationContext()
                                                                                .getSystemService(
                                                                                        Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = mConnectivityManager.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isAvailable()) {
            return networkInfo.getType();
        }
        return -1;
    }

    private boolean isIPv4Address(String ip) {
        String regex = "([1-9]|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])(\\.(\\d|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])){3}";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(ip);
        return matcher.matches();
    }


    public void setTimeOut(int timeOut) {
        this.timeOut = timeOut;
    }

    public void setLogEnable(boolean logEnable) {
        this.logEnable = logEnable;
    }

    public boolean isScanning() {
        return isScanning;
    }

    public void stop() {
        onSmbSearchListener = null;
        stopRun = true;
        isScanning = false;
    }

    // samba 搜索监听
    public interface OnSmbSearchListener {
        /**
         * cancel search
         */
        int UPDATE_DEVICE_CANCEL = 1;
        /**
         * search complete
         */
        int UPDATE_DEVICE_DONE = 2;
        /**
         * search for a device
         */
        int UPDATE_DEVICE_ADD = 3;

        /**
         * Notify search results
         *
         * @param status Result status
         * @param device Searched device
         */
        void onSearch(int status, SmbDevice device);
    }

    class PingRunnable implements Runnable {
        String startIp;
        OnSmbSearchListener callback;
        int startIndex;
        int endIndex;
        int threadId;

        PingRunnable(String smbUrl, int threadID, int startIndex, int endIndex, OnSmbSearchListener callback) {
            this.startIp = smbUrl;
            this.callback = callback;
            this.startIndex = startIndex;
            this.endIndex = endIndex;
            this.threadId = threadID;
        }

        @Override
        public void run() {
            try {
                int lastDotIndex = this.startIp.lastIndexOf(46);
                String newStr = this.startIp.substring(0, lastDotIndex + 1);
                String ipStr = this.startIp.substring(lastDotIndex + 1, this.startIp.length());
                int ipNum = Integer.parseInt(ipStr);
                if (logEnable) {
                    Log.i(TAG, "start ip : " + newStr);
                    Log.i(TAG, " ip : " + ipNum);
                }

                for (int loop
                     = this.startIndex; loop <= this.endIndex && !stopRun && loop <= 255 && loop >= 1; ++loop) {
                    String tmp = newStr + loop;
                    if (ShareUtils.pingHost(tmp, timeOut)) {
                        SmbDevice smbdevice = new SmbDevice(tmp);
                        deviceList.add(smbdevice);
                        callback.onSearch(OnSmbSearchListener.UPDATE_DEVICE_ADD, smbdevice);
                    }
                }
            } finally {
                if (scanCompleted.incrementAndGet() == MAX_THREAD_COUNT) {
                    isScanning = false;
                    if (stopRun) {
                        this.callback.onSearch(OnSmbSearchListener.UPDATE_DEVICE_CANCEL, null);
                    } else {
                        this.callback.onSearch(OnSmbSearchListener.UPDATE_DEVICE_DONE, null);
                    }
                }

            }

            if (stopRun) {
                isScanning = false;
            }

        }
    }
}

