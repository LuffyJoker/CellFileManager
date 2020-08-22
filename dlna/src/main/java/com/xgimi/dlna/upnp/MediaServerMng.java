package com.xgimi.dlna.upnp;

import android.content.Context;

/**
 * author : joker.peng
 * e-mail : joker.peng@xgimi.com
 * date   : 2020/8/4 10:18
 * desc   :
 */
public class MediaServerMng extends AbstractMediaMng {

    public MediaServerMng(Context context) {
        super(context);

    }

    @Override
    public void sendAddBrocast(Context context) {
        DMSDeviceBrocastFactory.sendAddBrocast(context);
    }

    @Override
    public void sendRemoveBrocast(Context context, boolean isSelected) {
        DMSDeviceBrocastFactory.sendRemoveBrocast(context, isSelected);
    }

    @Override
    public void sendClearBrocast(Context context) {
        DMSDeviceBrocastFactory.sendClearBrocast(context);
    }

}

