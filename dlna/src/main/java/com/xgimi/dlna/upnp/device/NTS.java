package com.xgimi.dlna.upnp.device;

/**
 * author : joker.peng
 * e-mail : joker.peng@xgimi.com
 * date   : 2020/8/3 19:50
 * desc   :
 */
public class NTS {
    public final static String ALIVE = "ssdp:alive";
    public final static String BYEBYE = "ssdp:byebye";
    public final static String PROPCHANGE = "upnp:propchange";

    public final static boolean isAlive(String ntsValue) {
        if (ntsValue == null) {
            return false;
        }
        return ntsValue.startsWith(NTS.ALIVE);
    }

    public final static boolean isByeBye(String ntsValue) {
        if (ntsValue == null) {
            return false;
        }
        return ntsValue.startsWith(NTS.BYEBYE);
    }
}
