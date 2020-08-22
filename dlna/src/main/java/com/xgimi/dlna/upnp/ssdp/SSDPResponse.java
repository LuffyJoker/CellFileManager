package com.xgimi.dlna.upnp.ssdp;

import com.xgimi.dlna.http.HTTP;
import com.xgimi.dlna.http.HTTPResponse;

import java.io.InputStream;

/**
 * author : joker.peng
 * e-mail : joker.peng@xgimi.com
 * date   : 2020/8/4 9:51
 * desc   :
 */
public class SSDPResponse extends HTTPResponse {
    ////////////////////////////////////////////////
    //	Constructor
    ////////////////////////////////////////////////

    public SSDPResponse() {
        setVersion(HTTP.VERSION_11);
    }

    public SSDPResponse(InputStream in) {
        super(in);
    }

    ////////////////////////////////////////////////
    //	ST (SearchTarget)
    ////////////////////////////////////////////////

    public void setST(String value) {
        setHeader(HTTP.ST, value);
    }

    public String getST() {
        return getHeaderValue(HTTP.ST);
    }

    ////////////////////////////////////////////////
    //	Location
    ////////////////////////////////////////////////

    public void setLocation(String value) {
        setHeader(HTTP.LOCATION, value);
    }

    public String getLocation() {
        return getHeaderValue(HTTP.LOCATION);
    }

    ////////////////////////////////////////////////
    //	USN
    ////////////////////////////////////////////////

    public void setUSN(String value) {
        setHeader(HTTP.USN, value);
    }

    public String getUSN() {
        return getHeaderValue(HTTP.USN);
    }

    ////////////////////////////////////////////////
    //	MYNAME
    ////////////////////////////////////////////////

    public void setMYNAME(String value) {
        setHeader(HTTP.MYNAME, value);
    }

    public String getMYNAME() {
        return getHeaderValue(HTTP.MYNAME);
    }

    ////////////////////////////////////////////////
    //	CacheControl
    ////////////////////////////////////////////////

    public void setLeaseTime(int len) {
        setHeader(HTTP.CACHE_CONTROL, "max-age=" + Integer.toString(len));
    }

    public int getLeaseTime() {
        String cacheCtrl = getHeaderValue(HTTP.CACHE_CONTROL);
        return SSDP.getLeaseTime(cacheCtrl);
    }

    ////////////////////////////////////////////////
    //	getHeader (Override)
    ////////////////////////////////////////////////

    public String getHeader() {
        StringBuffer str = new StringBuffer();

        str.append(getStatusLineString());
        str.append(getHeaderString());
        str.append(HTTP.CRLF); // for Intel UPnP control points.

        return str.toString();
    }

}

