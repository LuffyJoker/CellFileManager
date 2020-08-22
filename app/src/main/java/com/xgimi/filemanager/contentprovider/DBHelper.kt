package com.xgimi.filemanager.contentprovider

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.blankj.utilcode.util.LogUtils
import com.xgimi.filemanager.bean.DeviceInfo

/**
 *    author : joker.peng
 *    e-mail : joker.peng@xgimi.com
 *    date   : 2020/8/10 14:23
 *    desc   :
 */
class DBHelper : SQLiteOpenHelper {
    companion object {
        val DB_NAME = "xgimifilemanager.db"
        val deviceInfo = "deviceInfo"
        val recordInfo = "videoreport"
        val posterInfo = "postersource"
        val videoInfo = "videosource"
        val audioInfo = "audiosource"
        val documentInfo = "documentsource"
        val pictureInfo = "picturesource"
        val apkInfo = "apk"
        val musicInfo = "musicPosterInfo"
        val versionCode = 12

        //设备信息
        private val CREATE_TBL_DEVICEINFO = "CREATE TABLE IF NOT EXISTS " +
                deviceInfo + "(_id integer primary key," +
                DeviceInfo.DEVICETYPE + " integer," +
                DeviceInfo.DEVICENAME + " text," +
                DeviceInfo.HOSTNAME + " text," +
                DeviceInfo.IP + " varchar," +
                DeviceInfo.ROOTPATH + " text," +
                DeviceInfo.USERNAME + " text," +
                DeviceInfo.PASSWARD + " text," +
                DeviceInfo.QUOTATOTAL + " long," +
                DeviceInfo.QUOTAAVAILABLE + " long," +
                DeviceInfo.HIDE + " integer" +
                ")"
        //播放记录信息
        private val CREATE_TBL_VIDEORECORD = "CREATE TABLE IF NOT EXISTS " +
                recordInfo + "(_id integer primary key," +
                ContentData.DOUBANID + " integer," +
                ContentData.DOUBANURL + " text," +
                ContentData.POSTERURL + " text," +
                ContentData.FILENAME + " text," +
                ContentData.FILEPATH + " text," +
                ContentData.LASTPLAYTIME + " integer," +
                ContentData.LASTPLAYPOSITIION + " integer," +
                ContentData.DURATION + " long," +
                ContentData.RECORDKEY + " text ," +
                ContentData.ISHIDE + " integer" +
                ")"
        //海报信息
        private val CREATE_TBL_POSTERSOURCE = "CREATE TABLE IF NOT EXISTS " +
                posterInfo + "(_id integer primary key," +
                ContentData.FILENAME + " text," +
                ContentData.ALIAS + " text," +
                ContentData.DOUBANID + " integer," +
                ContentData.POSTERURL + " text," +
                ContentData.SAVEPATH + " text " +
                ")"

        //视频信息
        private val CREATE_TBL_VIDEOSOURCE = "CREATE TABLE IF NOT EXISTS " +
                videoInfo + "(_id integer primary key," +
                ContentData.FILENAME + " text," +
                ContentData.ROOTPATH + " text," +
                ContentData.FILEPATH + " text," +
                ContentData.ADDTIME + " integer," +
                ContentData.LOADSTAT + " integer," +
                ContentData.DOUBANID + " text," +
                ContentData.DOUBANURL + " text," +
                ContentData.POSTERURL + " text ," +
                ContentData.ISHIDE + " integer" +
                ")"
        //音乐信息
        private val CREATE_TBL_AUDIOSOURCE = "CREATE TABLE IF NOT EXISTS " +
                audioInfo + "(_id integer primary key," +
                ContentData.FILENAME + " text," +
                ContentData.ROOTPATH + " text," +
                ContentData.FILEPATH + " text," +
                ContentData.TITLE + " text," +
                ContentData.ALBUM + " text," +
                ContentData.SINGER + " String," +
                ContentData.ARTIST + " text," +
                ContentData.TRACK + " text," +
                ContentData.DISC_NO + " text," +
                ContentData.ADDTIME + " integer ," +
                ContentData.ISHIDE + " integer" +
                ")"

        //音乐海报信息
        private val CREATE_TBL_AUDIOPOSTERSOURCE = "CREATE TABLE IF NOT EXISTS " +
                musicInfo + "(_id integer primary key," +
                ContentData.ARTIST + " text," +
                ContentData.ALBUM + " text," +
                ContentData.TITLE + " text," +
                ContentData.COMMENT + " text," +
                ContentData.YEAR + " text," +
                ContentData.TRACK + " text," +
                ContentData.DISC_NO + " text," +
                ContentData.COMPOSER + " text," +
                ContentData.ARTIST_SORT + " text," +
                ContentData.ADDTIME + " integer ," +
                ContentData.ISHIDE + " integer" +
                ")"

        //图片信息
        private val CREATE_TBL_PICTURESOURCE = "CREATE TABLE IF NOT EXISTS " +
                pictureInfo + "(_id integer primary key," +
                ContentData.FILENAME + " text," +
                ContentData.ROOTPATH + " text," +
                ContentData.FILEPATH + " text," +
                ContentData.ADDTIME + " integer ," +
                ContentData.ISHIDE + " integer" +
                ")"
        //文档信息
        private val CREATE_TBL_DOCUMENTSOURCE = "CREATE TABLE IF NOT EXISTS " +
                documentInfo + "(_id integer primary key," +
                ContentData.FILENAME + " text," +
                ContentData.ROOTPATH + " text," +
                ContentData.FILEPATH + " text," +
                ContentData.ADDTIME + " integer ," +
                ContentData.ISHIDE + " integer" +
                ")"

        //Apk信息
        private val CREATE_TBL_APK = "CREATE TABLE IF NOT EXISTS " +
                apkInfo + "(_id integer primary key," +
                ContentData.FILENAME + " text," +
                ContentData.ROOTPATH + " text," +
                ContentData.FILEPATH + " text," +
                ContentData.ADDTIME + " integer ," +
                ContentData.ISHIDE + " integer" +
                ")"

    }


