package com.xgimi.filemanager.bean

import com.xgimi.samba.SmbDevice
import java.io.Serializable

/**
 * author : joker.peng
 * e-mail : joker.peng@xgimi.com
 * date   : 2020/7/30 16:10
 * desc   : 设备信息
 */
class DeviceInfo : Serializable {

    enum class DeviceCategory {
        Usb, Samba, Dlna, Kuaipan, Baiduyun, Yun, AddDevice, DeviceCategory, LOCALUSB
    }

    constructor(device: SmbDevice) {
        deviceType = DeviceCategory.Samba.ordinal
        ip = device.ip
        deviceName = device.name
        rootPath = ip
        hostName = device.name
        userName = device.userName
        passward = device.password
    }

    constructor(
        deviceType: Int,
        hostName: String,
        ip: String,
        rootPath: String?,
        userName: String,
        passward: String,
        quotaTotal: Long,
        quotaAvailable: Long,
        deviceName: String
    ) : super() {
        this.deviceType = deviceType
        this.hostName = hostName
        this.ip = ip
        this.rootPath = rootPath
        this.userName = userName
        this.passward = passward
        this.quotaTotal = quotaTotal
        this.quotaAvailable = quotaAvailable
        this.deviceName = deviceName
        hide = 0
    }

    constructor() {
        deviceType = 0
        deviceName = ""
        hostName = ""
        ip = ""
        rootPath = ""
        userName = ""
        passward = ""
        quotaTotal = 0
        quotaAvailable = 0
        deviceForm = ""
        maxFileSize = 0
    }

    //总容量
    var quotaTotal: Long = 0
    //已用容量
    var quotaAvailable: Long = 0
    //设备名称
    var deviceName: String
    var deviceNameID = 0
    var deviceType: Int
    var hostName: String
    var ip: String
    var rootPath: String?
    var userName: String
    var passward: String
    var deviceForm: String? = null
    var maxFileSize: Long = 0
    var hide = 0
    var isShared = false
    override fun equals(o: Any?): Boolean {
        if (o == null) {
            return false
        }
        if (o !is DeviceInfo) {
            return false
        }
        val device = o
        if (rootPath == null) {
            return false
        }
        if (device.rootPath == null) {
            return false
        }
        return if (rootPath == device.rootPath) {
            true
        } else super.equals(o)
    }

    companion object {
        const val DEVICETYPE = "deviceType"
        const val DEVICENAME = "deviceName"
        const val HOSTNAME = "hostName"
        const val IP = "ip"
        const val ROOTPATH = "rootPath"
        const val USERNAME = "userName"
        const val PASSWARD = "passward"
        const val QUOTATOTAL = "quotaTotal"
        const val QUOTAAVAILABLE = "quotaAvailable"
        const val HIDE = "hide"
    }
}