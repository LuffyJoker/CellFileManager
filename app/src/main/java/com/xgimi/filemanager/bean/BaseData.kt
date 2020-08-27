package com.xgimi.filemanager.bean

import com.xgimi.filemanager.enums.FileCategory
import com.xgimi.filemanager.utils.FileCategoryUtil
import com.xgimi.filemanager.utils.FileUtil
import com.xgimi.samba.ShareItem
import com.xgimi.samba.bean.CategoryBean
import com.xgimi.samba.bean.ShareDisk
import com.xgimi.samba.bean.ShareFile
import com.xgimi.samba.constants.HttpHelper
import com.xgimi.samba.tools.SmbUrlTools
import java.io.File

/**
 *    author : joker.peng
 *    e-mail : joker.peng@xgimi.com
 *    date   : 2020/8/10 14:35
 *    desc   :
 */
class BaseData : CategoryBean {

    var name: String? = null

    var path: String? = null

    var parentPath: String? = null
        get() {
            if (field == null && path != null) {
                if (path!!.lastIndexOf(File.separator) > 0) {
                    return path!!.substring(0, path!!.lastIndexOf(File.separator))
                }
            }
            return field
        }

    var rootPath: String? = null

    /**
     * 文件类型
     */
    var type = -1

    var size: String? = null

    /**
     * 文件格式
     */
    var format: String? = null

    var description: String? = null

    var artist: String? = null

    var title: String? = null

    var modifyTime: Long = 0

    // 音乐文件在数据库中的关键字
    var id: Long = 0

    // Album art id
    var album: Long = 0

    // Duration, 单位: 秒
    var duration: Long = 0

    var loadStat = 0

    var doubanId = 0

    var doubanUrl: String? = null

    var posterUrl: String? = null

    var lastPlayPosition: Long = 0

    var lastPlayTime: Long = 0

    var nameVisible: String? = null

    //1 his  0 other
    var hisType = 0

    /**
     * 0、本地文件
     * 1、samba 文件
     * 2、 dlna 文件
     */
    var dataSources = 0

    /**
     * 用于标记 video 的类型
     * 0、播放记录
     * 1、最近添加
     * 2、全部视频。
     */
    var videoType = -1

    var lastModified: Long = 0

    var sambaRowPath: String? = null


    constructor()

    var shareItem: ShareItem? = null

    constructor(category: Int) {
        this.category = category
    }

    constructor(rootPath: String?, file: File) {
        val path = file.path
        category = if (file.isDirectory) {
            FileCategory.Folder.ordinal
        } else {
            FileCategoryUtil.getTypeByNameOrPath(path)
        }
        this.path = path;
        this.name = file.name
        this.rootPath = rootPath;
        this.parentPath = file.parent
        lastModified = file.lastModified()
    }


    constructor(shareItem: ShareItem) {
        this.shareItem = shareItem
        sambaRowPath = shareItem.smbPath
        val path: String = SmbUrlTools.convertToHttpUrl(
            shareItem.smbPath,
            HttpHelper.DEFAULT_IP,
            HttpHelper.DEFAULT_SERVER_PORT
        )
        if (shareItem.isDirectory) {
            category = FileCategory.Folder.ordinal
            size = "-"
        } else {
            try {
                size = FileUtil.formatFileSize((shareItem as ShareFile).getFileSize())
            } catch (t: Throwable) {
                t.printStackTrace()
                size = "-"
            }
            category = FileCategoryUtil.getTypeByNameOrPath(path)
        }
        dataSources = 1
        modifyTime = shareItem.changeTime
        this.path = path
        this.name = shareItem.name
        lastModified = if (shareItem is ShareDisk) {
            0
        } else {
            shareItem.lastAccessTime
        }
    }


    var isTitle = false

    override fun toString(): String {
        return ("BaseData [name=" + name + ", mPathText=" + path + ", parentPath="
                + parentPath + ", rootPath=" + rootPath + ", type=" + type
                + ", size=" + size + ", format=" + format + ", description="
                + description + ", artist=" + artist + ", title=" + title
                + ", modifyTime=" + modifyTime + ", id=" + id + ", album="
                + album + ", duration=" + duration
                + ", loadStat=" + loadStat + ", doubanId=" + doubanId
                + ", doubanUrl=" + doubanUrl + ", posterUrl=" + posterUrl
                + ", lastPlayPosition=" + lastPlayPosition + ", lastPlayTime="
                + lastPlayTime + ", nameVisble=" + nameVisible + ", category="
                + category + ", isTitle=" + isTitle + "]")
    }

    override fun equals(o: Any?): Boolean {
        if (this === o) {
            return true
        }
        if (o == null || BaseData::class !== o.javaClass) {
            return false
        }
        val baseData = o as BaseData
        if (videoType != baseData.videoType) {
            return false
        }
        return if (path != null) path == baseData.path else baseData.path == null
    }

    override fun hashCode(): Int {
        var result = if (path != null) path!!.hashCode() else 0
        result = 31 * result + videoType
        return result
    }

    fun clone(): BaseData? {
        val baseData = BaseData()
        baseData.category = category
        baseData.name = name
        baseData.path = path
        baseData.parentPath = parentPath
        baseData.rootPath = rootPath
        baseData.type = type
        baseData.size = size
        baseData.format = format
        baseData.description = description
        baseData.artist = artist
        baseData.title = title
        baseData.modifyTime = modifyTime
        baseData.id = id
        baseData.album = album
        baseData.duration = duration
        baseData.loadStat = loadStat
        baseData.doubanId = doubanId
        baseData.doubanUrl = doubanUrl
        baseData.posterUrl = posterUrl
        baseData.lastPlayPosition = lastPlayPosition
        baseData.lastPlayTime = lastPlayTime
        baseData.nameVisible = nameVisible
        baseData.hisType = hisType
        baseData.dataSources = dataSources
        baseData.videoType = videoType
        baseData.lastModified = lastModified
        return baseData
    }
}