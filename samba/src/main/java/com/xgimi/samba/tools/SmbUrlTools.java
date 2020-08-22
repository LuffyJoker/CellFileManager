package com.xgimi.samba.tools;

import android.text.TextUtils;
import android.util.Log;

import com.xgimi.samba.constants.HttpHelper;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;

/**
 * author : joker.peng
 * e-mail : joker.peng@xgimi.com
 * date   : 2020/7/30 16:18
 * desc   :
 */
public class SmbUrlTools {
    private final static String TAG = "SmbUrlTools";

    public static String convertToHttpUrl(String url, String ip, int port) {
        Log.i(TAG, " path:" + url);
        if (TextUtils.isEmpty(url)) {
            return null;
        }
        try {
            url = URLEncoder.encode(url, "UTF-8");
        } catch (Exception e) {
            e.printStackTrace();
        }
        StringBuilder builder = new StringBuilder("http://")
                .append(ip)
                .append(File.pathSeparator)
                .append(port)
                .append(HttpHelper.CONTENT_EXPORT_URI);
        builder.append(url);

        return builder.toString();
    }

    /**
     * Turn from <b>"/smb=XXX"</b> to <b>"smb://XXX"</b>
     */
    public final static String cropStreamSmbURL(String url) {
        Log.d(HttpHelper.TAG, " cropStreamSmbURL ----------> url = " + url);
        if (TextUtils.isEmpty(url)) {
            return null;
        }
        try {
            url = URLDecoder.decode(url, "UTF-8");
        } catch (UnsupportedEncodingException e1) {
            e1.printStackTrace();
        }
        if (!url.startsWith(HttpHelper.CONTENT_EXPORT_URI)) {
            return url;
        }
        if (url.length() <= HttpHelper.CONTENT_EXPORT_URI.length()) {
            return url;
        }
        // int indexOf = filePaths.indexOf("&");
        // if (indexOf != -1) {
        //     filePaths = filePaths.substring(0, indexOf);
        // }
        return url.substring(HttpHelper.CONTENT_EXPORT_URI.length());
    }

    public final static boolean isSmbUrl(String url) {
        if (TextUtils.isEmpty(url)) {
            return false;
        }
        return true;
    }
}

