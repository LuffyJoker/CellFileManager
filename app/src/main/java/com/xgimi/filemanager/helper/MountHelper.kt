package com.xgimi.filemanager.helper

import android.content.Context
import android.os.Build
import android.os.Environment
import android.os.StatFs
import android.os.storage.StorageManager
import android.util.Log
import com.blankj.utilcode.util.LogUtils
import com.blankj.utilcode.util.StringUtils
import com.blankj.utilcode.util.Utils
import com.xgimi.device.GmMountManager.getUsbDevices
import com.xgimi.device.GmMountManager.getVolumeLabel
import com.xgimi.device.GmMountManager.unMountUsb
import com.xgimi.filemanager.R
import com.xgimi.filemanager.bean.DeviceInfo
import com.xgimi.utils.KLog
import java.io.DataInputStream
import java.io.File
import java.io.FileInputStream
import java.util.*

/**
 *    author : joker.peng
 *    e-mail : joker.peng@xgimi.com
 *    date   : 2020/8/10 11:50
 *    desc   :
 */
object MountHelper {
    private val TAG = MountHelper::class.java.simpleName
    private val LOCAL_USB_PATH = "/mnt/sdcard"
    private val LOCAL_USB_PATH_838 = "/storage/emulated/0"
    private val localDeviceList: MutableList<DeviceInfo> = ArrayList()

    private var mInstance: MountHelper? = null

    fun getLocalDeviceList(): MutableList<DeviceInfo> {
        return localDeviceList
    }

