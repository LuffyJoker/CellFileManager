package com.xgimi.dlna.upnp.device;

/**
 * author : joker.peng
 * e-mail : joker.peng@xgimi.com
 * date   : 2020/8/3 19:49
 * desc   :
 */

public class MAN {
    public final static String DISCOVER = "ssdp:discover";

    public final static boolean isDiscover(String value) {
        if (value == null) {
            return false;
        }
        if (value.equals(MAN.DISCOVER) == true) {
            return true;
        }
        return value.equals("\"" + MAN.DISCOVER + "\"");
    }
}

