package com.xgimi.filemanager.contentprovider

import android.content.UriMatcher
import android.net.Uri
import android.provider.BaseColumns

/**
 *    author : joker.peng
 *    e-mail : joker.peng@xgimi.com
 *    date   : 2020/8/10 14:23
 *    desc   :
 */
object ContentData {

    const val AUTHORITY = "com.xgimi.filemanager.contentProvider"
    const val TABLE_NAME = "xgimiresources"
    const val ID = "_id"
    const val FILENAME = "fileName"
    const val ALIAS = "alias"
    const val ROOTPATH = "rootPath"
    const val FILEPATH = "filePath"
    const val DOUBANID = "douBanId"
    const val LOADSTAT = "loadStat"
    const val DOUBANURL = "doubanUrl"
    const val POSTERURL = "posterUrl"
    const val SAVEPATH = "savePath"
    const val SINGER = "singer"
    const val ARTIST = "artist"
    const val TITLE = "title"
    const val COMMENT = "comment"
    const val YEAR = "year"
    const val TRACK = "track"
    const val DISC_NO = "disc_no"
    const val COMPOSER = "composer"
    const val ARTIST_SORT = "artistSort"
    const val ALBUM = "album"
    const val ADDTIME = "addTime"
    const val DEVICETYPE = "deviceType"
    const val LASTPLAYTIME = "lastPlayTime"
    const val LASTPLAYPOSITIION = "lastPlayPosition"
    const val DURATION = "duration"
    const val RECORDKEY = "_recordkey"
    const val ISHIDE = "ishide"

    const val DEVICEINFO = 1
    const val VIDEORECORD = 2
    const val POSTERRESOURCE = 3

    const val VIDEORESOURCE = 4
    const val AUDIORESOURCE = 5
    const val PICTURERESOURCE = 6
    const val DOCUMENTRESOURCE = 7
    const val APKSOURCE = 8
    const val MUSICPOSTERSOURCE = 9

    //Uri，外部程序需要访问就是通过这个Uri访问的，这个Uri必须的唯一的。
    val CONTENT_URI_DEVICE = Uri.parse("content://" + AUTHORITY + "/" + DBHelper.deviceInfo)
    val CONTENT_URI_RECORD = Uri.parse("content://" + AUTHORITY + "/" + DBHelper.recordInfo)
    val CONTENT_URI_POSTER = Uri.parse("content://" + AUTHORITY + "/" + DBHelper.posterInfo)
    val CONTENT_URI_VIDEO = Uri.parse("content://" + AUTHORITY + "/" + DBHelper.videoInfo)
    val CONTENT_URI_AUDIO = Uri.parse("content://" + AUTHORITY + "/" + DBHelper.audioInfo)
    val CONTENT_URI_PICTURE = Uri.parse("content://" + AUTHORITY + "/" + DBHelper.pictureInfo)
    val CONTENT_URI_DOCUMENT = Uri.parse("content://" + AUTHORITY + "/" + DBHelper.documentInfo)
    val CONTENT_URI_APK = Uri.parse("content://" + AUTHORITY + "/" + DBHelper.apkInfo)
    val CONTENT_URI_MUSICPOSTER = Uri.parse("content://" + AUTHORITY + "/" + DBHelper.musicInfo)

    //创建 数据库的时候，都必须加上版本信息；并且必须大于4
    const val DATABASE_VERSION = 4
    const val USERS_TABLE_NAME = "devices"

    object DeviceTableData : BaseColumns {
        // 数据集的MIME类型字符串则应该以vnd.android.cursor.dir/开头
        const val CONTENT_TYPE = "vnd.android.cursor.dir/xgimi.devices"
        // 单一数据的MIME类型字符串应该以vnd.android.cursor.item/开头
        const val CONTENT_TYPE_ITME = "vnd.android.cursor.item/xgimi.device.data"
        //自定义匹配码
        const val DEFAULT_SORT_ORDER = "_id desc"
        var uriMatcher: UriMatcher? = null

        init { // 常量UriMatcher.NO_MATCH表示不匹配任何路径的返回码
            uriMatcher = UriMatcher(UriMatcher.NO_MATCH)
            uriMatcher?.addURI(AUTHORITY, DBHelper.deviceInfo, DEVICEINFO)
            uriMatcher?.addURI(AUTHORITY, DBHelper.recordInfo, VIDEORECORD)
            uriMatcher?.addURI(AUTHORITY, DBHelper.posterInfo, POSTERRESOURCE)
            uriMatcher?.addURI(AUTHORITY, DBHelper.videoInfo, VIDEORESOURCE)
            uriMatcher?.addURI(AUTHORITY, DBHelper.audioInfo, AUDIORESOURCE)
            uriMatcher?.addURI(AUTHORITY, DBHelper.pictureInfo, PICTURERESOURCE)
            uriMatcher?.addURI(AUTHORITY, DBHelper.documentInfo, DOCUMENTRESOURCE)
            uriMatcher?.addURI(AUTHORITY, DBHelper.apkInfo, APKSOURCE)
            uriMatcher?.addURI(AUTHORITY, DBHelper.musicInfo, MUSICPOSTERSOURCE)
        }
    }
}