package com.xgimi.dlna.upnp.event;

import com.xgimi.dlna.http.HTTP;
import com.xgimi.dlna.http.HTTPRequest;
import com.xgimi.dlna.http.HTTPResponse;
import com.xgimi.dlna.upnp.Device;
import com.xgimi.dlna.upnp.Service;
import com.xgimi.dlna.upnp.device.NT;

/**
 * author : joker.peng
 * e-mail : joker.peng@xgimi.com
 * date   : 2020/8/4 9:47
 * desc   :
 */
public class SubscriptionRequest extends HTTPRequest {
    ////////////////////////////////////////////////
    //	Constructor
    ////////////////////////////////////////////////

    public SubscriptionRequest() {
        setContentLength(0);
    }

    public SubscriptionRequest(HTTPRequest httpReq) {
        this();
        set(httpReq);
    }

    ////////////////////////////////////////////////
    //	setRequest
    ////////////////////////////////////////////////

    private void setService(Service service) {
        String eventSubURL = service.getEventSubURL();

        // Thanks for Giordano Sassaroli <sassarol@cefriel.it> (05/21/03)
        setURI(eventSubURL, true);

        String urlBaseStr = "";
        Device dev = service.getDevice();
        if (dev != null) {
            urlBaseStr = dev.getURLBase();
        }

        if (urlBaseStr == null || urlBaseStr.length() <= 0) {
            Device rootDev = service.getRootDevice();
            if (rootDev != null) {
                urlBaseStr = rootDev.getURLBase();
            }
        }

        // Thansk for Markus Thurner <markus.thurner@fh-hagenberg.at> (06/11/2004)
        if (urlBaseStr == null || urlBaseStr.length() <= 0) {
            Device rootDev = service.getRootDevice();
            if (rootDev != null) {
                urlBaseStr = rootDev.getLocation();
            }
        }

        // Thanks for Giordano Sassaroli <sassarol@cefriel.it> (09/02/03)
        if (urlBaseStr == null || urlBaseStr.length() <= 0) {
            if (HTTP.isAbsoluteURL(eventSubURL)) {
                urlBaseStr = eventSubURL;
            }
        }

        String reqHost = HTTP.getHost(urlBaseStr);
        int reqPort = HTTP.getPort(urlBaseStr);

        setHost(reqHost, reqPort);
        setRequestHost(reqHost);
        setRequestPort(reqPort);
    }

    public void setSubscribeRequest(Service service, String callback, long timeout) {
        setMethod(Subscription.SUBSCRIBE_METHOD);
        setService(service);
        setCallback(callback);
        setNT(NT.EVENT);
        setTimeout(timeout);
    }

    public void setRenewRequest(Service service, String uuid, long timeout) {
        setMethod(Subscription.SUBSCRIBE_METHOD);
        setService(service);
        setSID(uuid);
        setTimeout(timeout);
    }

    public void setUnsubscribeRequest(Service service) {
        setMethod(Subscription.UNSUBSCRIBE_METHOD);
        setService(service);
        setSID(service.getSID());
    }

    ////////////////////////////////////////////////
    //	NT
    ////////////////////////////////////////////////

    public void setNT(String value) {
        setHeader(HTTP.NT, value);
    }

    public String getNT() {
        return getHeaderValue(HTTP.NT);
    }

    public boolean hasNT() {
        String nt = getNT();
        return (nt != null && 0 < nt.length());
    }

    ////////////////////////////////////////////////
    //	CALLBACK
    ////////////////////////////////////////////////

    private final static String CALLBACK_START_WITH = "<";
    private final static String CALLBACK_END_WITH = ">";

    public void setCallback(String value) {
        setStringHeader(HTTP.CALLBACK, value, CALLBACK_START_WITH, CALLBACK_END_WITH);
    }

    public String getCallback() {
        return getStringHeaderValue(HTTP.CALLBACK, CALLBACK_START_WITH, CALLBACK_END_WITH);
    }

    public boolean hasCallback() {
        String callback = getCallback();
        return (callback != null && 0 < callback.length());
    }

    ////////////////////////////////////////////////
    //	SID
    ////////////////////////////////////////////////

    public void setSID(String id) {
        setHeader(HTTP.SID, Subscription.toSIDHeaderString(id));
    }

    public String getSID() {
        // Thanks for Grzegorz Lehmann and Stefano Lenzi(12/06/04)
        String sid = Subscription.getSID(getHeaderValue(HTTP.SID));
        if (sid == null) {
            return "";
        }
        return sid;
    }

    public boolean hasSID() {
        String sid = getSID();
        return (sid != null && 0 < sid.length());
    }

    ////////////////////////////////////////////////
    //	Timeout
    ////////////////////////////////////////////////

    public final void setTimeout(long value) {
        setHeader(HTTP.TIMEOUT, Subscription.toTimeoutHeaderString(value));
    }

    public long getTimeout() {
        return Subscription.getTimeout(getHeaderValue(HTTP.TIMEOUT));
    }

    ////////////////////////////////////////////////
    //	post (Response)
    ////////////////////////////////////////////////

    public void post(SubscriptionResponse subRes) {
        super.post(subRes);
    }

    ////////////////////////////////////////////////
    //	post
    ////////////////////////////////////////////////

    public SubscriptionResponse post() {
        HTTPResponse httpRes = post(getRequestHost(), getRequestPort());
        return new SubscriptionResponse(httpRes);
    }
}
