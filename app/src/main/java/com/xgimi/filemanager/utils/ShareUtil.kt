package com.xgimi.filemanager.utils

import android.util.Log
import java.io.IOException
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.Socket
import java.net.SocketAddress

/**
 *    author : joker.peng
 *    e-mail : joker.peng@xgimi.com
 *    date   : 2020/8/10 14:44
 *    desc   :
 */
object ShareUtil {
    /**
     * Check if the shared item name is valid and does not contain some invalid characters
     *
     * @param fileName File name to be checked
     * @return Status of the check
     */
    fun isValidSharedItemName(fileName: String?): Boolean {
        return when (fileName) {
            ".", "..", "/" -> false
            else -> true
        }
    }

    fun isEmpty(fileName: String?): Boolean {
        return fileName == null || fileName.isEmpty()
    }

    /**
     * 目标主机是否能 ping 通
     * @param host
     * @param timeout
     * @return
     */
    fun pingHost(host: String, timeout: Int): Boolean {
        var ret = false
        var s: Socket? = null
        try {
            val address = InetAddress.getByName(host)
            val sa: SocketAddress = InetSocketAddress(address, 445)
            s = Socket()
            s.connect(sa, timeout)
            if (s.isConnected) {
                ret = true
            }
        } catch (var9: IOException) { // var9.printStackTrace();
        } finally {
            try {
                s?.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        Log.i("Ping", "ping  -w $timeout $host $ret")
        return ret
    }
}