    constructor(context: Context?) : super(context, DB_NAME, null, versionCode)

    override fun onCreate(db: SQLiteDatabase) { //设备信息
        db.execSQL(CREATE_TBL_DEVICEINFO)
        //视频记录
        db.execSQL(CREATE_TBL_VIDEORECORD)
        //海报信息
        db.execSQL(CREATE_TBL_POSTERSOURCE)
        //视频信息
        db.execSQL(CREATE_TBL_VIDEOSOURCE)
        //音频信息
        db.execSQL(CREATE_TBL_AUDIOSOURCE)
        //图片信息
        db.execSQL(CREATE_TBL_PICTURESOURCE)
        //文档信息
        db.execSQL(CREATE_TBL_DOCUMENTSOURCE)
        //音乐海报信息
        db.execSQL(CREATE_TBL_AUDIOPOSTERSOURCE)
        //应用信息
        db.execSQL(CREATE_TBL_APK)
    }


    override fun onUpgrade(
        db: SQLiteDatabase,
        oldVersion: Int,
        newVersion: Int
    ) { //删除设备信息表
        db.execSQL("DROP TABLE IF EXISTS $deviceInfo;")
        //删除视频记录表
        db.execSQL("DROP TABLE IF EXISTS $recordInfo;")
        //删除海报信息表
        db.execSQL("DROP TABLE IF EXISTS $posterInfo;")
        //删除视频资源信息表
        db.execSQL("DROP TABLE IF EXISTS $videoInfo;")
        //删除音频资源信息表
        db.execSQL("DROP TABLE IF EXISTS $audioInfo;")
        //删除图片资源信息表
        db.execSQL("DROP TABLE IF EXISTS $pictureInfo;")
        //删除文档资源信息表
        db.execSQL("DROP TABLE IF EXISTS $documentInfo;")
        //删除应用资源信息表
        db.execSQL("DROP TABLE IF EXISTS $apkInfo;")
        //删除音乐海报信息
        db.execSQL("DROP TABLE IF EXISTS $musicInfo;")
        //重新建表
        onCreate(db)
    }

    override fun onDowngrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        LogUtils.e("onDowngrade")
        //删除设备信息表
        db.execSQL("DROP TABLE IF EXISTS $deviceInfo;")
        //删除视频记录表
        db.execSQL("DROP TABLE IF EXISTS $recordInfo;")
        //删除海报信息表
        db.execSQL("DROP TABLE IF EXISTS $posterInfo;")
        //删除视频资源信息表
        db.execSQL("DROP TABLE IF EXISTS $videoInfo;")
        //删除音频资源信息表
        db.execSQL("DROP TABLE IF EXISTS $audioInfo;")
        //删除图片资源信息表
        db.execSQL("DROP TABLE IF EXISTS $pictureInfo;")
        //删除文档资源信息表
        db.execSQL("DROP TABLE IF EXISTS $documentInfo;")
        //删除应用资源信息表
        db.execSQL("DROP TABLE IF EXISTS $apkInfo;")
        //删除音乐海报信息
        db.execSQL("DROP TABLE IF EXISTS $musicInfo;")
        //重新建表
        onCreate(db)
    }
}