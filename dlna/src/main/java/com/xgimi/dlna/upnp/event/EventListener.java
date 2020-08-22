package com.xgimi.dlna.upnp.event;

/**
 * author : joker.peng
 * e-mail : joker.peng@xgimi.com
 * date   : 2020/8/4 9:46
 * desc   :
 */
public interface EventListener {
    void eventNotifyReceived(String uuid, long seq, String varName, String value);
}
