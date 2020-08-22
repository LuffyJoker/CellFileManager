package com.xgimi.dlna.upnp.device;

import com.xgimi.dlna.upnp.Device;

/**
 * author : joker.peng
 * e-mail : joker.peng@xgimi.com
 * date   : 2020/8/4 10:01
 * desc   :
 */
public interface DeviceChangeListener {
    void deviceAdded(Device dev);

    void deviceRemoved(Device dev);
}

