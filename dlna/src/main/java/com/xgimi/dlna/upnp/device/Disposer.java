package com.xgimi.dlna.upnp.device;

import com.blankj.utilcode.util.LogUtils;
import com.xgimi.dlna.upnp.ControlPoint;
import com.xgimi.dlna.utils.ThreadCore;

/**
 * author : joker.peng
 * e-mail : joker.peng@xgimi.com
 * date   : 2020/8/4 9:58
 * desc   :
 */
public class Disposer extends ThreadCore {
    // //////////////////////////////////////////////
    // Constructor
    // //////////////////////////////////////////////

    public Disposer(ControlPoint ctrlp) {
        setControlPoint(ctrlp);
    }

    // //////////////////////////////////////////////
    // Member
    // //////////////////////////////////////////////

    private ControlPoint ctrlPoint;

    public void setControlPoint(ControlPoint ctrlp) {
        ctrlPoint = ctrlp;
    }

    public ControlPoint getControlPoint() {
        return ctrlPoint;
    }

    // //////////////////////////////////////////////
    // Thread
    // //////////////////////////////////////////////

    public void run() {
        ControlPoint ctrlp = getControlPoint();
        long monitorInterval = ctrlp.getExpiredDeviceMonitoringInterval() * 1000;

        while (isRunnable() == true) {

            try {
                Thread.sleep(monitorInterval);
            } catch (InterruptedException e) {
            }

            try {

                long time1 = System.currentTimeMillis();
                ctrlp.removeExpiredDevices();
                long time2 = System.currentTimeMillis();
                //		log.e("ctrlp.removeExpiredDevices() cost time = " + (time2 - time1));
            } catch (Exception e) {
                LogUtils.e("dlna_framework" + "catch exception!!!e = " + e.getMessage());
            }


            // ctrlp.print();
        }
    }
}

