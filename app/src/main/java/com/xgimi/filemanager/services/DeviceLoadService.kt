package com.xgimi.filemanager.services

import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.*
import android.util.Log
import com.blankj.utilcode.util.LogUtils
import com.blankj.utilcode.util.ReflectUtils
import com.blankj.utilcode.util.SPUtils
import com.blankj.utilcode.util.StringUtils
import com.xgimi.filemanager.bean.DeviceInfo
import com.xgimi.filemanager.bean.DeviceInfo.DeviceCategory
import com.xgimi.filemanager.contentprovider.BatchOperator
import com.xgimi.filemanager.helper.MountHelper
import com.xgimi.filemanager.helper.ResourceHelper
import com.xgimi.filemanager.helper.UsbPopupHelper
import com.xgimi.filemanager.receivers.BootReceiver
import com.xgimi.filemanager.searcher.LocalFileSearcher
import com.xgimi.filemanager.utils.DocumentsUtil
import com.xgimi.filemanager.utils.ThreadManager
import java.util.*
import java.util.concurrent.ConcurrentLinkedQueue

/**
 *    author : joker.peng
 *    e-mail : joker.peng@xgimi.com
 *    date   : 2020/8/19 17:37
 *    desc   :
 */
class DeviceLoadService : Service() {

    companion object {
        private val TAG = "DeviceLoadService"
        private val USBMANAGER_THREAD_NAME = "USBMANAGERTHREAD"
        /**
         * 开机启动action
         */
        const val ONBOOTSERVICE = "com.xgimi.filemanager.action.ONBOOTSERVICE"
        /**
         * 挂载action
         */
        const val ONMOUNT = "com.xgimi.filemanager.action.ONMOUNTSERVICE"
        /**
         * 卸载action
         */
        const val ONUNMOUNT = "com.xgimi.filemanager.action.ONUNMOUNTSERVICE"
        /**
         * 扫描完成状态广播
         */
        const val SCAN_OVER = "com.xgimi.filemanager.action.SCANOVER"
        /**
         * 重命名文件广播
         */
        const val REMOVE_FILE = "com.xgimi.filemanager.action.FileScanerReceiver.REMOVE_FILE"
        /**
         * 新建文件广播
         */
        const val NEW_FILE = "com.xgimi.filemanager.action.FileScanerReceiver.NEW_FILE"
        /**
         * 挂载点
         */
        const val EXTRA_VOLUME = "Volume"
        /**
         * 扫描路径
         */
        const val EXTRA_FILE_PATH = "FilePath"

        /**
         * 处理事件Handler what
         */
        private const val HANDLE_TASK = 1000
        /**
         * 检查挂载路径Handler what
         */
        private const val CHECK_MOUNT_STATUS = 1999

        /**
         * 定时扫描时间
         */
        private const val TIMING_SCANNING_TIME = 20000
    }

