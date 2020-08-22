package com.xgimi.dlna.upnp.device;

import com.xgimi.dlna.upnp.Device;
import com.xgimi.dlna.utils.ThreadCore;

/**
 * author : joker.peng
 * e-mail : joker.peng@xgimi.com
 * date   : 2020/8/4 10:02
 * desc   :
 */
public class Advertiser extends ThreadCore {
    ////////////////////////////////////////////////
    //	Constructor
    ////////////////////////////////////////////////

    public Advertiser(Device dev) {
        setDevice(dev);
    }

    ////////////////////////////////////////////////
    //	Member
    ////////////////////////////////////////////////

    private Device device;

    public void setDevice(Device dev) {
        device = dev;
    }

    public Device getDevice() {
        return device;
    }

    ////////////////////////////////////////////////
    //	Thread
    ////////////////////////////////////////////////

    public void run() {
        Device dev = getDevice();
        long leaseTime = dev.getLeaseTime();
        long notifyInterval;
        while (isRunnable() == true) {
            notifyInterval = (leaseTime / 4) + (long) ((float) leaseTime * (Math.random() * 0.25f));
            notifyInterval *= 1000;
            try {
                Thread.sleep(notifyInterval);
            } catch (InterruptedException e) {
            }
            dev.announce();
        }
    }
}