    /**
     * 获取磁盘名
     *
     * @param path
     * @return
     */
    fun getUsbName(mContext: Context, path: String): String {
        try {
            if (isLocalStorage(path)) {
                return mContext!!.getString(R.string.sdcard_name)
            }
            if (isNativeDisk(path)) {
                return mContext!!.getString(R.string.usb_native)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                val mStorageManager =
                    mContext?.getSystemService(Context.STORAGE_SERVICE) as StorageManager
                val volumes =
                    mStorageManager.storageVolumes
                try {
                    val storageVolumeClazz = Class
                        .forName("android.os.storage.StorageVolume")
                    //通过反射调用系统hide的方法
                    val getPath = storageVolumeClazz.getMethod("getPath")
                    for (i in volumes.indices) { //获取每个挂载的StorageVolume
                        val storageVolume = volumes[i]
                        //获取路径
                        val storagePath =
                            getPath.invoke(storageVolume) as String
                        if (path != storagePath) {
                            continue
                        }
                        val description = storageVolume.getDescription(mContext)
                        KLog.e(
                            "Err",
                            " i=$i ,storagePath=$storagePath ,description=$description"
                        )
                        return description
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    KLog.e("jason", " e:$e")
                }
            } else {
                val usbName = getVolumeLabel(path)
                return usbName?.trim { it <= ' ' } ?: mContext!!.getString(R.string.usb_name)
            }
        } catch (e: Throwable) {
            e.printStackTrace()
        }
        return mContext!!.getString(R.string.usb_name)
    }

    /**
     * 是否是内置硬盘
     *
     * @param path
     * @return
     */
    private fun isNativeDisk(path: String): Boolean {
        val file = File("/data/system/nativeDisk")
        val strLine: String
        if (file.exists()) {
            try {
                val input = FileInputStream(file)
                val dataIO = DataInputStream(input)
                strLine = dataIO.readLine()
                dataIO.close()
                input.close()
                if (!StringUtils.isEmpty(strLine)) {
                    if (path.contains(strLine)) {
                        return true
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return false
    }

    /**
     * 获取磁盘大小信息
     *
     * @param path
     * @return
     */
    fun getQuotaInfo(path: String?): DeviceInfo? {
        try {
            val deviceInfo = DeviceInfo()
            //获取磁盘容量信息的类
            val file = File(path)
            if (!file.exists()) {
                return null
            }
            val stat = StatFs(path)
            val blockSize = stat.blockSize.toLong()
            //文件系统的总的块数
            val totalBlocks = stat.blockCount.toLong()
            //文件系统上空闲的可用于程序的存储块数
            val availableBlocks = stat.availableBlocks.toLong()
            //总的容量
            val totalSize = blockSize * totalBlocks
            val availableSize = blockSize * availableBlocks
            deviceInfo.quotaAvailable = availableSize
            deviceInfo.quotaTotal = totalSize
            if (isLocalStorage(path) && "full_mstarnapoli" != Build.PRODUCT) {
                deviceInfo.quotaTotal = correctLocalStorageTotalSize(deviceInfo.quotaTotal)
            }
            Log.i(
                "JSize",
                "quotaAvailable=$availableSize---quotaTotal=$totalSize"
            )
            return deviceInfo
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    /**
     * 是否是本地存储
     *
     * @param path 路径
     * @return 是否是本地存储
     */
    fun isLocalStorage(path: String?): Boolean {
        return if (path == null) {
            false
        } else LOCAL_USB_PATH == path || LOCAL_USB_PATH_838 == path || path == getExternalStoragePath()
    }

    /**
     * 获取外置存储路径
     *
     * @return
     */
    fun getExternalStoragePath(): String {
        val file = Environment.getExternalStorageDirectory()
        return if (file != null && file.exists()) {
            file.absolutePath
        } else ""
    }

    private val CORRECT_SIZE = longArrayOf(8, 16, 32, 64, 128, 256, 512, 1024)

    private val GB_SIZE = 1024 * 1024 * 1024.toLong()

    /**
     * 产品需求，显示宣传大小，实际内存存储分区小于宣传大小
     * @param quotaTotal
     * @return
     */
    private fun correctLocalStorageTotalSize(quotaTotal: Long): Long {
        for (i in CORRECT_SIZE.indices) {
            if (quotaTotal / GB_SIZE < CORRECT_SIZE[i]) {
                return CORRECT_SIZE[i] * GB_SIZE
            }
        }
        return quotaTotal
    }

    /**
     * 根据路径获取设备信息
     *
     * @param path
     * @return
     */
    fun getDeviceInfo(mContext: Context, path: String): DeviceInfo {
        val time = System.currentTimeMillis()
        Log.e(TAG, "startTime>>>$time")
        val name = getUsbName(mContext, path)
        val form = ""
        val maxFileSize: Long = -0x1
        val device = DeviceInfo()
        device.deviceName = name
        device.deviceType =
            if (isLocalStorage(path)) DeviceInfo.DeviceCategory.LOCALUSB.ordinal else DeviceInfo.DeviceCategory.Usb.ordinal
        device.rootPath = path
        device.quotaAvailable = 0
        device.quotaTotal = 0
        device.maxFileSize = maxFileSize
        device.deviceForm = form
        return device
    }


    /**
     * 获取挂载点信息
     *
     * @return
     */
    fun getMountPathList(): List<String>? {
        val list: MutableList<String> = mutableListOf()
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                val mStorageManager =
                    Utils.getApp().getSystemService(Context.STORAGE_SERVICE) as StorageManager
                val volumes = mStorageManager.storageVolumes
                try {
                    val storageVolumeClazz = Class.forName("android.os.storage.StorageVolume")
                    //通过反射调用系统 hide 的方法
                    val getPath = storageVolumeClazz.getMethod("getPath")
                    for (i in volumes.indices) { //获取每个挂载的StorageVolume
                        val storageVolume = volumes[i]
                        //获取路径
                        val storagePath = getPath.invoke(storageVolume) as String
                        //名称
                        val description = storageVolume.getDescription(Utils.getApp())
                        //处于挂载状态
                        val mState = storageVolume.state
                        //可移除并且已挂载的状态才用来显示
                        if (Environment.MEDIA_MOUNTED == mState) {
                            list.add(storagePath)
                        }
                        LogUtils.d(
                            TAG, " i=" + i + " ,storagePath=" + storagePath
                                    + " ,description=" + description
                                    + ",state = " + mState
                        )
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    LogUtils.e("jason", " e:$e")
                }
            } else {
                val usbList: MutableList<String>? = getUsbDevices()
                if (usbList != null) {
                    list.addAll(usbList)
                }
            }
        } catch (e: Throwable) {
            e.printStackTrace()
        }
        LogUtils.e(TAG, "getUsbDevices: $list")
        return list
    }


    /**
     * 获取 usb 信息
     */
    fun loadUSBDevice(mContext: Context) {
        localDeviceList.clear()
        val mountList = getMountPathList()
        if (mountList != null && mountList.size >= 0) {
            for (item in mountList) {
                localDeviceList.add(getDeviceInfo(mContext, item))
            }
        }
    }

    /**
     * 通过路径移除usb设备
     *
     * @param path
     */
    fun unMountUSB(path: String) {
        try {
            KLog.d("DDD", "unMountUSB: $path")
            unMountUsb(path)
        } catch (e: Throwable) {
            e.printStackTrace()
        }
    }
}

