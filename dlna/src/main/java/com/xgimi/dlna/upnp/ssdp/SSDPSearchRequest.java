package com.xgimi.dlna.upnp.ssdp;

import com.xgimi.dlna.http.HTTP;
import com.xgimi.dlna.net.HostInterface;
import com.xgimi.dlna.upnp.device.MAN;
import com.xgimi.dlna.upnp.device.ST;

/**
 * author : joker.peng
 * e-mail : joker.peng@xgimi.com
 * date   : 2020/8/4 9:51
 * desc   :
 */
public class SSDPSearchRequest extends SSDPRequest {
    ////////////////////////////////////////////////
    //	Constructor
    ////////////////////////////////////////////////

    public SSDPSearchRequest(String serachTarget, int mx) {
        setMethod(HTTP.M_SEARCH);
        setURI("*");

        setHeader(HTTP.ST, serachTarget);
        setHeader(HTTP.MX, Integer.toString(mx));
        setHeader(HTTP.MAN, "\"" + MAN.DISCOVER + "\"");
    }

    public SSDPSearchRequest(String serachTarget) {
        this(serachTarget, SSDP.DEFAULT_MSEARCH_MX);
    }

    public SSDPSearchRequest() {
        this(ST.ROOT_DEVICE);
    }

    ////////////////////////////////////////////////
    //	HOST
    ////////////////////////////////////////////////

    public void setLocalAddress(String bindAddr) {
        String ssdpAddr = SSDP.ADDRESS;
        if (HostInterface.isIPv6Address(bindAddr) == true) {
            ssdpAddr = SSDP.getIPv6Address();
        }
        setHost(ssdpAddr, SSDP.PORT);
    }

}

