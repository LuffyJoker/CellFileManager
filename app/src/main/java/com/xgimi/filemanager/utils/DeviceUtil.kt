package com.xgimi.filemanager.utils

import android.content.Context
import android.net.Uri
import android.os.Build
import android.text.TextUtils
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.xgimi.filemanager.R
import com.xgimi.filemanager.bean.DeviceInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.NetworkInterface
import java.util.*
import java.util.regex.Pattern

/**
 *    author : joker.peng
 *    e-mail : joker.peng@xgimi.com
 *    date   : 2020/8/7 10:42
 *    desc   :
 */
object DeviceUtil {

    private var deviceInfoList = MutableLiveData<MutableList<DeviceInfo>>()
    private val deviceInfoListTemp = mutableListOf<DeviceInfo>()

    fun getDeviceInfoCache(): LiveData<MutableList<DeviceInfo>> {
        return deviceInfoList
    }

    fun initDeviceList(context: Context) {
        GlobalScope.launch(Dispatchers.IO) {
            getDeviceList(context)
        }
    }

    private suspend fun getDeviceList(context: Context) = withContext(Dispatchers.IO) {
        //添加设备
        initAddDevice(context)
        deviceInfoList.postValue(deviceInfoListTemp)
    }

    /**
     * 初始化添加设备按钮
     */
    private fun initAddDevice(context: Context) {
        val deviceInfo = DeviceInfo()
        deviceInfo.deviceType = DeviceInfo.DeviceCategory.AddDevice.ordinal
        deviceInfo.deviceName = context.getString(R.string.add_device_add)
        deviceInfo.rootPath = "add Device"
        addDevice(deviceInfo)
    }

    /**
     * 添加设备
     *
     * @param deviceInfo
     */
    private fun addDevice(deviceInfo: DeviceInfo?): Boolean {
        if (deviceInfo != null && !deviceInfoListTemp.contains(deviceInfo)) {
            deviceInfoListTemp.add(deviceInfo)
            return true
        }
        return false
    }

    private const val DEVICE_NAME = "XgimiDeviceName"

    fun getDeviceName(context: Context): String? {
        var queryResult = ""
        try {
            val cursor = context.contentResolver.query(
                Uri.parse("content://mstar.tv.usersetting/gimisetting"), null,
                null, null, null
            )
            if (cursor!!.moveToFirst()) {
                queryResult = cursor.getString(cursor.getColumnIndex(DEVICE_NAME))
                Log.d("devicename", "$DEVICE_NAME=$queryResult")
                cursor.close()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            queryResult = "xgimi"
        }
        return queryResult
    }

    fun getDeviceType(): String? {
        return Build.DEVICE
    }

    fun getDeviceVersion(): String? {
        return Build.VERSION.INCREMENTAL
    }

    fun getWireMacAddress(): String? {
        var mac = getMacLevel9("eth[0-9]+")
        if (TextUtils.isEmpty(mac)) {
            mac = getMacNetcfg("eth[0-9]+")
        }
        if (!TextUtils.isEmpty(mac)) {
            mac = mac!!.toUpperCase()
        }
        if (mac == null) mac = ""
        return mac
    }

    private fun getMacNetcfg(name_pattern_rgx: String): String? {
        val proc: Process
        try {
            proc = Runtime.getRuntime().exec("netcfg")
            val `is` =
                BufferedReader(InputStreamReader(proc.inputStream))
            proc.waitFor()
            val result_pattern = Pattern.compile(
                "^([a-z0-9]+)\\s+(UP|DOWN)\\s+([0-9./]+)\\s+.+\\s+([0-9a-f:]+)$",
                Pattern.CASE_INSENSITIVE
            )
            while (`is`.ready()) {
                val info = `is`.readLine()
                val m = result_pattern.matcher(info)
                if (m.matches()) {
                    val name = m.group(1).toLowerCase(Locale.CHINA)
                    val status = m.group(2)
                    val addr = m.group(3)
                    val mac = m.group(4).toUpperCase(Locale.CHINA)
                    Log.d(
                        "xgimi_log_l",
                        "match success name::$name  status::$status  addr::$addr  mac::$mac"
                    )
                    if (name.matches(Regex(name_pattern_rgx))) {
                        return mac
                    }
                }
            }
        } catch (e: Exception) {
        }
        return null
    }

    private fun getMacLevel9(name_pattern_rgx: String): String? {
        try {
            val getHardwareAddress =
                NetworkInterface::class.java.getMethod("getHardwareAddress")
            val nis =
                NetworkInterface.getNetworkInterfaces()
            while (nis.hasMoreElements()) {
                val n = nis.nextElement()
                getHardwareAddress.invoke(n)
                val hw_addr = getHardwareAddress.invoke(n) as ByteArray
                if (hw_addr != null) {
                    val name = n.name.toLowerCase(Locale.CHINA)
                    val mac = MacString(hw_addr)
                    Log.d(
                        "xgimi_log_l",
                        "-------name::$name  mac::$mac  name_pattern_rgx::$name_pattern_rgx"
                    )
                    if (name.matches(Regex(name_pattern_rgx))) {
                        return mac
                    }
                }
            }
        } catch (e: Exception) { // TODO: handle exception
        }
        return ""
    }

    private fun MacString(mac: ByteArray): String {
        val sb = StringBuilder()
        for (v in mac) {
            if (sb.length > 0) {
                sb.append(":")
            }
            sb.append(String.format("%02X", v))
        }
        return sb.toString()
    }

    fun is358(): Boolean {
        try {
            return Build.PRODUCT.contains("bennet")
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return false
    }

    fun is848(): Boolean {
        try {
            val platform = getProp("ro.board.platform")
            return "m7221" == platform || "tl1" == platform
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return false
    }

    fun isChildMode(): Boolean {
        return "true".equals(
            getProp("persist.sys.childmode.state", "false"),
            ignoreCase = true
        )
    }

    fun isSoundMode(): Boolean {
        try {
            return getProp("xgimi.soundmode.type", "0")!!.toInt() != 0
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return false
    }

    /**
     * 获取prop
     *
     * @param prop
     * @return
     */
    fun getProp(prop: String): String? {
        return getProp(prop, null)
    }

    /**
     * 获取prop
     *
     * @param prop
     * @return
     */
    fun getProp(prop: String, defultProp: String?): String? {
        return try {
            val getStringMethod =
                Class.forName("android.os.SystemProperties")
                    .getMethod("get", String::class.java, String::class.java)
            (getStringMethod.invoke(null, prop, defultProp) as String).toString()
        } catch (e: Throwable) {
            Log.d(
                "DDDDTest",
                "getProp: " + prop + "     \n error!!" + e.localizedMessage
            )
            defultProp
        }
    }

    /**
     * 设置prop
     * (每次开机都会被清除)
     *
     * @param mProp
     * @param str_name
     */
    fun setProp(mProp: String?, str_name: String?) {
        try {
            val getStringMethod =
                Class.forName("android.os.SystemProperties")
                    .getMethod("set", String::class.java, String::class.java)
            getStringMethod.invoke(null, mProp, str_name)
        } catch (e: Throwable) {
            e.printStackTrace()
        }
    }


}