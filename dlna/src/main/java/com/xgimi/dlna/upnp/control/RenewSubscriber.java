package com.xgimi.dlna.upnp.control;

import com.blankj.utilcode.util.LogUtils;
import com.xgimi.dlna.upnp.ControlPoint;
import com.xgimi.dlna.utils.ThreadCore;

/**
 * author : joker.peng
 * e-mail : joker.peng@xgimi.com
 * date   : 2020/8/4 10:07
 * desc   :
 */
public class RenewSubscriber extends ThreadCore {
    public final static long INTERVAL = 120;
    ////////////////////////////////////////////////
    //	Constructor
    ////////////////////////////////////////////////

    public RenewSubscriber(ControlPoint ctrlp) {
        setControlPoint(ctrlp);
    }

    ////////////////////////////////////////////////
    //	Member
    ////////////////////////////////////////////////

    private ControlPoint ctrlPoint;

    public void setControlPoint(ControlPoint ctrlp) {
        ctrlPoint = ctrlp;
    }

    public ControlPoint getControlPoint() {
        return ctrlPoint;
    }

    ////////////////////////////////////////////////
    //	Thread
    ////////////////////////////////////////////////

    public void run() {
        ControlPoint ctrlp = getControlPoint();
        long renewInterval = INTERVAL * 1000;
        while (isRunnable() == true) {

            try {
                Thread.sleep(renewInterval);
            } catch (InterruptedException e1) {
            }

            try {
                long time1 = System.currentTimeMillis();
                ctrlp.renewSubscriberService();
                long time2 = System.currentTimeMillis();
                //			log.e("ctrlp.renewSubscriberService() cost time = " + (time2 - time1));
            } catch (Exception e) {
                LogUtils.e("dlna_framework", "catch exception!!!e = " + e.getMessage());
            }

        }
    }
}

