package com.xgimi.dlna.upnp.ssdp;

import com.xgimi.dlna.http.HTTP;
import com.xgimi.dlna.http.HTTPStatus;
import com.xgimi.dlna.upnp.Device;
import com.xgimi.dlna.upnp.UPnP;

/**
 * author : joker.peng
 * e-mail : joker.peng@xgimi.com
 * date   : 2020/8/4 9:52
 * desc   :
 */
public class SSDPSearchResponse extends SSDPResponse {
    ////////////////////////////////////////////////
    //	Constructor
    ////////////////////////////////////////////////

    public SSDPSearchResponse() {
        setStatusCode(HTTPStatus.OK);
        setCacheControl(Device.DEFAULT_LEASE_TIME);
        setHeader(HTTP.SERVER, UPnP.getServerName());
        setHeader(HTTP.EXT, "");
    }
}
