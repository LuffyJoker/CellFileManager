package com.xgimi.dlna.upnp.device;

/**
 * author : joker.peng
 * e-mail : joker.peng@xgimi.com
 * date   : 2020/8/3 19:49
 * desc   :
 */
public class ST {
    public final static String ALL_DEVICE = "ssdp:all";
    public final static String ROOT_DEVICE = "upnp:rootdevice";
    public final static String UUID_DEVICE = "uuid";
    public final static String URN_DEVICE = "urn:schemas-upnp-org:device:";
    public final static String URN_SERVICE = "urn:schemas-upnp-org:service:";

    public final static boolean isAllDevice(String value) {
        if (value == null) {
            return false;
        }
        if (value.equals(ALL_DEVICE) == true) {
            return true;
        }
        return value.equals("\"" + ALL_DEVICE + "\"");
    }

    public final static boolean isRootDevice(String value) {
        if (value == null) {
            return false;
        }
        if (value.equals(ROOT_DEVICE) == true) {
            return true;
        }
        return value.equals("\"" + ROOT_DEVICE + "\"");
    }

    public final static boolean isUUIDDevice(String value) {
        if (value == null) {
            return false;
        }
        if (value.startsWith(UUID_DEVICE) == true) {
            return true;
        }
        return value.startsWith("\"" + UUID_DEVICE);
    }

    public final static boolean isURNDevice(String value) {
        if (value == null) {
            return false;
        }
        if (value.startsWith(URN_DEVICE) == true) {
            return true;
        }
        return value.startsWith("\"" + URN_DEVICE);
    }

    public final static boolean isURNService(String value) {
        if (value == null) {
            return false;
        }
        if (value.startsWith(URN_SERVICE) == true) {
            return true;
        }
        return value.startsWith("\"" + URN_SERVICE);
    }
}


