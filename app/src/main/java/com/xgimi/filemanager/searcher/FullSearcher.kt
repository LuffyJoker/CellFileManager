package com.xgimi.filemanager.searcher

import android.content.ContentValues
import com.blankj.utilcode.util.StringUtils
import com.xgimi.filemanager.bean.BaseData
import com.xgimi.filemanager.constants.Constants
import com.xgimi.filemanager.contentprovider.BatchOperator
import com.xgimi.filemanager.contentprovider.ContentData
import com.xgimi.filemanager.helper.ResourceHelper
import com.xgimi.filemanager.utils.FileCategoryUtil
import java.io.File

/**
 *    author : joker.peng
 *    e-mail : joker.peng@xgimi.com
 *    date   : 2020/8/19 10:39
 *    desc   :
 */
class FullSearcher : Searcher {
    /**
     * 数据库批量操作类
     */
    private var mBatchOperator: BatchOperator? = null
    private var mResourceHelper: ResourceHelper? = null
    private var hasRepeat = false

    constructor(
        rootPath: String?,
        scanPath: String?,
        batchOperator: BatchOperator?
    ) : this(rootPath, scanPath, batchOperator, null, false)


    constructor(
        rootPath: String?,
        scanPath: String?,
        batchOperator: BatchOperator?,
        resourceHelper: ResourceHelper?,
        hasRepeat: Boolean
    ) : super(rootPath, scanPath) {
        mResourceHelper = resourceHelper
        this.hasRepeat = hasRepeat
        mBatchOperator = batchOperator
    }

    /**
     * 处理文件
     *
     * @param file 待处理文件
     */
    override fun handFile(file: File?) {
        if (file?.isFile!!) {
            saveFile(BaseData(mRootPath, file))
        } else {
            scanFilesByRecursion(file.path)
        }
    }

    /**
     * 完成扫描
     */
    override fun completedSearch() {
        try {
            mBatchOperator!!.flushAllPriority()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * 保存文件到数据库
     *
     * @param baseData
     */
    private fun saveFile(baseData: BaseData) {
        val file = File(baseData.path)
        if (!file.exists()) {
            return
        }
        val extension: String? = FileCategoryUtil.getExtensionName(baseData.path!!)
        if (StringUtils.isEmpty(extension)) {
            return
        }
        val values = ContentValues()
        values.put(ContentData.FILENAME, baseData.name)
        values.put(ContentData.FILEPATH, baseData.path)
        values.put(ContentData.ROOTPATH, baseData.rootPath)
        values.put(ContentData.ADDTIME, System.currentTimeMillis())
        try {
            if (FileCategoryUtil.isAudioFile(extension) && file.length() > Constants.DEFAULTAUDIOSIZE) {
                if (hasRepeat && mResourceHelper != null && ResourceHelper.getDataSize(
                        ContentData.CONTENT_URI_AUDIO,
                        ContentData.FILEPATH + " =?", arrayOf(baseData.path)
                    ) > 0
                ) {
                    return
                }
                mBatchOperator!!.insert(ContentData.CONTENT_URI_AUDIO, values)
                //                MediaUtil.scanMusicFile(FileManagerApplication.getInstance(),baseData.getPath());
            } else if (FileCategoryUtil.isPictureFile(extension) && file.length() > Constants.DEFAULTPICTURESIZE) {
                if (hasRepeat && mResourceHelper != null && ResourceHelper.getDataSize(
                        ContentData.CONTENT_URI_PICTURE,
                        ContentData.FILEPATH + " =?", arrayOf(baseData.path)
                    ) > 0
                ) {
                    return
                }
                mBatchOperator!!.insert(ContentData.CONTENT_URI_PICTURE, values)
            } else if (FileCategoryUtil.isVideoFile(extension) && file.length() > Constants.DEFAULTVIDEOSIZE) {
                if (hasRepeat && mResourceHelper != null && ResourceHelper.getDataSize(
                        ContentData.CONTENT_URI_VIDEO,
                        ContentData.FILEPATH + " =?", arrayOf(baseData.path)
                    ) > 0
                ) {
                    return
                }
                mBatchOperator!!.insert(ContentData.CONTENT_URI_VIDEO, values)
                //                ImgLoadUtils.loaderThubnail(baseData. (),null);
            } else if (FileCategoryUtil.isApkFile(extension)) {
                if (hasRepeat && mResourceHelper != null && ResourceHelper.getDataSize(
                        ContentData.CONTENT_URI_APK,
                        ContentData.FILEPATH + " =?", arrayOf(baseData.path)
                    ) > 0
                ) {
                    return
                }
                mBatchOperator!!.insert(ContentData.CONTENT_URI_APK, values)
            } else if (FileCategoryUtil.isOfficialFile(extension)) {
                if (hasRepeat && mResourceHelper != null && ResourceHelper.getDataSize(
                        ContentData.CONTENT_URI_DOCUMENT,
                        ContentData.FILEPATH + " =?",
                        arrayOf(baseData.path)
                    ) > 0
                ) {
                    return
                }
                mBatchOperator!!.insert(ContentData.CONTENT_URI_DOCUMENT, values)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
