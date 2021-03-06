package com.xgimi.dlna.utils;

import com.xgimi.dlna.upnp.Device;
import com.xgimi.dlna.upnp.MediaItem;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

/**
 * author : joker.peng
 * e-mail : joker.peng@xgimi.com
 * date   : 2020/8/4 10:25
 * desc   :
 */
public class UpnpUtil {

    public static boolean isMediaServerDevice(Device device) {
        return "urn:schemas-upnp-org:device:MediaServer:1".equalsIgnoreCase(device.getDeviceType());
    }


    //	public static boolean isValidRenderDevice(Device device){
    //
    //		if (UpnpUtil.isMediaRenderDevice(device) && !UpnpUtil.isLocalIpAddress(device)){
    //			return true;
    //		}
    //
    //		return false;
    //
    //	}


    //	public static boolean isMediaRenderDevice(Device device){
    //		if ("urn:schemas-upnp-org:device:MediaRenderer:1".equalsIgnoreCase(device.getDeviceType())){
    //			return true;
    //		}
    //		return false;
    //	}

    public final static String DLNA_OBJECTCLASS_MUSICID = "object.item.audioItem";
    public final static String DLNA_OBJECTCLASS_VIDEOID = "object.item.videoItem";
    public final static String DLNA_OBJECTCLASS_PHOTOID = "object.item.imageItem";

    public static boolean isAudioItem(MediaItem item) {
        String objectClass = item.getObjectClass();
        return objectClass != null && objectClass.contains(DLNA_OBJECTCLASS_MUSICID);
    }

    public static boolean isVideoItem(MediaItem item) {
        String objectClass = item.getObjectClass();
        return objectClass != null && objectClass.contains(DLNA_OBJECTCLASS_VIDEOID);
    }

    public static boolean isPictureItem(MediaItem item) {
        String objectClass = item.getObjectClass();
        return objectClass != null && objectClass.contains(DLNA_OBJECTCLASS_PHOTOID);
    }


    public static boolean isLocalIpAddress(Device device) {
        try {
            String addrip = device.getLocation();
            addrip = addrip.substring("http://".length(), addrip.length());
            addrip = addrip.substring(0, addrip.indexOf(":"));
            boolean ret = isLocalIpAddress(addrip);
            ret = false;
            return ret;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    public static boolean isLocalIpAddress(String checkip) {

        boolean ret = false;
        if (checkip != null) {
            try {
                for (Enumeration<NetworkInterface> en
                     = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements(); ) {
                    NetworkInterface intf = en.nextElement();
                    for (Enumeration<InetAddress> enumIpAddr
                         = intf.getInetAddresses(); enumIpAddr.hasMoreElements(); ) {
                        InetAddress inetAddress = enumIpAddr.nextElement();
                        if (!inetAddress.isLoopbackAddress()) {
                            String ip = inetAddress.getHostAddress().toString();

                            if (ip == null) {
                                continue;
                            }
                            if (checkip.equals(ip)) {
                                return true;
                            }
                        }
                    }
                }
            } catch (SocketException ex) {
                ex.printStackTrace();
            }
        }

        return ret;
    }
}

