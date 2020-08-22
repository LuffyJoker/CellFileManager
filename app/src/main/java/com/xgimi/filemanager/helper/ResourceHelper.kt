package com.xgimi.filemanager.helper

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.text.TextUtils
import android.util.Log
import com.blankj.utilcode.util.FileUtils
import com.blankj.utilcode.util.StringUtils
import com.blankj.utilcode.util.Utils
import com.xgimi.filemanager.bean.BaseData
import com.xgimi.filemanager.bean.CatalogInfo
import com.xgimi.filemanager.bean.CatalogInfoList
import com.xgimi.filemanager.bean.DeviceInfo
import com.xgimi.filemanager.constants.Constants
import com.xgimi.filemanager.contentprovider.ContentData
import com.xgimi.filemanager.enums.FileCategory
import com.xgimi.samba.SmbDevice
import java.io.File
import java.util.*

/**
 *    author : joker.peng
 *    e-mail : joker.peng@xgimi.com
 *    date   : 2020/8/10 14:20
 *    desc   : 获取音乐、视频信息，缩略图
 */
object ResourceHelper {
    /**
     * 搜索限制大小
     */
    private const val DEFAULT_SEARCH_FILE_COUNT = 100
    /**
     * 限制播放记录大小
     */
    private const val DEFAULT_GET_RECODE_COUNT = 5

    private var mInstance: ResourceHelper? = null

    private var mContext: Context = Utils.getApp()


    /************************************************设备相关**************************************************/

    /**
     * 清除设备状态
     */
    fun clearDeviceState() {
        if (mContext == null) {
            return
        }
        val values = ContentValues()
        values.put(DeviceInfo.HIDE, 0)
        mContext!!.contentResolver
            .update(ContentData.CONTENT_URI_DEVICE, values, "_id >= 0 ", null)
    }

    /**
     * 保存 samba设备
     *
     * @param deviceInfo 设备信息
     */
    fun saveSambaDeviceInfo(deviceInfo: SmbDevice) {
        val info = DeviceInfo(deviceInfo)
        saveDeviceInfo(info)
    }

    /**
     * 根据ip获取以前挂载过的samba info
     *
     * @param ip
     * @return
     */
    fun getSambaDeviceInfo(ip: String): DeviceInfo? {
        return queryDeviceInfo(DeviceInfo.IP, ip)
    }

