package com.xgimi.filemanager.utils

import android.content.Context
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.os.Handler
import android.os.Message
import android.widget.ImageView
import com.blankj.utilcode.util.StringUtils
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.signature.ObjectKey
import com.xgimi.filemanager.R
import com.xgimi.filemanager.bean.ApkIconModel
import com.xgimi.filemanager.bean.BaseData
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.internal.synchronized
import java.io.File
import java.util.*
import java.util.concurrent.PriorityBlockingQueue
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit

/**
 *    author : joker.peng
 *    e-mail : joker.peng@xgimi.com
 *    date   : 2020/8/17 16:10
 *    desc   :
 */
object ImgLoadUtils {
    private var hasAnim = true

    private var mHandler: Handler? = null
    private val mTask: MutableMap<String, PriorityTask>? =
        Hashtable()
    private val CPU_COUNT = Runtime.getRuntime().availableProcessors()
    private val CORE_POOL_SIZE = CPU_COUNT + 1
    private val MAXIMUM_POOL_SIZE = CPU_COUNT * 2 + 1
    private val KEEP_ALIVE = 3
    private val mExecutorService =
        ThreadPoolExecutor(
            CORE_POOL_SIZE,
            MAXIMUM_POOL_SIZE,
            KEEP_ALIVE.toLong(),
            TimeUnit.SECONDS,
            PriorityBlockingQueue()
        )
    //    private static ThreadPoolExecutor mExecutorService= new ThreadPoolExecutor(2, 2, 3, TimeUnit.MILLISECONDS,
    // new PriorityBlockingQueue<Runnable>());
    //    private static ThreadPoolExecutor mExecutorService= new ThreadPoolExecutor(2, 2, 3, TimeUnit.MILLISECONDS,
// new PriorityBlockingQueue<Runnable>());
    /**
     * 优先级
     * bb.ts ,h264_AVC_DTSH ,TCL Magic Show.mov,wmv2_wma.wmv,
     */
    @Volatile
    private var currentMaxPriority = 0

    /**
     * 初始化
     */
    fun init() {
        mHandler = Handler(Handler.Callback { msg ->
            if (msg.what == 1) {
                val task = msg.obj as PriorityTask
                if (task.mLoaderListenerList != null) {
                    for (listener in task.mLoaderListenerList!!) {
                        listener!!.onCompleted(task.result, false)
                    }
                }
            }
            false
        })
    }

    /**
     * 图片加载请求
     *
     * @param context     上下文
     * @param baseData    文件实体
     * @param placeholder 默认图片
     * @param imageView   显示view
     */
    fun load(
        context: Context?,
        baseData: BaseData,
        placeholder: Int,
        imageView: ImageView
    ) {
        load(context, baseData, placeholder, imageView, null)
    }

    fun load(
        context: Context?,
        baseData: BaseData,
        placeholder: Int,
        imageView: ImageView,
        loaderListener: LoaderListener?
    ) {
        val wh = getImageViewWh(imageView)
        Glide.with(context!!)
            .load(baseData.path)
            .apply(
                RequestOptions()
                    .signature(ObjectKey(baseData.lastModified.toString()))
                    .override(wh[0], wh[1])
                    .placeholder(placeholder)
                    .dontAnimate()
            ).into(imageView)
    }

    fun cancelLoad(view: ImageView) {
        view.setImageBitmap(null)
        view.setTag(R.id.img_tag, null)
        view.clearAnimation()
        view.tag = null
    }

    /**
     * 获取显示大小
     *
     * @param imageView
     * @return
     */
    private fun getImageViewWh(imageView: ImageView): IntArray {
        val wh = IntArray(2)
        if (imageView.layoutParams != null) {
            wh[0] = imageView.layoutParams.width
            wh[1] = imageView.layoutParams.height
        } else {
            wh[0] = imageView.width
            wh[1] = imageView.height
        }
        return wh
    }

    /**
     * 加载apk图标
     *
     * @param context   上下文
     * @param apkPath   apk路径
     * @param imageView 显示view
     */
    fun loadApkIcon(
        context: Context?,
        apkPath: String?,
        imageView: ImageView?
    ) {
        Glide.with(context!!)
            .load(ApkIconModel(apkPath))
            .apply(RequestOptions().placeholder(R.mipmap.new_file_icon_apk))
            .into(imageView!!)
    }

    /**
     * 取消加载apk图标，释放资源
     *
     * @param path
     */
    @InternalCoroutinesApi
    @Synchronized
    fun cancleLoadApk(path: String) {
        cancelLoadTaskByPath(path, true)
    }

