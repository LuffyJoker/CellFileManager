package com.xgimi.dlna.upnp;

import java.util.Vector;

/**
 * author : joker.peng
 * e-mail : joker.peng@xgimi.com
 * date   : 2020/8/4 10:05
 * desc   :
 */
public class ServiceList extends Vector {
    ////////////////////////////////////////////////
    //	Constants
    ////////////////////////////////////////////////

    public final static String ELEM_NAME = "serviceList";

    ////////////////////////////////////////////////
    //	Constructor
    ////////////////////////////////////////////////

    public ServiceList() {
    }

    ////////////////////////////////////////////////
    //	Methods
    ////////////////////////////////////////////////

    public Service getService(int n) {
        Object obj = null;
        try {
            obj = get(n);
        } catch (Exception e) {
        }
        return (Service) obj;
    }
}