    /**
     * 查询设备
     *
     * @param selection
     * @param selectionArg
     * @return
     */
    private fun queryDeviceInfo(
        selection: String,
        selectionArg: String
    ): DeviceInfo? {
        var deviceInfo: DeviceInfo? = null
        var cursor: Cursor? = null
        try {
            cursor = mContext!!.contentResolver
                .query(
                    ContentData.CONTENT_URI_DEVICE,
                    null,
                    "$selection=?",
                    arrayOf(selectionArg),
                    null
                )
            if (cursor != null && cursor.moveToFirst()) {
                deviceInfo = getDeviceInfoByCursor(cursor)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            deviceInfo = null
        } finally {
            try {
                cursor?.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return deviceInfo
    }

    /**
     * 从Cursor解析设备信息
     *
     * @param cursor 游标
     * @return 设备信息
     */
    private fun getDeviceInfoByCursor(cursor: Cursor): DeviceInfo? {
        val deviceInfo = DeviceInfo()
        deviceInfo.deviceType = cursor.getInt(cursor.getColumnIndex(DeviceInfo.DEVICETYPE))
        deviceInfo.deviceName = cursor.getString(cursor.getColumnIndex(DeviceInfo.DEVICENAME))
        deviceInfo.hostName = cursor.getString(cursor.getColumnIndex(DeviceInfo.HOSTNAME))
        deviceInfo.ip = cursor.getString(cursor.getColumnIndex(DeviceInfo.IP))
        deviceInfo.rootPath = cursor.getString(cursor.getColumnIndex(DeviceInfo.ROOTPATH))
        deviceInfo.userName = cursor.getString(cursor.getColumnIndex(DeviceInfo.USERNAME))
        deviceInfo.passward = cursor.getString(cursor.getColumnIndex(DeviceInfo.PASSWARD))
        deviceInfo.hide = cursor.getInt(cursor.getColumnIndex(DeviceInfo.HIDE))
        return deviceInfo
    }

    /**
     * 保存设备
     *
     * @param deviceInfo 设备信息
     */
    fun saveDeviceInfo(deviceInfo: DeviceInfo) {
        val values = ContentValues()
        values.put(DeviceInfo.DEVICENAME, deviceInfo.deviceName)
        values.put(DeviceInfo.DEVICETYPE, deviceInfo.deviceType)
        values.put(DeviceInfo.HOSTNAME, deviceInfo.hostName)
        values.put(DeviceInfo.ROOTPATH, deviceInfo.rootPath)
        values.put(DeviceInfo.IP, deviceInfo.ip)
        values.put(DeviceInfo.USERNAME, deviceInfo.userName)
        values.put(DeviceInfo.PASSWARD, deviceInfo.passward)
        Log.e(TAG, "saveDeviceInfo")
        var cursor: Cursor? = null
        try {
            cursor = mContext!!.contentResolver.query(
                ContentData.CONTENT_URI_DEVICE, null,
                DeviceInfo.ROOTPATH + " =? ", arrayOf(deviceInfo.rootPath), null
            )
            if (cursor != null && cursor.moveToFirst()) {
                mContext!!.contentResolver
                    .update(
                        ContentData.CONTENT_URI_DEVICE,
                        values,
                        DeviceInfo.ROOTPATH + " =? ",
                        arrayOf(deviceInfo.rootPath)
                    )
            } else {
                mContext!!.contentResolver.insert(ContentData.CONTENT_URI_DEVICE, values)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            try {
                cursor?.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    /**
     * 通过 ip 删除设备
     *
     * @param ip
     */
    fun delDeviceInfoByIp(ip: String) {
        mContext!!.contentResolver.delete(
            ContentData.CONTENT_URI_DEVICE,
            DeviceInfo.IP + "=?",
            arrayOf(ip)
        )
    }

    /**
     * 通过路径删除设备
     *
     * @param rootPath 路径
     */
    fun delDeviceInfoByPath(rootPath: String) {
        mContext!!.contentResolver
            .delete(
                ContentData.CONTENT_URI_DEVICE,
                DeviceInfo.ROOTPATH + " =? ",
                arrayOf(rootPath)
            )
    }

    /**
     * 获取所有 samba 设备信息
     *
     * @return 设备信息列表
     */
    fun getAllSambaDeviceInfo(): ArrayList<DeviceInfo?>? {
        var deviceList: ArrayList<DeviceInfo?>? = null
        var cursor: Cursor? = null
        try {
            cursor = mContext!!.contentResolver
                .query(
                    ContentData.CONTENT_URI_DEVICE,
                    null,
                    DeviceInfo.DEVICETYPE + "=?",
                    arrayOf("" + DeviceInfo.DeviceCategory.Samba.ordinal),
                    null
                )
            if (cursor != null && cursor.moveToFirst()) {
                deviceList = ArrayList()
                do {
                    deviceList.add(getDeviceInfoByCursor(cursor))
                } while (cursor.moveToNext())
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            try {
                cursor?.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return deviceList
    }

    /************************************************视频相关**************************************************/
    /************************************************视频相关 */
    /**
     * 访问媒体信息相关的数据
     * 这里提供视频、音乐、文档、图片、apk所有数据的访问
     */
    private const val TAG = "ResourceHelper"

    fun getRecordList(): List<BaseData>? {
        val recordList: MutableList<BaseData> = mutableListOf()
        var cursor: Cursor? = null
        try {
            cursor = mContext!!.contentResolver
                .query(
                    ContentData.CONTENT_URI_RECORD,
                    null,
                    null,
                    null,
                    ContentData.LASTPLAYTIME.toString() + " DESC"
                )
            var i = 0
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    val filepath =
                        cursor.getString(cursor.getColumnIndex(ContentData.FILEPATH))
                    if (!checkVideoExist(filepath)) {
                        continue
                    }
                    val isHide = cursor.getInt(cursor.getColumnIndex(ContentData.ISHIDE))
                    if (isHide != 1) {
                        recordList.add(getRecordInfoByCursor(cursor))
                    }
                    i++
                    if (i >= DEFAULT_GET_RECODE_COUNT) {
                        break
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            try {
                cursor?.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return recordList
    }

    private fun checkVideoExist(path: String): Boolean {
        var exist = true
        val file = File(path)
        if (!file.exists()) {
            mContext!!.contentResolver
                .delete(
                    ContentData.CONTENT_URI_RECORD,
                    ContentData.FILEPATH.toString() + " = ? ",
                    arrayOf(path)
                )
            exist = false
        }
        if (isSamba(file.path)) {
            exist = false
        }
        var cursor: Cursor? = null
        try {
            cursor = mContext!!.contentResolver
                .query(
                    ContentData.CONTENT_URI_VIDEO,
                    null,
                    ContentData.FILEPATH.toString() + " = ? ",
                    arrayOf(path),
                    null
                )
            exist = cursor != null && cursor.count > 0
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            try {
                cursor?.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return exist
    }

    private fun isSamba(rootPath: String): Boolean {
        return !TextUtils.isDigitsOnly(rootPath) && rootPath.startsWith("/mnt/samba")
    }

    /**
     * 根据Cursor获取播放记录BaseData
     *
     * @param cursor
     * @return
     */
    private fun getRecordInfoByCursor(cursor: Cursor): BaseData {
        val record = BaseData()
        record.category = FileCategory.Video.ordinal
        record.doubanId = cursor.getInt(cursor.getColumnIndex(ContentData.DOUBANID))
        record.doubanUrl = cursor.getString(cursor.getColumnIndex(ContentData.DOUBANURL))
        record.posterUrl = cursor.getString(cursor.getColumnIndex(ContentData.POSTERURL))
        record.name = cursor.getString(cursor.getColumnIndex(ContentData.FILENAME))
        record.path = cursor.getString(cursor.getColumnIndex(ContentData.FILEPATH))
        record.hisType = 1
        record.lastPlayTime =
            cursor.getInt(cursor.getColumnIndex(ContentData.LASTPLAYTIME)).toLong()
        record.lastPlayPosition =
            cursor.getInt(cursor.getColumnIndex(ContentData.LASTPLAYPOSITIION)).toLong()
        record.duration = cursor.getInt(cursor.getColumnIndex(ContentData.DURATION)).toLong()
        return record
    }

    /**
     * 查询历史记录
     *
     * @param filePath
     * @return
     */
    fun checkRecordExist(filePath: String): BaseData? {
        val recordCode: Int
        recordCode = if (filePath.startsWith("/mnt/samba")) {
            val smbPath = filePath.substring("/mnt/samba/".length)
            smbPath.substring(smbPath.indexOf("/")).hashCode()
        } else {
            filePath.hashCode()
        }
        var cursor: Cursor? = null
        var record: BaseData? = null
        try {
            cursor = mContext!!.contentResolver
                .query(
                    ContentData.CONTENT_URI_RECORD,
                    null,
                    ContentData.RECORDKEY.toString() + " =?",
                    arrayOf("" + recordCode),
                    null
                )
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    record = getRecordInfoByCursor(cursor)
                }
            } catch (e: Exception) {
                record = null
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            cursor?.close()
        }
        return record
    }

    /**
     * 清空历史记录
     */
    fun clearAllHistory() {
        mContext!!.contentResolver.delete(
            ContentData.CONTENT_URI_RECORD,
            ContentData.ID.toString() + "> ?",
            arrayOf("0")
        )
        mContext!!.contentResolver.notifyChange(ContentData.CONTENT_URI_RECORD, null)
    }
    /*********************************文档相关**************************************************/
    /*********************************文档相关 */
    /**
     * 查询文档并且以目录的方式返回
     *
     * @param mountPaths 挂载点
     * @param category   类型
     * @param sort       排序方式
     * @return
     */
    fun queryCategoryFileCatalog(
        mountPaths: List<String>?,
        category: Int,
        sort: Int
    ): CatalogInfoList? {
        val pictureList: List<BaseData>? = querySource(category, sort)
        val catalogInfoList = CatalogInfoList()
        if (pictureList != null && pictureList.isNotEmpty()) {
            for (data in pictureList) { //String parentName = FileUtils.getParentDirName(baseData.getPath());
                val index: Int = data.path!!.lastIndexOf('/')
                val parentName: String = data.path!!.substring(0, index)
                val catalogInfo: Any? = catalogInfoList[parentName]
                if (catalogInfo is CatalogInfo) {
                    catalogInfo.add(data)
                } else {
                    if (mountPaths != null && mountPaths.contains(data.parentPath)) {
                        catalogInfoList.add(data)
                    } else {
                        val newCatalogInfo = CatalogInfo(parentName)
                        newCatalogInfo.category = category
                        newCatalogInfo.add(data)
                        catalogInfoList.add(newCatalogInfo)
                    }
                }
            }
        }
        //排除掉路径名
        for (info in catalogInfoList) {
            if (info is CatalogInfo) {
                val abPath: String = info.name!!
                val index = abPath.lastIndexOf('/')
                info.name = abPath.substring(index + 1)
            }
        }
        //删除无用文件夹
        val tempCatalogInfoList = CatalogInfoList()
        for (i in 0 until catalogInfoList.size) {
            if (catalogInfoList[i] is CatalogInfo) {
                val catalogInfo: CatalogInfo = catalogInfoList[i] as CatalogInfo
                if (catalogInfo.datas.size === 1) {
                    tempCatalogInfoList.add(catalogInfo.datas[0])
                } else {
                    tempCatalogInfoList.add(catalogInfo)
                }
            } else {
                tempCatalogInfoList.add(catalogInfoList[i])
            }
        }
        if (tempCatalogInfoList != null && tempCatalogInfoList.size > 0) {
            Collections.sort(tempCatalogInfoList, Comparators.getForFile(sort))
        }
        return tempCatalogInfoList
    }
    /*************************************全局相关*************************************************/
    /*************************************全局相关 */
    /**
     * 检查数据是否发生变化
     *
     * @param uri
     * @param oldSize
     * @return
     */
    fun checkDataChange(uri: Uri?, oldSize: Int): Boolean {
        var changed = false
        var cursor: Cursor? = null
        try {
            cursor = mContext!!.contentResolver.query(uri!!, null, null, null, null)
            if (cursor != null) {
                val currentSize = cursor.count
                if (oldSize != currentSize) {
                    changed = true
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            try {
                cursor?.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return changed
    }

    /**
     * 获取data size
     *
     * @param uri
     * @return
     */
    fun getDataSize(uri: Uri?): Int {
        return getDataSize(uri, null, null)
    }

    fun getDataSize(
        type: Int,
        selection: String?,
        selectionArgs: Array<String?>?
    ): Int {
        return getDataSize(getUriByCategory(type), selection, selectionArgs)
    }

    fun getDataSize(
        uri: Uri?,
        selection: String?,
        selectionArgs: Array<String?>?
    ): Int {
        var currentSize = 0
        var cursor: Cursor? = null
        try {
            cursor = mContext!!.contentResolver.query(uri!!, null, selection, selectionArgs, null)
            if (cursor != null) {
                currentSize = cursor.count
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            try {
                cursor?.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return currentSize
    }

    /**
     * 获取视频、音乐、文档、应用 等资源
     *
     * @param type 是 contentProvider 中数据类型
     * @return 返回相应的数据
     */
    fun querySource(type: Int, sortMode: Int): List<BaseData> {
        val dataList = mutableListOf<BaseData>()
        val uri = getUriByCategory(type)
        uri?.apply {
            var cursor: Cursor? = null
            try {
                cursor = mContext.contentResolver.query(uri, null, null, null, null)
                cursor?.apply {
                    while (this.moveToNext()){
                        val baseData = BaseData()
                        baseData.name = cursor.getString(cursor.getColumnIndex(ContentData.FILENAME))
                        baseData.path = cursor.getString(cursor.getColumnIndex(ContentData.FILEPATH))
                        baseData.category = type
                        dataList.add(baseData)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                cursor?.close()
            }
        }
        if(dataList.size > 0){
            Collections.sort(
                dataList,
                if (type != FileCategory.Document.ordinal) {
                    Comparators.getForFile(sortMode)
                } else {
                    Comparators.getForDoc(sortMode)
                }
            )
        }
        return dataList
    }

    /**
     * 根据 FileCategory 分类信息获取数据库 Uri
     *
     * @param category
     * @return
     */
    private fun getUriByCategory(category: Int): Uri? {
        var uri: Uri? = null
        when (category) {
            FileCategory.Video.ordinal -> {
                uri = ContentData.CONTENT_URI_VIDEO
            }
            FileCategory.Music.ordinal -> {
                uri = ContentData.CONTENT_URI_AUDIO
            }
            FileCategory.Picture.ordinal -> {
                uri = ContentData.CONTENT_URI_PICTURE
            }
            FileCategory.Document.ordinal -> {
                uri = ContentData.CONTENT_URI_DOCUMENT
            }
            FileCategory.Apk.ordinal -> {
                uri = ContentData.CONTENT_URI_APK
            }
        }
        return uri
    }

    private fun saveOperationDataBase(baseData: BaseData) {
        if (mContext == null) {
            return
        }
        val category: Int = baseData.category
        val uri = getUriByCategory(category) ?: return
        val values = ContentValues()
        values.put(ContentData.FILENAME, baseData.name)
        values.put(ContentData.FILEPATH, baseData.path)
        values.put(ContentData.ROOTPATH, baseData.rootPath)
        values.put(ContentData.ADDTIME, System.currentTimeMillis())
        if (uri === ContentData.CONTENT_URI_VIDEO) {
            values.put(ContentData.LOADSTAT, 0)
        }
        mContext!!.contentResolver.insert(uri, values)
        mContext!!.contentResolver.notifyChange(uri, null)
    }

    /**
     * 删除文件信息
     *
     * @param path
     */
    fun deleteFile(path: String) {
        mContext!!.contentResolver
            .delete(
                ContentData.CONTENT_URI_RECORD,
                ContentData.FILEPATH.toString() + " =? ",
                arrayOf(path)
            )
        mContext!!.contentResolver
            .delete(
                ContentData.CONTENT_URI_VIDEO,
                ContentData.FILEPATH.toString() + " =? ",
                arrayOf(path)
            )
        mContext!!.contentResolver
            .delete(
                ContentData.CONTENT_URI_AUDIO,
                ContentData.FILEPATH.toString() + " =? ",
                arrayOf(path)
            )
        mContext!!.contentResolver
            .delete(
                ContentData.CONTENT_URI_DOCUMENT,
                ContentData.FILEPATH.toString() + " =? ",
                arrayOf(path)
            )
        mContext!!.contentResolver
            .delete(
                ContentData.CONTENT_URI_PICTURE,
                ContentData.FILEPATH.toString() + " =? ",
                arrayOf(path)
            )
        mContext!!.contentResolver
            .delete(
                ContentData.CONTENT_URI_APK,
                ContentData.FILEPATH.toString() + " =? ",
                arrayOf(path)
            )
        //        if (FileCategoryUtils.isAudioFile(FileCategoryUtils.getExtensionName(path))) {
//            MediaUtil.removeMusic(mContext, path);
//        }
    }

    fun deleteDir(path: String) { //        mContext.getContentResolver().delete(ContentData.CONTENT_URI_RECORD, ContentData.FILEPATH + " LIKE
// '" + path + "%'", null);
        mContext!!.contentResolver
            .delete(
                ContentData.CONTENT_URI_VIDEO,
                ContentData.FILEPATH.toString() + " LIKE '" + path + "%'",
                null
            )
        mContext!!.contentResolver
            .delete(
                ContentData.CONTENT_URI_AUDIO,
                ContentData.FILEPATH.toString() + " LIKE '" + path + "%'",
                null
            )
        mContext!!.contentResolver
            .delete(
                ContentData.CONTENT_URI_DOCUMENT,
                ContentData.FILEPATH.toString() + " LIKE '" + path + "%'",
                null
            )
        mContext!!.contentResolver
            .delete(
                ContentData.CONTENT_URI_PICTURE,
                ContentData.FILEPATH.toString() + " LIKE '" + path + "%'",
                null
            )
        mContext!!.contentResolver
            .delete(
                ContentData.CONTENT_URI_APK,
                ContentData.FILEPATH.toString() + " LIKE '" + path + "%'",
                null
            )
    }

    /**
     * 清空数据库
     */
    fun clearDataBase() {
        mContext!!.contentResolver
            .delete(
                ContentData.CONTENT_URI_DEVICE,
                DeviceInfo.DEVICETYPE + " =? ",
                arrayOf(
                    DeviceInfo.DeviceCategory.Usb.ordinal.toString() + ""
                )
            )
        mContext!!.contentResolver
            .delete(
                ContentData.CONTENT_URI_DEVICE,
                DeviceInfo.DEVICETYPE + " =? ",
                arrayOf(
                    DeviceInfo.DeviceCategory.Baiduyun.ordinal.toString() + ""
                )
            )
        //        mContext.getContentResolver().delete(ContentData.CONTENT_URI_RECORD, ContentData.ID + " >= ?", new
// String[]{"0"});
        mContext!!.contentResolver
            .delete(
                ContentData.CONTENT_URI_VIDEO,
                ContentData.ID.toString() + " >= ?",
                arrayOf("0")
            )
        mContext!!.contentResolver
            .delete(
                ContentData.CONTENT_URI_AUDIO,
                ContentData.ID.toString() + " >= ?",
                arrayOf("0")
            )
        mContext!!.contentResolver
            .delete(
                ContentData.CONTENT_URI_DOCUMENT,
                ContentData.ID.toString() + " >= ?",
                arrayOf("0")
            )
        mContext!!.contentResolver
            .delete(
                ContentData.CONTENT_URI_PICTURE,
                ContentData.ID.toString() + " >= ?",
                arrayOf("0")
            )
        mContext!!.contentResolver.delete(
            ContentData.CONTENT_URI_APK,
            ContentData.ID.toString() + " >= ?",
            arrayOf("0")
        )
    }

    /**
     * 根据类型搜索文件
     *
     * @return
     */
    fun searchFileSource(
        type: Int,
        keyWord: String
    ): List<BaseData>? {
        val dataList: MutableList<BaseData> = ArrayList<BaseData>()
        val uri = getUriByCategory(type) ?: return dataList
        var cursor: Cursor? = null
        try {
            cursor = mContext!!.contentResolver.query(uri, null, null, null, null)
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    val name =
                        cursor.getString(cursor.getColumnIndex(ContentData.FILENAME))
                    val path =
                        cursor.getString(cursor.getColumnIndex(ContentData.FILEPATH))
                    if (name.toLowerCase().contains(keyWord.toLowerCase())) {
                        val baseData = BaseData()
                        baseData.name = name
                        baseData.path = path
                        baseData.category = type
                        dataList.add(baseData)
                    }
                    if (dataList.size > DEFAULT_SEARCH_FILE_COUNT) {
                        break
                    }
                } while (cursor != null && cursor.moveToNext())
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            try {
                cursor?.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return dataList
    }

    fun insertFile(rootPath: String?, path: String?) {
        if (!StringUtils.isEmpty(path)) {
            val file = File(path)
            if (file.exists()) {
                val baseData = BaseData(rootPath, file)
                val length = file.length()
                if (baseData.category === FileCategory.Music.ordinal && length < Constants.DEFAULTAUDIOSIZE) {
                    return
                } else if (baseData.category === FileCategory.Picture.ordinal && length < Constants.DEFAULTPICTURESIZE) {
                    return
                } else if (baseData.category === FileCategory.Video.ordinal && length < Constants.DEFAULTVIDEOSIZE) {
                    return
                }
                saveOperationDataBase(baseData)
                //                if (FileCategoryUtils.isAudioFile(FileCategoryUtils.getExtensionName(path)))
//                    MediaUtil.scanMusicFile(mContext, path);
            }
        }
    }

    /**
     * 查询所有设备路径
     *
     * @return
     */
    fun queryUsbList(): List<String>? {
        val usbList: MutableList<String> =
            ArrayList()
        if (mContext == null) {
            return usbList
        }
        var cursor: Cursor? = null
        try {
            cursor = mContext!!.contentResolver
                .query(
                    ContentData.CONTENT_URI_DEVICE,
                    null,
                    DeviceInfo.DEVICETYPE + " = 0",
                    null,
                    null
                )
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    val rootPath = cursor.getString(
                        cursor.getColumnIndex(
                            DeviceInfo.ROOTPATH
                        )
                    )
                    usbList.add(rootPath)
                } while (cursor.moveToNext())
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            cursor?.close()
        }
        return usbList
    }

    /**
     * 判断数据库中是否有相应设备
     *
     * @param mountPath
     * @return
     */
    fun checkDeviceExist(mountPath: String): Boolean {
        var isExist = false
        var cursor: Cursor? = null
        try {
            cursor = mContext!!.contentResolver
                .query(
                    ContentData.CONTENT_URI_DEVICE,
                    null,
                    DeviceInfo.ROOTPATH + " =?",
                    arrayOf(mountPath),
                    null
                )
            if (cursor != null && cursor.moveToFirst()) {
                isExist = true
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            cursor?.close()
        }
        return isExist
    }
}