package com.xgimi.filemanager.searcher

import com.blankj.utilcode.util.StringUtils
import com.xgimi.filemanager.bean.BaseData
import com.xgimi.filemanager.enums.FileCategory
import java.io.File
import java.util.*

/**
 *    author : joker.peng
 *    e-mail : joker.peng@xgimi.com
 *    date   : 2020/8/19 10:37
 *    desc   :
 */
abstract class Searcher {
    /**
     * 要忽略的文件夹
     */
    private val ignorePaths = arrayOf(
        "android",
        "android-sdk",
        "android-sdk-linux",
        "android-sdk-windows",
        "lost.dir",
        "myeclipse",
        "cbox",
        "catstudio",
        "kascend",
        "pptv",
        "BootApk",
        "updateapk",
        "my_download"
    )

    var dataList = mutableListOf<BaseData>()
    var stoped = false
    protected var mRootPath: String? = null
    private var mScanPath: String? = null


    constructor(rootPath: String?, scanPath: String?) {
        mRootPath = rootPath
        mScanPath = scanPath
    }

    /**
     * 扫描
     */
    open fun search() { // 开始扫描
        stoped = false
        dataList.clear()
        scanFilesByRecursion(mScanPath)
        if (!stoped) {
            completedSearch()
        }
    }

    /**
     * 递归扫描
     *
     * @param path
     */
    fun scanFilesByRecursion(path: String?) {
        if (stoped) return
        val files = getFileList(path) ?: return
        val size = files.size
        var i = 0
        while (i < size && !stoped) {
            val f = files[i]
            if (filterFile(f)) {
                i++
                continue
            }
            handFile(f)
            i++
        }
    }

    /**
     * 处理文件
     * @param file 待处理文件
     */
    abstract fun handFile(file: File?)

    /**
     * 完成扫描
     */
    abstract fun completedSearch()

    /**
     * 获取文件夹的 filelist
     *
     * @param path
     * @return
     */
    private fun getFileList(path: String?): Array<File>? {
        if (path == null) {
            return null
        }
        val file = File(path)
        return if (!file.exists()) {
            null
        } else file.listFiles() ?: return null
    }

    /**
     * @param file
     * @return true 过滤    false 不过滤
     */
    fun filterFile(file: File): Boolean {
        val name = file.name
        if (name.startsWith(".") || name.startsWith("$")) return true
        if (file.isDirectory) {
            for (end in ignorePaths) {
                if (file.name.equals(end, ignoreCase = true)) {
                    return true
                }
            }
        }
        return false
    }

    /**
     * 是否显示apk
     * @param baseData
     * @return
     */
    protected fun canShowFile(baseData: BaseData?): Boolean {
        if (baseData == null) return false
        val isApk = baseData.category == FileCategory.Apk.ordinal
        val parentIsQNYK =
            !StringUtils.isEmpty(baseData.parentPath) && baseData.parentPath!!.endsWith("QNYK")
        return !isApk || parentIsQNYK
    }
}
