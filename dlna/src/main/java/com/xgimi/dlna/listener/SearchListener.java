package com.xgimi.dlna.listener;

import com.xgimi.dlna.upnp.ssdp.SSDPPacket;

/**
 * author : joker.peng
 * e-mail : joker.peng@xgimi.com
 * date   : 2020/8/3 17:32
 * desc   : 搜索 Dlna 设备监听
 */
public interface SearchListener {
    void deviceSearchReceived(SSDPPacket ssdpPacket);
}
