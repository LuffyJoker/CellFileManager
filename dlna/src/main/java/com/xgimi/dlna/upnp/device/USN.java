package com.xgimi.dlna.upnp.device;

/**
 * author : joker.peng
 * e-mail : joker.peng@xgimi.com
 * date   : 2020/8/3 19:49
 * desc   :
 */
public class USN {
    public final static String ROOTDEVICE = "upnp:rootdevice";

    public final static boolean isRootDevice(String usnValue) {
        if (usnValue == null) {
            return false;
        }
        return usnValue.endsWith(ROOTDEVICE);
    }

    public final static String getUDN(String usnValue) {
        if (usnValue == null) {
            return "";
        }
        int idx = usnValue.indexOf("::");
        if (idx < 0) {
            return usnValue.trim();
        }
        String udnValue = new String(usnValue.getBytes(), 0, idx);
        return udnValue.trim();
    }
}


