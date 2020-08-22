package com.xgimi.dlna.upnp;

import java.util.Vector;

/**
 * author : joker.peng
 * e-mail : joker.peng@xgimi.com
 * date   : 2020/8/4 9:43
 * desc   :
 */
public class DeviceList extends Vector {
    ////////////////////////////////////////////////
    //	Constants
    ////////////////////////////////////////////////

    public final static String ELEM_NAME = "deviceList";

    ////////////////////////////////////////////////
    //	Constructor
    ////////////////////////////////////////////////

    public DeviceList() {
    }

    ////////////////////////////////////////////////
    //	Methods
    ////////////////////////////////////////////////

    public Device getDevice(int n) {
        return (Device) get(n);
    }
}
