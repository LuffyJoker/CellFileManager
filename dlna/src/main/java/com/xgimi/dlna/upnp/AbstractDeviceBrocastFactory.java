package com.xgimi.dlna.upnp;

import android.content.Context;

import com.xgimi.dlna.proxy.IDeviceChangeListener;

/**
 * author : joker.peng
 * e-mail : joker.peng@xgimi.com
 * date   : 2020/8/4 10:19
 * desc   :
 */
public abstract class AbstractDeviceBrocastFactory {
    protected Context mContext;

    protected AbstractDeviceChangeBrocastReceiver mReceiver;

    public AbstractDeviceBrocastFactory(Context context) {
        mContext = context;
    }

    public abstract void registerListener(IDeviceChangeListener listener);

    public abstract void unRegisterListener();

}

