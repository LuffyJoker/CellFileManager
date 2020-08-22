package com.xgimi.dlna.upnp.event;

import java.util.Vector;

/**
 * author : joker.peng
 * e-mail : joker.peng@xgimi.com
 * date   : 2020/8/3 18:05
 * desc   :
 */
public class SubscriberList extends Vector {
    ////////////////////////////////////////////////
    //	Constructor
    ////////////////////////////////////////////////

    public SubscriberList() {
    }

    ////////////////////////////////////////////////
    //	Methods
    ////////////////////////////////////////////////

    public Subscriber getSubscriber(int n) {
        Object obj = null;
        try {
            obj = get(n);
        } catch (Exception e) {
        }
        return (Subscriber) obj;
    }
}


