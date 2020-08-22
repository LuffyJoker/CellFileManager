package com.xgimi.filemanager.utils

import com.xgimi.filemanager.enums.FileCategory
import java.util.*

/**
 *    author : joker.peng
 *    e-mail : joker.peng@xgimi.com
 *    date   : 2020/8/10 14:41
 *    desc   :
 */
object FileCategoryUtil {
    var videoTypeList: MutableList<String> = ArrayList()
    var audioTypeList: MutableList<String> = ArrayList()
    var officialTypeList: MutableList<String> = ArrayList()
    var apkTypeList: MutableList<String> = ArrayList()
    var pictureTypeList: MutableList<String> = ArrayList()
    var compressedList: MutableList<String> = ArrayList()
    var textList: MutableList<String> = ArrayList()

    init {
        videoTypeList.add(".avi")
        videoTypeList.add(".asf")
        videoTypeList.add(".wmv")
        videoTypeList.add(".m2t")
        videoTypeList.add(".mts")
        videoTypeList.add(".ts")
        videoTypeList.add(".mpg")
        videoTypeList.add(".m2p")
        videoTypeList.add(".mp4")
        videoTypeList.add(".flv")
        videoTypeList.add(".swf")
        videoTypeList.add(".vob")
        videoTypeList.add(".mkv")
        videoTypeList.add(".divx")
        videoTypeList.add(".xvid")
        videoTypeList.add(".mov")
        videoTypeList.add(".rmvb")
        videoTypeList.add(".rv")
        videoTypeList.add(".3gp")
        videoTypeList.add(".3g2")
        videoTypeList.add(".pmp")
        videoTypeList.add(".tp")
        videoTypeList.add(".trp")
        videoTypeList.add(".rm")
        videoTypeList.add(".webm")
        videoTypeList.add(".m2ts")
        videoTypeList.add(".ssif")
        videoTypeList.add(".mpeg")
        videoTypeList.add(".mpe")
        videoTypeList.add(".m3u8")
        videoTypeList.add(".m4v")
        videoTypeList.add(".xv")

        audioTypeList.add(".mp3")
        audioTypeList.add(".wma")
        audioTypeList.add(".aac")
        audioTypeList.add(".ogg")
        audioTypeList.add(".pcm")
        audioTypeList.add(".m4a")
        audioTypeList.add(".ac3")
        audioTypeList.add(".wav")
        audioTypeList.add(".flac")
        audioTypeList.add(".midi")
        audioTypeList.add(".mid")

        pictureTypeList.add(".jpeg")
        pictureTypeList.add(".jpg")
        pictureTypeList.add(".png")
        pictureTypeList.add(".bmp")
        pictureTypeList.add(".gif")
        pictureTypeList.add(".webp")
        officialTypeList.add(".pdf")
        officialTypeList.add(".txt")
        officialTypeList.add(".xls")
        officialTypeList.add(".doc")
        officialTypeList.add(".ppt")
        officialTypeList.add(".pptx")
        officialTypeList.add(".docx")
        officialTypeList.add(".xlsx")
        apkTypeList.add(".apk")

        //压缩文件
        compressedList.add(".iso")
        compressedList.add(".tar")
        compressedList.add(".gz")
        compressedList.add(".zip")
        compressedList.add(".rar")

        //文本文件
        textList.add(".txt")
    }

    fun isAudioFile(exts: String?): Boolean {
        return if (exts != null && exts !== "") {
            audioTypeList.contains(exts)
        } else false
    }

    fun isVideoFile(exts: String?): Boolean {
        return if (exts != null && exts !== "") {
            videoTypeList.contains(exts)
        } else false
    }

    fun isApkFile(exts: String?): Boolean {
        return if (exts != null && exts !== "") {
            apkTypeList.contains(exts)
        } else false
    }

    fun isPictureFile(exts: String?): Boolean {
        return if (exts != null && exts !== "") {
            pictureTypeList.contains(exts)
        } else false
    }

    fun isOfficialFile(exts: String?): Boolean {
        return if (exts != null && exts !== "") {
            officialTypeList.contains(exts)
        } else false
    }

    fun getTypeByNameOrPath(nameOrPath: String): Int {
        val extension = getExtensionName(nameOrPath)
        if (ShareUtil.isEmpty(extension)) return FileCategory.Other.ordinal
        return if (FileCategoryUtil.isAudioFile(extension)) {
            FileCategory.Music.ordinal
        } else if (FileCategoryUtil.isPictureFile(extension)) {
            FileCategory.Picture.ordinal
        } else if (FileCategoryUtil.isVideoFile(extension)) {
            FileCategory.Video.ordinal
        } else if (FileCategoryUtil.isApkFile(extension)) {
            FileCategory.Apk.ordinal
        } else if (FileCategoryUtil.isOfficialFile(extension)) {
            FileCategory.Document.ordinal
        } else {
            FileCategory.Other.ordinal
        }
    }

    private val DOC_PDF = 5
    private val DOC_XLSX = 4
    private val DOC_DOCX = 3
    private val DOC_PPT = 2
    private val DOC_TXT = 1
    private val DOC_OTHER = 0

    fun getDocType(path: String): Int {
        if (!ShareUtil.isEmpty(path)) {
            val ext = getExtensionName(path)
            if (!ShareUtil.isEmpty(ext)) {
                if (".pdf" == ext) return DOC_PDF else if (".xlsx" == ext || ".xls" == ext) return DOC_XLSX else if (".doc" == ext || ".docx" == ext) return DOC_DOCX else if (".ppt" == ext || ".pptx" == ext) return DOC_PPT else if (".txt" == ext) return DOC_TXT
            }
        }
        return DOC_OTHER
    }

    /**
     * 获取扩展名
     *
     * @param path
     * @return
     */
    fun getExtensionName(path: String): String? {
        val pos = path.lastIndexOf(".")
        return if (pos > 0) path.toLowerCase().substring(pos) else null
    }
}