package com.xgimi.dlna.upnp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.xgimi.dlna.proxy.IDeviceChangeListener;

/**
 * author : joker.peng
 * e-mail : joker.peng@xgimi.com
 * date   : 2020/8/4 10:19
 * desc   :
 */
public abstract class AbstractDeviceChangeBrocastReceiver extends BroadcastReceiver {
    protected IDeviceChangeListener mListener;

    @Override
    public void onReceive(Context context, Intent intent) {

    }

    public void setListener(IDeviceChangeListener listener) {
        mListener = listener;
    }

}
