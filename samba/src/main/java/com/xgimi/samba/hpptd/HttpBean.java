package com.xgimi.samba.hpptd;

/**
 * author : joker.peng
 * e-mail : joker.peng@xgimi.com
 * date   : 2020/8/26 17:08
 * desc   :
 */
public class HttpBean {
    private static String mPassword;
    private static String mName;
    private static String mSmbUrl;
    private static String mIpaddress;

    public HttpBean() {
    }

    public static String getmSmbUrl() {
        return mSmbUrl;
    }

    public static void setmSmbUrl(String SmbUrl) {
        mSmbUrl = SmbUrl;
    }

    public static String getmPassword() {
        return mPassword;
    }

    public static void setmPassword(String Password) {
        mPassword = Password;
    }

    public static String getmName() {
        return mName;
    }

    public static void setmName(String Name) {
        mName = Name;
    }

    public static void setmIpAddress(String ipaddress) {
        mIpaddress = ipaddress;
    }

    public static String getmIpAddress() {
        return mIpaddress;
    }

}

