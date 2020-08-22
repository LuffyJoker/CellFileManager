package com.xgimi.dlna.upnp.ssdp;

import com.xgimi.dlna.http.HTTP;

/**
 * author : joker.peng
 * e-mail : joker.peng@xgimi.com
 * date   : 2020/8/4 9:41
 * desc   :
 */
public class SSDPNotifyRequest extends SSDPRequest
{
    ////////////////////////////////////////////////
    //	Constructor
    ////////////////////////////////////////////////

    public SSDPNotifyRequest()
    {
        setMethod(HTTP.NOTIFY);
        setURI("*");
    }
}