    /**
     * 是否开机启动
     */
    var isBootFirst = false
    /**
     * 当前待事件
     */
    private val mUsbEventList = ConcurrentLinkedQueue<UsbMountTask>()
    /**
     * 定时器，用于定时扫描U盘
     */
    private var mCheckTimer: Timer? = null
    /**
     * 数据库批量操作
     */
    private var mFileScanner: BatchOperator? = null
    private var mUsbHandlerThread: UsbHandlerThread? = null
    private var mEventHandler: Handler? = null
    private var mThreadHandleStatu = false
    /**
     * 文件操作广播.当文件及文件夹重命名或剪切后重新扫描更新文件信息
     */
    private val mFileScanerReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action
            val volume = intent.getStringExtra(EXTRA_VOLUME)
            val filePath = intent.getStringExtra(EXTRA_FILE_PATH)
            if (REMOVE_FILE == action) {
                addNewTask(volume, filePath, UsbMountTask.REMOVEFILE)
            } else if (NEW_FILE == action) {
                addNewTask(volume, filePath, UsbMountTask.NEWFILE)
            }
        }
    }
    /**
     * 由于STR的广播需要动态注册,所以暂时在此处注册
     */
    var mSTRBootBroadcastReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent != null && !StringUtils.isEmpty(intent.action)) {
                val action = intent.action
                if (Intent.ACTION_SCREEN_ON == action) {
                    LogUtils.e("BootReceiver", "set com.xgimi.boot=true")
//                    Reflect.on("android.os.SystemProperties").call("set", BootReceiver.BOOT_PROP, "true")
                } else if (Intent.ACTION_SCREEN_OFF == action) {
                    SPUtils.getInstance()
                        .put(BootReceiver.START_TIME, SystemClock.elapsedRealtime())
                    LogUtils.e(
                        "BootReceiver",
                        "set com.xgimi.boot=false BootReceiver.START_TIME=" + SystemClock.elapsedRealtime()
                    )
//                    Reflect.on("android.os.SystemProperties").call("set", BootReceiver.BOOT_PROP, "false")
                }
            }
        }
    }
    var mUsbBroadcastReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            DocumentsUtil.cleanCache()
            if (Intent.ACTION_MEDIA_EJECT == intent.action) {
                try {
                    val path = intent.data!!.path
                    Log.e(
                        "BootReceiver",
                        "mUsbBroadcastReceiver ACTION_MEDIA_EJECT$path"
                    )
//                    UsbPopupHelper.getInstance(context).hidePopup(path)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        //初始化Handler
        mUsbHandlerThread = UsbHandlerThread()
        mUsbHandlerThread!!.start()
        mEventHandler = Handler(mUsbHandlerThread!!.looper, mUsbHandlerThread)
        mFileScanner = BatchOperator(this)
        mThreadHandleStatu = false
        //清空数据库
        try {
            ResourceHelper.clearDataBase()
        } catch (e: Exception) {
            e.printStackTrace()
        }
//        UsbPopupHelper.getInstance(this).upDateUsbPaths()
        //注册广播
        registerBroadcast()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent == null) {
            return Service.START_NOT_STICKY
        }
        val action = intent.action
        //初始化是 否是开机启动
        if (ONBOOTSERVICE == action) {
            isBootFirst = true
        }
        //发送检查挂载点变化Handler
        mEventHandler!!.removeMessages(CHECK_MOUNT_STATUS)
        mEventHandler!!.sendEmptyMessage(CHECK_MOUNT_STATUS)
        LogUtils.e(TAG, "===========================isBootFirst:$isBootFirst")
        return Service.START_NOT_STICKY
    }

    /**
     * 后台线程处理
     *
     * @author Administrator
     */
    class UsbHandlerThread : HandlerThread(USBMANAGER_THREAD_NAME),
        Handler.Callback {
        override fun handleMessage(msg: Message): Boolean {
            when (msg.what) {
                HANDLE_TASK -> {
                    LogUtils.e(TAG, "HANDLE_TASK>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>")
//                    handleTask()
                }
                LocalFileSearcher.SearchLocalFileDone -> {
                    LogUtils.e(TAG, "SearchLocalFileDone>>>>>>>>>>>>>>>>>>>>>>>>>>>>>")
                    val searchingRootPath = msg.obj as String
//                    onSearchLocalFileDone(searchingRootPath)
                }
                CHECK_MOUNT_STATUS -> {
//                    LogUtils.e(TAG, "CHECK_MOUNT_STATUS>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>$mThreadHandleStatu")
//                    checkMountState()
                }
                else -> {
                }
            }
            return true
        }
    }

    /**
     * 检查挂载点变化
     */
    fun checkMountState() {
        if (!mThreadHandleStatu) {
            val mountList: List<String>? = MountHelper.getMountPathList()
            //添加挂载任务
            for (mountPath in mountList!!) {
                LogUtils.e(TAG, "mountPath$mountPath")
                if (!ResourceHelper.checkDeviceExist(mountPath)) {
                    addNewTask(mountPath, mountPath, UsbMountTask.MOUNTED)
                    break
                }
            }
            //添加卸载任务
            val usbList: List<String>? = ResourceHelper.queryUsbList()
            for (usb in usbList!!) {
                if (usb != null && !mountList.contains(usb)) {
                    addNewTask(usb, usb, UsbMountTask.UNMOUNTED)
                    break
                }
            }
        } else {
            mEventHandler!!.removeMessages(CHECK_MOUNT_STATUS)
            mEventHandler!!.sendEmptyMessageDelayed(CHECK_MOUNT_STATUS, 3000)
        }
        notifyCheckChanged()
    }

    /**
     * 定时发送检测挂载状态变化
     */
    @Synchronized
    fun notifyCheckChanged() {
        LogUtils.e(TAG, "notifyCheckChanged>>>>>>>>>>>>>>>>>>>>>>")
        if (!mThreadHandleStatu) {
            if (mCheckTimer != null) {
                mCheckTimer!!.cancel()
            }
            mCheckTimer = Timer()
            mCheckTimer!!.schedule(object : TimerTask() {
                override fun run() {
                    mEventHandler!!.removeMessages(CHECK_MOUNT_STATUS)
                    mEventHandler!!.sendEmptyMessage(CHECK_MOUNT_STATUS)
                }
            }, TIMING_SCANNING_TIME.toLong())
        }
    }

    /**
     * 注册相关广播
     */
    private fun registerBroadcast() { //注册文件操作广播
        val intentFilter = IntentFilter()
        intentFilter.addAction(REMOVE_FILE)
        intentFilter.addAction(NEW_FILE)
        registerReceiver(mFileScanerReceiver, intentFilter)
        //注册str广播
        val strIntentFilter = IntentFilter()
        strIntentFilter.addAction(Intent.ACTION_SCREEN_ON)
        strIntentFilter.addAction(Intent.ACTION_SCREEN_OFF)
        registerReceiver(mSTRBootBroadcastReceiver, strIntentFilter)
        // TODO: 2020/7/1 开机过程中挂载USB，导致应用崩溃
        val usbFilter = IntentFilter(Intent.ACTION_MEDIA_EJECT)
        usbFilter.addAction(Intent.ACTION_MEDIA_MOUNTED)
        usbFilter.addDataScheme("file")
        registerReceiver(mUsbBroadcastReceiver, usbFilter)
    }

    /**
     * 启动任务处理
     */
    @Synchronized
    private fun sendTask() { //当前并未处理任务
        if (!mThreadHandleStatu) {
            LogUtils.e(TAG, "sendTask >>>>>>>")
            mThreadHandleStatu = true
            mEventHandler!!.sendEmptyMessageDelayed(HANDLE_TASK, 500)
        }
    }


    /**
     * 添加任务到列表
     *
     * @param status
     */
    private fun addNewTask(
        rootPath: String,
        filePath: String,
        status: Int
    ) {
        val mountEvent = UsbMountTask()
        mountEvent.rootPath = rootPath
        mountEvent.scanPath = filePath
        mountEvent.mountStatus = status
        mountEvent.eventHandlerStat = UsbMountTask.WAITHANDLE
        LogUtils.e("addNewTask ", "UsbMountTask$mountEvent")
        mUsbEventList.add(mountEvent)
        if (status == UsbMountTask.MOUNTED) {
            saveDeviceInfo(rootPath)
        } else if (status == UsbMountTask.UNMOUNTED) {
            delDeviceInfo(rootPath)
        }
        sendTask()
    }


    /**
     * 当有u盘卸载掉后
     * 删除相应根路径的文件信息
     * 包括视频  、 音乐 、 文档 、 图片 、应用
     */
    private fun delSourceNew(rootPath: String?, path: String?) {
        LogUtils.i(
            "delSourceNew",
            "--------delSourceNew------rootPath=$rootPath-----path=$path"
        )
        ResourceHelper.deleteDir(path!!)
        if (rootPath == path) {
            ResourceHelper.delDeviceInfoByPath(rootPath)
            LogUtils.i("delSourceNew", "MediaUtil.removeVideoThumbnailCache(rootPath)")
            // MediaUtil.removeVideoThumbnailCache(rootPath);
        }
        for (event in mUsbEventList) {
            if (event.rootPath === path && event.eventHandlerStat == UsbMountTask.HANDLEING) {
                event.eventHandlerStat = UsbMountTask.HANDLED
                sendScanOverBroadcast(event.rootPath, "Unmount")
                mEventHandler!!.sendEmptyMessage(HANDLE_TASK)
                break
            }
        }
    }

    /**
     * 保存usb设备信息
     */
    private fun saveDeviceInfo(rootPath: String) {
        val info = DeviceInfo()
        info.deviceType = DeviceCategory.Usb.ordinal
        info.rootPath = rootPath
        ResourceHelper.saveDeviceInfo(info)
    }

    /**
     * 删除usb设备信息
     *
     * @param rootPath
     */
    private fun delDeviceInfo(rootPath: String) {
        ResourceHelper.delDeviceInfoByPath(rootPath)
    }

    /**
     * 处理任务
     */
    fun handleTask() { //待处理任务不为空
        if (mUsbEventList.size > 0) {
            var haveTask = false
            for (event in mUsbEventList) {
                if (event.eventHandlerStat == UsbMountTask.WAITHANDLE) {
                    haveTask = true
                    val isSuccess = startScan(event)
                    if (isSuccess) {
                        break
                    }
                }
            }
            if (!haveTask) {
                mUsbEventList.clear()
                if (isBootFirst) {
                    isBootFirst = false
                }
                mThreadHandleStatu = false
                notifyCheckChanged()
            }
        }
    }

    /**
     * 开始扫描
     *
     * @param event 带扫描任务
     * @return 是否处理
     */
    private fun startScan(event: UsbMountTask): Boolean {
        return if (event.mountStatus == UsbMountTask.MOUNTED || event.mountStatus == UsbMountTask.NEWFILE) { //挂载处理    直接扫描
            event.eventHandlerStat = UsbMountTask.HANDLEING
            LogUtils.e(TAG, "MOUNTED>>>>mmmmmmmmmmm")
            val builder: LocalFileSearcher.Builder =
                LocalFileSearcher.Builder(this@DeviceLoadService)
                    .searchMode(LocalFileSearcher.SearchMode.FullScan)
                    .searchRootPath(event.rootPath)
                    .searchPath(event.scanPath)
                    .setHandler(mEventHandler)
                    .dbBatchOperator(mFileScanner)
            ThreadManager.execute(builder.build())
            true
        } else if (event.mountStatus == UsbMountTask.UNMOUNTED) { //卸载处理
            event.eventHandlerStat = UsbMountTask.HANDLEING
            delSourceNew(event.rootPath, event.scanPath)
            true
        } else {
            false
        }
    }

    /**
     * 指定路径扫描完成
     *
     * @param searchingRootPath
     */
    fun onSearchLocalFileDone(searchingRootPath: String) {
        for (event in mUsbEventList) {
            if (event.eventHandlerStat == UsbMountTask.HANDLEING) {
                event.eventHandlerStat = UsbMountTask.HANDLED
                LogUtils.e(TAG, "SearchLocalFileDone>>>>>>mmmmmmmmmmm$searchingRootPath")
                sendScanOverBroadcast(event.rootPath, "Mounted")
                mEventHandler!!.sendEmptyMessage(HANDLE_TASK)
                break
            }
        }
    }

    /**
     * 发送扫描完成广播
     *
     * @param rootPath
     */
    private fun sendScanOverBroadcast(
        rootPath: String?,
        event: String
    ) {
        val mountedIntent = Intent(SCAN_OVER)
        mountedIntent.putExtra("RootPath", rootPath)
        mountedIntent.putExtra("EVENT", event)
        mountedIntent.putExtra("isBootScan", isBootFirst)
        sendBroadcast(mountedIntent)
    }

    override fun onDestroy() {
        unregisterReceiver(mFileScanerReceiver)
        unregisterReceiver(mSTRBootBroadcastReceiver)
        unregisterReceiver(mUsbBroadcastReceiver)
        mEventHandler!!.removeCallbacksAndMessages(null)
        if (mCheckTimer != null) {
            mCheckTimer!!.cancel()
        }
        LogUtils.e(TAG, "start DeviceLoadService>mmmonDestroymm")
        super.onDestroy()
    }

    /**
     * 封装usb状态
     *
     * @author Administrator
     */
    class UsbMountTask {
        /**
         * 挂载点
         */
        var rootPath: String? = null
        var scanPath: String? = null
        var mountStatus = 0
        var eventHandlerStat = 0
        override fun toString(): String {
            return ("UsbMountTask [rootPath=" + rootPath + ", scanPath="
                    + scanPath + ", mountStatus=" + mountStatus
                    + ", eventHandlerStat=" + eventHandlerStat + "]")
        }

        companion object {
            /**
             * mountStat 状态
             */
            const val MOUNTED = 0
            const val UNMOUNTED = 1
            const val NEWFILE = 2
            const val REMOVEFILE = 3
            /**
             * 事件处理状态
             * 0未加入扫描队列
             * 1(加入到扫描队列)等待中
             * 2扫描中
             * 3扫描完成
             */
            const val WAITHANDLE = 1
            const val HANDLEING = 2
            const val HANDLED = 3
        }
    }
}