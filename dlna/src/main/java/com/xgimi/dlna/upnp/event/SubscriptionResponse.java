package com.xgimi.dlna.upnp.event;

import com.xgimi.dlna.http.HTTP;
import com.xgimi.dlna.http.HTTPResponse;
import com.xgimi.dlna.upnp.UPnP;

/**
 * author : joker.peng
 * e-mail : joker.peng@xgimi.com
 * date   : 2020/8/4 9:47
 * desc   :
 */
public class SubscriptionResponse extends HTTPResponse {
    ////////////////////////////////////////////////
    //	Constructor
    ////////////////////////////////////////////////

    public SubscriptionResponse() {
        setServer(UPnP.getServerName());
    }

    public SubscriptionResponse(HTTPResponse httpRes) {
        super(httpRes);
    }

    ////////////////////////////////////////////////
    //	Error
    ////////////////////////////////////////////////

    public void setResponse(int code) {
        setStatusCode(code);
        setContentLength(0);
    }

    ////////////////////////////////////////////////
    //	Error
    ////////////////////////////////////////////////

    public void setErrorResponse(int code) {
        setStatusCode(code);
        setContentLength(0);
    }

    ////////////////////////////////////////////////
    //	SID
    ////////////////////////////////////////////////

    public void setSID(String id) {
        setHeader(HTTP.SID, Subscription.toSIDHeaderString(id));
    }

    public String getSID() {
        return Subscription.getSID(getHeaderValue(HTTP.SID));
    }

    ////////////////////////////////////////////////
    //	Timeout
    ////////////////////////////////////////////////

    public void setTimeout(long value) {
        setHeader(HTTP.TIMEOUT, Subscription.toTimeoutHeaderString(value));
    }

    public long getTimeout() {
        return Subscription.getTimeout(getHeaderValue(HTTP.TIMEOUT));
    }
}

