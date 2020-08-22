package com.xgimi.dlna.upnp.control;

import com.xgimi.dlna.http.HTTP;
import com.xgimi.dlna.http.HTTPRequest;
import com.xgimi.dlna.soap.SOAPRequest;
import com.xgimi.dlna.upnp.Service;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * author : joker.peng
 * e-mail : joker.peng@xgimi.com
 * date   : 2020/8/4 9:31
 * desc   :
 */
public class ControlRequest extends SOAPRequest {
    ////////////////////////////////////////////////
    //	Constructor
    ////////////////////////////////////////////////

    public ControlRequest() {
    }

    public ControlRequest(HTTPRequest httpReq) {
        set(httpReq);
    }

    ////////////////////////////////////////////////
    //	Query
    ////////////////////////////////////////////////

    public boolean isQueryControl() {
        return isSOAPAction(Control.QUERY_SOAPACTION);
    }

    public boolean isActionControl() {
        return !isQueryControl();
    }

    ////////////////////////////////////////////////
    //	setRequest
    ////////////////////////////////////////////////

    protected void setRequestHost(Service service) {
        String ctrlURL = service.getControlURL();

        // Thanks for Thomas Schulz (2004/03/20)
        String urlBase = service.getRootDevice().getURLBase();
        if (urlBase != null && 0 < urlBase.length()) {
            try {
                URL url = new URL(urlBase);
                String basePath = url.getPath();
                int baseLen = basePath.length();
                if (0 < baseLen) {
                    if (1 < baseLen || (basePath.charAt(0) != '/')) {
                        ctrlURL = basePath + ctrlURL;
                    }
                }
            } catch (MalformedURLException e) {
            }
        }

        // Thanks for Giordano Sassaroli <sassarol@cefriel.it> (05/21/03)
        setURI(ctrlURL, true);

        // Thanks for Giordano Sassaroli <sassarol@cefriel.it> and Suzan Foster (09/02/03)
        // Thanks for Andre <andre@antiheld.net> (02/18/04)
        String postURL = "";
        if (HTTP.isAbsoluteURL(ctrlURL) == true) {
            postURL = ctrlURL;
        }

        if (postURL == null || postURL.length() <= 0) {
            postURL = service.getRootDevice().getURLBase();
        }

        // Thanks for Rob van den Boomen <rob.van.den.boomen@philips.com> (02/17/04)
        // BUGFIX, set urlbase from location string if not set in description.xml
        if (postURL == null || postURL.length() <= 0) {
            postURL = service.getRootDevice().getLocation();
        }

        String reqHost = HTTP.getHost(postURL);
        int reqPort = HTTP.getPort(postURL);

        setHost(reqHost, reqPort);
        setRequestHost(reqHost);
        setRequestPort(reqPort);
    }

}

