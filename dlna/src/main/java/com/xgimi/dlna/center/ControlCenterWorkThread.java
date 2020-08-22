package com.xgimi.dlna.center;

import android.content.Context;

import com.blankj.utilcode.util.LogUtils;
import com.xgimi.dlna.upnp.ControlPoint;
import com.xgimi.dlna.utils.CommonUtil;

/**
 * author : joker.peng
 * e-mail : joker.peng@xgimi.com
 * date   : 2020/8/4 10:28
 * desc   :
 */
public class ControlCenterWorkThread extends Thread {

    private static final int REFRESH_DEVICES_INTERVAL = 30 * 1000;

    public interface ISearchDeviceListener {
        void onSearchComplete(boolean searchSuccess);
    }

    private ControlPoint mCP = null;
    private Context mContext = null;
    private boolean mStartComplete = false;
    private boolean mIsExit = false;
    private ISearchDeviceListener mSearchDeviceListener;

    public ControlCenterWorkThread(Context context, ControlPoint controlPoint) {
        mContext = context;
        mCP = controlPoint;
    }

    public void setCompleteFlag(boolean flag) {
        mStartComplete = flag;
    }

    public void setSearchListener(ISearchDeviceListener listener) {
        mSearchDeviceListener = listener;
    }

    public void awakeThread() {
        synchronized (this) {
            notifyAll();
        }
    }


    public void reset() {
        setCompleteFlag(false);
        awakeThread();
    }

    public void exit() {
        mIsExit = true;
        awakeThread();
    }


    @Override
    public void run() {
        LogUtils.e("dlna_framework", "ControlCenterWorkThread run...");

        while (true) {
            if (mIsExit) {
                mCP.stop();
                break;
            }

            refreshDevices();

            synchronized (this) {
                try {
                    wait(REFRESH_DEVICES_INTERVAL);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        LogUtils.e("dlna_framework", "ControlCenterWorkThread over...");
    }

    private void refreshDevices() {
        LogUtils.e("dlna_framework", "refreshDevices...");
        if (!CommonUtil.checkNetworkState(mContext)) {
            return;
        }

        try {
            if (mStartComplete) {
                boolean searchRet = mCP.search();
                LogUtils.e("dlna_framework", "mCP.search() ret = " + searchRet);
                if (mSearchDeviceListener != null) {
                    mSearchDeviceListener.onSearchComplete(searchRet);
                }
            } else {
                boolean startRet = mCP.start();
                LogUtils.e("dlna_framework", "mCP.start() ret = " + startRet);
                if (startRet) {
                    mStartComplete = true;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

