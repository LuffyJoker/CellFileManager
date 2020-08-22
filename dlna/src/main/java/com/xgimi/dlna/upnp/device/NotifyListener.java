package com.xgimi.dlna.upnp.device;

import com.xgimi.dlna.upnp.ssdp.SSDPPacket;

/**
 * author : joker.peng
 * e-mail : joker.peng@xgimi.com
 * date   : 2020/8/4 10:00
 * desc   :
 */
public interface NotifyListener {
    void deviceNotifyReceived(SSDPPacket ssdpPacket);
}
