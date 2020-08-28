package com.xgimi.filemanager

import android.app.Application
import android.graphics.Color
import com.blankj.utilcode.util.NetworkUtils
import com.blankj.utilcode.util.StringUtils
import com.blankj.utilcode.util.Utils
import com.xgimi.collectionsdk.DataReporter.register
import com.xgimi.collectionsdk.DataReporter.resourceManager
import com.xgimi.filemanager.enums.FileCategory
import com.xgimi.filemanager.utils.FileCategoryUtil
import com.xgimi.filemanager.utils.ScreenAdapterUtils
import com.xgimi.filemanager.utils.ThreadManager
import com.xgimi.gimiskin.sdk.SkinEngine.initEngine
import com.xgimi.view.cell.GlobalConfig
import java.io.File

/**
 *    author : joker.peng
 *    e-mail : joker.peng@xgimi.com
 *    date   : 2020/8/6 16:47
 *    desc   :
 */
class FileManagerApplication : Application() {

    private val types = arrayOf(
        "folder",
        "video",
        "audio",
        "picture",
        "doc",
        "apk",
        "theme",
        "doc",
        "zip",
        "custom",
        "other",
        "favorite"
    )

    companion object {

        @Suppress("unused")
        private const val TAG = "CommonApp"

        private var INSTANCE: FileManagerApplication? = null

        fun getInstance(): FileManagerApplication {
            return INSTANCE!!
        }
    }

    override fun onCreate() {
        super.onCreate()
//        GlobalConfig.SHOW_BORDER = Color.RED
//        GlobalConfig.SHOW_CONTENT = Color.BLUE
        INSTANCE = this
        initEngine(this)
        Utils.init(this)
        ScreenAdapterUtils.register(
            this,
            960f,
            ScreenAdapterUtils.MATCH_BASE_WIDTH,
            ScreenAdapterUtils.MATCH_UNIT_DP
        )
        //初始化数据采集
        register(this)
    }

    fun reportFileCevent(path: String) {
        ThreadManager.execute(Runnable {
            try {
                if (StringUtils.isEmpty(path)) {
                    return@Runnable
                }
                val src: String = when {
                    path.startsWith("http") -> {
                        "dlna"
                    }
                    path.startsWith("/mnt/samba") -> {
                        "samba"
                    }
                    else -> {
                        "usb"
                    }
                }
                val file = File(path)
                if (file != null && file.exists()) {
                    val category: Int
                    category = if (file.isDirectory) {
                        FileCategory.Folder.ordinal
                    } else {
                        FileCategoryUtil.getTypeByNameOrPath(path)
                    }
                    var type: String = types[category]
                    if (path.toLowerCase().endsWith(".zip")) {
                        type = "zip"
                    } else if (path.toLowerCase().endsWith(".rar")) {
                        type = "rar"
                    } else if (path.toLowerCase().endsWith(".iso")) {
                        type = "iso"
                    }
                    val lastIndex = file.name.lastIndexOf('.')
                    var extension: String? = null
                    if (lastIndex > 0) {
                        extension = file.name.substring(lastIndex)
                    }
                    val fileName = file.name.substring(0, lastIndex) //todo 越界异常
                    resourceManager
                        .fileCEvent(
                            src,
                            type,
                            if (NetworkUtils.isAvailable()) 1 else -1,
                            fileName,
                            extension,
                            null
                        )
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        })
    }
}