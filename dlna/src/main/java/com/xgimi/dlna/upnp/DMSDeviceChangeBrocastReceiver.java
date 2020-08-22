package com.xgimi.dlna.upnp;

import android.content.Context;
import android.content.Intent;

/**
 * author : joker.peng
 * e-mail : joker.peng@xgimi.com
 * date   : 2020/8/4 10:27
 * desc   :
 */
public class DMSDeviceChangeBrocastReceiver extends AbstractDeviceChangeBrocastReceiver {


    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();


        if (DMSDeviceBrocastFactory.ADD_DEVICES.equalsIgnoreCase(action) ||
                DMSDeviceBrocastFactory.REMOVE_DEVICES.equalsIgnoreCase(action) ||
                DMSDeviceBrocastFactory.CLEAR_DEVICES.equalsIgnoreCase(action)) {
            boolean isSelDeviceChange = intent.getBooleanExtra(DMSDeviceBrocastFactory.REMOVE_EXTRA_FLAG, false);
            if (mListener != null) {
                mListener.onDeviceChange(isSelDeviceChange);
            }
        }

    }
}

