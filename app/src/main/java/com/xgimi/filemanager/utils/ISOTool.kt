package com.xgimi.filemanager.utils

import com.blankj.utilcode.util.Utils
import com.xgimi.filemanager.R
import java.io.File
import java.util.*

/**
 *    author : joker.peng
 *    e-mail : joker.peng@xgimi.com
 *    date   : 2020/8/20 9:39
 *    desc   :
 */
object ISOTool {

    private val Tag = "/mnt/iso"

    fun isISOFolderFile(strPathName: String): Boolean {
        var strPathName = strPathName
        if (strPathName.length < Tag.length) return false
        // 1.把路径转为小写
        strPathName = strPathName.toLowerCase()
        // 2.比较这个路径是否为iso下的路径
        val str = strPathName.substring(0, Tag.length + 1)
        return str.contains(Tag)
    }

    fun getISORootDir(): ArrayList<String> {
        val lisFile = ArrayList<String>()
        val dir = File(Tag)
        val listFiles = dir.listFiles()
        for (currentFile in listFiles) {
            if (currentFile.isFile) {
            } else lisFile.add(currentFile.path)
        }
        return lisFile
    }

    fun getFileList(selectFilePath: String): ArrayList<String>? {
        val fileList = ArrayList<String>()
        val selectFile = File(selectFilePath)
        if (!selectFile.exists()) return fileList
        if (selectFilePath.length <= Tag.length) return fileList
        // 1.解析出路径
        val path = selectFile.parent
        // 2.列举路径下所有的文件，需要过滤不支持的
        // 3.排序
        val myList = scan(path)
        // 4.过滤出被选文件后面的文件
        var remove = -1
        for (i in myList.indices) {
            val str = myList[i]
            if (selectFilePath == str) {
                remove = i
                break
            }
        }
        if (remove < 0) return myList
        for (i in remove until myList.size) {
            fileList.add(myList[i])
        }
        return fileList
    }

    private fun scan(fileDir: String): ArrayList<String> { // 现在只过滤视频
        val VideoExtensions = Utils.getApp().resources.getStringArray(
            R.array.video_filter
        )
        val lisFile = ArrayList<String>()
        val dir = File(fileDir)
        val listFiles = dir.listFiles()
        for (currentFile in listFiles) {
            if (currentFile.isFile) {
                val fileName = currentFile.name
                // 过滤格式
                if (checkExtension(fileName, VideoExtensions)) lisFile.add(currentFile.path)
            }
        }
        // 排序.
        if (lisFile.size > 0) {
            Collections.sort(lisFile)
        }
        return lisFile
    }

    /*
     * 通过文件名判断是什么类型的文件
     */
    private fun checkExtension(
        checked: String,
        extensions: Array<String>
    ): Boolean {
        for (end in extensions) {
            if (checked.toLowerCase().endsWith(end)) {
                return true
            }
        }
        return false
    }
}
