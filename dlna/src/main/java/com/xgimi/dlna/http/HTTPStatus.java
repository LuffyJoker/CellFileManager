package com.xgimi.dlna.http;

import com.xgimi.dlna.utils.Debug;

import java.util.StringTokenizer;

/**
 * author : joker.peng
 * e-mail : joker.peng@xgimi.com
 * date   : 2020/8/3 17:37
 * desc   : Http 状态
 */
public class HTTPStatus {
    ////////////////////////////////////////////////
    //	Code
    ////////////////////////////////////////////////

    public static final int CONTINUE = 100;
    public static final int OK = 200;
    //	Thanks for Brent Hills (10/20/04)
    public static final int PARTIAL_CONTENT = 206;
    public static final int BAD_REQUEST = 400;
    public static final int NOT_FOUND = 404;
    public static final int PRECONDITION_FAILED = 412;
    //	Thanks for Brent Hills (10/20/04)
    public static final int INVALID_RANGE = 416;
    public static final int INTERNAL_SERVER_ERROR = 500;

    public static final String code2String(int code) {
        switch (code) {
            case CONTINUE:
                return "Continue";
            case OK:
                return "OK";
            case PARTIAL_CONTENT:
                return "Partial Content";
            case BAD_REQUEST:
                return "Bad Request";
            case NOT_FOUND:
                return "Not Found";
            case PRECONDITION_FAILED:
                return "Precondition Failed";
            case INVALID_RANGE:
                return "Invalid Range";
            case INTERNAL_SERVER_ERROR:
                return "Internal Server Error";
        }
        return "";
    }

    ////////////////////////////////////////////////
    //	Constructor
    ////////////////////////////////////////////////

    public HTTPStatus() {
        setVersion("");
        setStatusCode(0);
        setReasonPhrase("");
    }

    public HTTPStatus(String ver, int code, String reason) {
        setVersion(ver);
        setStatusCode(code);
        setReasonPhrase(reason);
    }

    public HTTPStatus(String lineStr) {
        set(lineStr);
    }

    ////////////////////////////////////////////////
    //	Member
    ////////////////////////////////////////////////

    private String version = "";
    private int statusCode = 0;
    private String reasonPhrase = "";

    public void setVersion(String value) {
        version = value;
    }

    public void setStatusCode(int value) {
        statusCode = value;
    }

    public void setReasonPhrase(String value) {
        reasonPhrase = value;
    }

    public String getVersion() {
        return version;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public String getReasonPhrase() {
        return reasonPhrase;
    }

    ////////////////////////////////////////////////
    //	Status
    ////////////////////////////////////////////////

    final public static boolean isSuccessful(int statCode) {
        return 200 <= statCode && statCode < 300;
    }

    public boolean isSuccessful() {
        return isSuccessful(getStatusCode());
    }

    ////////////////////////////////////////////////
    //	set
    ////////////////////////////////////////////////

    public void set(String lineStr) {
        if (lineStr == null) {
            setVersion(HTTP.VERSION);
            setStatusCode(INTERNAL_SERVER_ERROR);
            setReasonPhrase(code2String(INTERNAL_SERVER_ERROR));
            return;
        }

        try {
            StringTokenizer st = new StringTokenizer(lineStr, HTTP.STATUS_LINE_DELIM);

            if (st.hasMoreTokens() == false) {
                return;
            }
            String ver = st.nextToken();
            setVersion(ver.trim());

            if (st.hasMoreTokens() == false) {
                return;
            }
            String codeStr = st.nextToken();
            int code = 0;
            try {
                code = Integer.parseInt(codeStr);
            } catch (Exception e1) {
            }
            setStatusCode(code);

            String reason = "";
            while (st.hasMoreTokens() == true) {
                if (0 <= reason.length()) {
                    reason += " ";
                }
                reason += st.nextToken();
            }
            setReasonPhrase(reason.trim());
        } catch (Exception e) {
            Debug.warning(e);
        }

    }
}

