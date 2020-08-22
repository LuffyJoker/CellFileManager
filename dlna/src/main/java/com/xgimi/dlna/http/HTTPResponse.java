package com.xgimi.dlna.http;

import java.io.InputStream;

/**
 * author : joker.peng
 * e-mail : joker.peng@xgimi.com
 * date   : 2020/8/3 17:39
 * desc   :
 */
public class HTTPResponse extends HTTPPacket {
    ////////////////////////////////////////////////
    //	Constructor
    ////////////////////////////////////////////////

    public HTTPResponse() {
        setVersion(HTTP.VERSION_11);
        setContentType(HTML.CONTENT_TYPE);
        setServer(HTTPServer.getName());
        setContent("");
    }

    public HTTPResponse(HTTPResponse httpRes) {
        set(httpRes);
    }

    public HTTPResponse(InputStream in) {
        super(in);
    }

    public HTTPResponse(HTTPSocket httpSock) {
        this(httpSock.getInputStream());
    }

    ////////////////////////////////////////////////
    //	Status Line
    ////////////////////////////////////////////////

    private int statusCode = 0;

    public void setStatusCode(int code) {
        statusCode = code;
    }

    public int getStatusCode() {
        if (statusCode != 0) {
            return statusCode;
        }
        HTTPStatus httpStatus = new HTTPStatus(getFirstLine());
        return httpStatus.getStatusCode();
    }

    public boolean isSuccessful() {
        return HTTPStatus.isSuccessful(getStatusCode());
    }

    public String getStatusLineString() {
        return "HTTP/" + getVersion() + " " + getStatusCode() + " " + HTTPStatus.code2String(statusCode) + HTTP.CRLF;
    }

    ////////////////////////////////////////////////
    //	getHeader
    ////////////////////////////////////////////////

    public String getHeader() {
        StringBuffer str = new StringBuffer();

        str.append(getStatusLineString());
        str.append(getHeaderString());

        return str.toString();
    }

    ////////////////////////////////////////////////
    //	toString
    ////////////////////////////////////////////////

    public String toString() {
        StringBuffer str = new StringBuffer();

        str.append(getStatusLineString());
        str.append(getHeaderString());
        str.append(HTTP.CRLF);
        str.append(getContentString());

        return str.toString();
    }

    public void print() {
        System.out.println(toString());
    }
}

