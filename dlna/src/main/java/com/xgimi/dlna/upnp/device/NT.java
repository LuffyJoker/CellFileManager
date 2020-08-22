package com.xgimi.dlna.upnp.device;

/**
 * author : joker.peng
 * e-mail : joker.peng@xgimi.com
 * date   : 2020/8/3 19:48
 * desc   :
 */
public class NT {
    public final static String ROOTDEVICE = "upnp:rootdevice";
    public final static String EVENT = "upnp:event";

    public final static boolean isRootDevice(String ntValue) {
        if (ntValue == null) {
            return false;
        }
        return ntValue.startsWith(ROOTDEVICE);
    }
}