    /**
     * 采用了新的办法获取APK图标，之前的失败是因为android中存在的一个BUG,通过
     * appInfo.publicSourceDir = apkPath;来修正这个问题，详情参见:
     * http://code.google.com/p/android/issues/detail?id=9151
     *
     * @param context 上下文
     * @param apkPath apk路径
     * @return icon Drawable
     */
    private fun getApkIcon(
        context: Context,
        apkPath: String
    ): Drawable? {
        val file = File(apkPath)
        if (!file.exists()) {
            return null
        }
        val pm = context.packageManager
        val info = pm.getPackageArchiveInfo(
            apkPath,
            PackageManager.GET_ACTIVITIES
        )
        if (info != null) {
            val appInfo = info.applicationInfo
            appInfo.sourceDir = apkPath
            appInfo.publicSourceDir = apkPath
            return appInfo.loadIcon(pm)
        }
        return null
    }

    /**
     * 加载视频缩略图
     *
     * @param path           视频地址
     * @param loaderListener 加载回调
     */
    @InternalCoroutinesApi
    @Synchronized
    fun loaderThubnail(path: String, loaderListener: LoaderListener?): Boolean {
        val cacheDir: String = MediaUtil.getVideoThumbnailCacheDir(path)
        val file = File(cacheDir + File.separator + path.hashCode())
        if (file.exists()) {
            loaderListener?.onCompleted(file.path, true)
            return true
        }
        synchronized(mTask!!) {
            var task = mTask[path]
            if (task == null) {
                task = VideoThubnailTask(path, loaderListener)
                mTask[path] = task
                mExecutorService.execute(task)
            }
            if (loaderListener != null) {
                task.mLoaderListenerList!!.add(loaderListener)
            }
            task.increasePriority()
        }
        return false
    }

    /**
     * 取消加载视频缩略图
     *
     * @param videoPath
     */
    @InternalCoroutinesApi
    fun cancelLoadVideoThumbnail(videoPath: String) {
        cancelLoadTaskByPath(videoPath, true)
    }

    fun cancelAllLoadVideoThumbnail() {}

    /**
     * 取消后台加载图片任务
     *
     * @param path 待加载文件路径
     */
    @InternalCoroutinesApi
    @Synchronized
    private fun cancelLoadTaskByPath(
        path: String,
        remove: Boolean
    ) {
        if (!StringUtils.isEmpty(path)) {
            synchronized(mTask!!) {
                if (mTask != null && mTask.containsKey(path)) { //清除监听
                    mTask[path]!!.mLoaderListenerList!!.clear()
                    mTask.remove(path)
                    //清除待处理任务
                    if (remove) {
                        val queue =
                            mExecutorService.queue
                        if (queue != null) {
                            val iterator =
                                queue.iterator()
                            while (iterator.hasNext()) {
                                val runnable = iterator.next()
                                if (runnable is PriorityTask && path == runnable.mPath) {
                                    runnable.mLoaderListenerList!!.clear()
                                    runnable.cancel()
                                    iterator.remove()
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * 优先级任务
     */
    internal abstract class PriorityTask(val mPath: String, loaderListener: LoaderListener?) :
        Runnable, Comparable<PriorityTask?> {
        var isCancelled = false
        var isRunning = false
        var isCompleted = false
        var priority = 0
        var mLoaderListenerList: MutableList<LoaderListener?>? =
            ArrayList()
        var result: Any? = null
        fun increasePriority() {
            currentMaxPriority++
            priority = currentMaxPriority
        }

        @InternalCoroutinesApi
        override fun run() {
            if (isCancelled) {
                return
            }
            isRunning = true
            result = doTast()
            synchronized(mTask!!) {
                mTask?.remove(mPath)
            }
            isRunning = false
            isCompleted = true
            if (mHandler != null) {
                val message: Message = mHandler!!.obtainMessage()
                message.what = 1
                message.obj = this
                mHandler!!.sendMessage(message)
            }
        }

        fun cancel() {
            isCancelled = true
        }

        abstract fun doTast(): Any?

        override fun compareTo(other: PriorityTask?): Int {
            if (priority < other?.priority!!) {
                return 1
            }
            return if (priority > other.priority) {
                -1
            } else 0
        }

        init {
            mLoaderListenerList!!.add(loaderListener)
        }
    }

    private class VideoThubnailTask : PriorityTask {

        constructor(path: String, loaderListener: LoaderListener?) : super(path, loaderListener)

        override fun doTast(): Any? {
            return MediaUtil.getVideoThumbnailPath(mPath)
        }
    }

    /**
     * 清除图片缓存
     *
     * @param context 上下文
     */
    fun clearMemory(context: Context?) {
        Glide.get(context!!).clearMemory()
    }

    interface LoaderListener {
        fun onCompleted(result: Any?, exist: Boolean)
        fun onLoadFailed(e: Throwable?)
    }
}
