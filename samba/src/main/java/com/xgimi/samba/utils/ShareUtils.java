package com.xgimi.samba.utils;

import android.util.Log;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;

/**
 * author : joker.peng
 * e-mail : joker.peng@xgimi.com
 * date   : 2020/7/31 17:08
 * desc   :
 */
public class ShareUtils {

    /**
     * Check if the shared item name is valid and does not contain some invalid characters
     *
     * @param fileName File name to be checked
     * @return Status of the check
     */
    public static boolean isValidSharedItemName(String fileName) {
        switch (fileName) {
            case ".":
            case "..":
            case "/":
                return false;
            default:
                return true;
        }
    }
    public static boolean isEmpty(String fileName){
        return fileName==null || fileName.isEmpty();
    }

    /**
     * 目标主机是否能 ping 通
     * @param host
     * @param timeout
     * @return
     */
    public static boolean pingHost(String host, int timeout) {
        boolean ret = false;
        Socket s = null;
        try {
            InetAddress address = InetAddress.getByName(host);
            SocketAddress sa = new InetSocketAddress(address, 445);
            s = new Socket();
            s.connect(sa, timeout);
            if (s.isConnected()) {
                ret = true;
            }
        } catch (IOException var9) {
            // var9.printStackTrace();
        } finally {
            try {
                if (s != null) {
                    s.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        Log.i("Ping", "ping  -w " + timeout + " " + host + " " + ret);
        return ret;
    }
}
