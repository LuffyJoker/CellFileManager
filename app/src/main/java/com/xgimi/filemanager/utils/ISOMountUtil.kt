package com.xgimi.filemanager.utils

import android.os.Build
import android.os.RemoteException
import android.os.storage.IISOActionListener
import android.os.storage.IMountService
import android.text.TextUtils
import android.util.Log
import com.xgimi.device.GmMountManager.GmMountListener
import com.xgimi.device.GmMountManager.getIsoFileMountPath
import com.xgimi.device.GmMountManager.mountIso
import com.xgimi.filemanager.manager.ServiceManager
import java.io.File
import java.util.*

/**
 *    author : joker.peng
 *    e-mail : joker.peng@xgimi.com
 *    date   : 2020/8/19 18:14
 *    desc   :
 */
object ISOMountUtil {
    private val TAG = ISOMountUtil::class.java.simpleName

    private var isoLargeFileSize: Long = 0
    private var isoLargeFilePath: String? = null

    @Volatile
    private lateinit var mMountService: IMountService

    private fun getMountService(): IMountService {
        synchronized(this) {
            if (mMountService == null) {
                mMountService = IMountService.Stub.asInterface(ServiceManager.getService("mount"))
            }
        }
        return mMountService
    }

    /**
     * 针对918获取蓝光ISO文件的可播放路径，其他平台不好说，具体用法参见Class Header
     *
     * @param isoFilePath
     * @return
     */
    fun getIsoLargeFilePath(isoFilePath: String?): String? {
        if (isAndroidO()) { // TODO: 2020/6/18 挂载 iso 返回 “”，此处要修改
            var mountPath = getIsoFileMountPath(isoFilePath)
            Log.i(TAG, " getIsoLargeFilePath, mount path : $mountPath")
            if (TextUtils.isEmpty(mountPath)) { // TODO: 2020/6/18 挂载失败
                mountIso(isoFilePath, object : GmMountListener {
                    override fun onISOStateChange(filePath: String?, state: Int) {
                        Log.i(TAG, " mount path is:$filePath; state = $state")
                    }
                })
                mountPath = getIsoFileMountPath(isoFilePath)
                Log.i(TAG, " mount path : $mountPath")
            }
            if (mountPath == null) {
                return null
            }
            findISOLargeFile(File(mountPath))
            return if (isoLargeFilePath != null) {
                isoLargeFilePath
            } else null
        } else {
            if (mountISOs(isoFilePath)) {
                val isoPath = listISOFileMountPaths(isoFilePath) ?: return null
                isoLargeFileSize = 0
                isoLargeFilePath = null
                findISOLargeFile(File(isoPath))
                if (isoLargeFilePath != null) {
                    return isoLargeFilePath
                }
            }
        }
        return null
    }

    private fun isAndroidO(): Boolean {
        return Build.VERSION.SDK_INT >= 26
    }

    /**
     * 卸载ISO文件，在合适的时机
     */
    fun unMountAll() {
        val isoPath = "/mnt/iso/"
        val file = File(isoPath)
        if (file.exists()) {
            val lists = file.list()
            for (path in lists) {
                unmountISO(isoPath + path, true)
            }
        }
    }

    private fun mountISOs(path: String?): Boolean {
        val isoList: ArrayList<String> = ISOTool.getISORootDir()
        for (index in isoList.indices) {
            if (unmountISO(path, true)) {
                Log.i(TAG, "unmountISO!!")
            }
        }
        try {
            return getMountService().mountISO(
                path,
                object : IISOActionListener.Stub() {
                    override fun onISOEvent(s: String?, i: Int, i1: Int) {}
                }, 1
            )
        } catch (e: RemoteException) {
            Log.e(TAG, "Failed to mount ISO", e)
        }
        return false
    }

    private fun findISOLargeFile(dir: File) {
        if (!dir.exists() || !dir.isDirectory) {
            return
        }
        val files = dir.listFiles() ?: return
        for (file in files) {
            if (file.isDirectory) {
                findISOLargeFile(file)
            } else if (file.isFile) {
                if (file.length() > isoLargeFileSize) {
                    isoLargeFileSize = file.length()
                    isoLargeFilePath = file.path
                }
            }
        }
    }

    private fun listISOFileMountPaths(isoPath: String?): String? {
        requireNotNull(isoPath) { "filename cannot be null" }
        try {
            return getMountService().getISOFileMountPath(isoPath)
        } catch (e: RemoteException) {
            Log.e(TAG, "Failed to get the mount mPathText of the ISO file", e)
        }
        return null
    }

    private fun unmountISO(filename: String?, force: Boolean): Boolean {
        requireNotNull(filename) { "filename cannot be null" }
        try {
            return getMountService().unmountISO(filename, force)
        } catch (e: RemoteException) {
            Log.e(TAG, "Failed to unmount ISO", e)
        }
        return false
    }

}
