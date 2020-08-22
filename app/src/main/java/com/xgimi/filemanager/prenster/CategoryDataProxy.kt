package com.xgimi.filemanager.prenster

import android.app.Activity
import android.os.Handler
import com.xgimi.filemanager.R
import com.xgimi.filemanager.bean.BaseData
import com.xgimi.filemanager.bean.CatalogInfo
import com.xgimi.filemanager.bean.CatalogInfoList
import com.xgimi.filemanager.config.DisplayMode
import com.xgimi.filemanager.config.LayoutType
import com.xgimi.filemanager.config.OperationConfigure
import com.xgimi.filemanager.contentprovider.ContentData
import com.xgimi.filemanager.contentprovider.MediaContentObserver
import com.xgimi.filemanager.enums.FileCategory
import com.xgimi.filemanager.helper.MountHelper
import com.xgimi.filemanager.helper.ResourceHelper
import rx.Observable
import rx.Subscription
import rx.android.schedulers.AndroidSchedulers
import rx.functions.Func1
import rx.schedulers.Schedulers
import java.util.*
import java.util.concurrent.TimeUnit

/**
 *    author : joker.peng
 *    e-mail : joker.peng@xgimi.com
 *    date   : 2020/8/17 11:21
 *    desc   :
 */
class CategoryDataProxy : BasePresenterImp {
    private val DELAY_LOAD_TIME = 500
    private lateinit var mediaContentObserver: MediaContentObserver
    private var dataSize = -1
    private var extraSize = -1
    private var mCategory = 0
    private var mActivity: Activity? = null
    private var forceRefreshAlbe = false

    interface CategoryDataloadListener {
        fun onSuccess(data: List<Any>?)
        fun onError(throwable: Throwable?)
    }

    constructor(activity: Activity?, category: Int) {
        mActivity = activity
        mCategory = category
    }

    fun register(handler: Handler?, listener: MediaContentObserver.ContentObserverListener?) {
        mediaContentObserver = MediaContentObserver(handler, listener)
        when (mCategory) {
            FileCategory.Video.ordinal -> {
                mActivity!!.contentResolver
                    .registerContentObserver(
                        ContentData.CONTENT_URI_VIDEO,
                        false,
                        mediaContentObserver
                    )
                mActivity!!.contentResolver
                    .registerContentObserver(
                        ContentData.CONTENT_URI_RECORD,
                        false,
                        mediaContentObserver
                    )
            }
            FileCategory.Music.ordinal -> {
                mActivity!!.contentResolver
                    .registerContentObserver(
                        ContentData.CONTENT_URI_AUDIO,
                        false,
                        mediaContentObserver
                    )
            }
            FileCategory.Picture.ordinal -> {
                mActivity!!.contentResolver
                    .registerContentObserver(
                        ContentData.CONTENT_URI_PICTURE,
                        false,
                        mediaContentObserver
                    )
            }
            FileCategory.Document.ordinal -> {
                mActivity!!.contentResolver
                    .registerContentObserver(
                        ContentData.CONTENT_URI_DOCUMENT,
                        false,
                        mediaContentObserver
                    )
            }
        }
    }

    private fun unRegister() {
        mediaContentObserver?.apply {
            mActivity?.contentResolver?.unregisterContentObserver(this)
        }
    }

    private var mSubscription: Subscription? = null

    fun loadData(listener: CategoryDataloadListener?) {
        loadData(listener, false, false)
    }

    fun loadData(listener: CategoryDataloadListener?, isForceRefresh: Boolean, isDelay: Boolean) {
        if (mSubscription == null || mSubscription!!.isUnsubscribed) {
            mSubscription = Observable.timer(
                if (isDelay) DELAY_LOAD_TIME.toLong() else 0.toLong(),
                TimeUnit.MILLISECONDS
            ).filter {
                val filter = isForceRefresh || forceRefreshAlbe || checkDataChange()
                forceRefreshAlbe = false
                filter
            }.map {
                loadData()
            }.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    { datas -> listener?.onSuccess(datas) },
                    { throwable -> listener?.onError(throwable) }
                ) { listener?.onError(null) }
            addSubscribe(mSubscription)
        }
    }

    /**
     * 判断所有影片数量与观看记录数量是否发生变化
     *
     *
     *
     *
     * 为规避资源管理器a（或ab），然后在播放器中播a或b，清理记录，造成观看记录数量1 -> 1 无变化，在1 -> 1 时刷新一次
     * extraSize == 1 && !mResourceHelper.checkDataChange(ContentData.CONTENT_URI_RECORD, extraSize）
     */
    private fun checkDataChange(): Boolean {
        if (mCategory == FileCategory.Video.ordinal) {
            return if (extraSize == 1 && !ResourceHelper.checkDataChange(
                    ContentData.CONTENT_URI_RECORD,
                    extraSize
                )
            ) {
                true
            } else ResourceHelper.checkDataChange(ContentData.CONTENT_URI_VIDEO, dataSize) ||
                    ResourceHelper.checkDataChange(ContentData.CONTENT_URI_RECORD, extraSize)
        }
        if (mCategory == FileCategory.Music.ordinal) {
            return ResourceHelper.checkDataChange(ContentData.CONTENT_URI_AUDIO, dataSize)
        }
        if (mCategory == FileCategory.Picture.ordinal) {
            return ResourceHelper.checkDataChange(ContentData.CONTENT_URI_PICTURE, dataSize)
        }
        return if (mCategory == FileCategory.Document.ordinal) {
            ResourceHelper.checkDataChange(ContentData.CONTENT_URI_DOCUMENT, dataSize)
        } else false
    }

    private var hasExtra = false

    private fun loadData(): List<Any>? {
        val mountPaths: List<String>? = MountHelper.getMountPathList()
        return if (mCategory == FileCategory.Video.ordinal) {
            if (OperationConfigure.getDisplayMode() === DisplayMode.FolderMode.ordinal) { //加载目录形式的数据
                val newRecordData: List<BaseData>? = ResourceHelper.getRecordList()
                hasExtra = newRecordData != null && newRecordData.isNotEmpty()
                val datas: CatalogInfoList? = ResourceHelper.queryCategoryFileCatalog(
                    mountPaths,
                    mCategory,
                    OperationConfigure.getSortOrder()
                )
                dataSize = datas?.getAllChildSize()!!
                extraSize = ResourceHelper.getDataSize(ContentData.CONTENT_URI_RECORD)
                initCatalogGridVideoData(newRecordData, datas)
            } else {
                val newRecordData: List<BaseData>? = ResourceHelper.getRecordList()
                hasExtra = newRecordData != null && newRecordData.isNotEmpty()
                val newAllData: List<BaseData>? = ResourceHelper.querySource(
                    FileCategory.Video.ordinal,
                    OperationConfigure.getSortOrder()
                )
                dataSize = newAllData?.size!!
                extraSize = ResourceHelper.getDataSize(ContentData.CONTENT_URI_RECORD)
                initGridVideoData(newRecordData, newAllData)
            }
        } else {
            if (mCategory == FileCategory.Document.ordinal) {
                val newAllData: List<BaseData>? = ResourceHelper.querySource(
                    FileCategory.Document.ordinal,
                    OperationConfigure.getSortOrder()
                )
                dataSize = newAllData?.size!!
                newAllData
            } else {
                val datas: CatalogInfoList? = ResourceHelper.queryCategoryFileCatalog(
                    mountPaths, mCategory,
                    OperationConfigure.getSortOrder()
                )
                dataSize = datas?.getAllChildSize()!!
                datas
            }
        }
    }

    fun hasExtraData(): Boolean {
        return hasExtra
    }

    fun needDynamicRefresh(): Boolean {
        return forceRefreshAlbe
    }

    fun setForceRefreshAlbe(albe: Boolean) {
        forceRefreshAlbe = albe
    }

    fun getCategory(): Int {
        return mCategory
    }

    fun destroy() {
        unRegister()
        unSubscribe()
        mActivity = null
    }

    /**
     * type 0 第一个   1、在第一排有资源  2、在第一排无资源  3、在第二排
     *
     * @param hisData=
     * @param allData
     */
    private fun initGridVideoData(
        hisData: List<BaseData>?,
        allData: List<BaseData>?
    ): MutableList<BaseData>? {
        val mVideoResource = mutableListOf<BaseData>()
        if (hisData != null && hisData.isNotEmpty()) {
            hisData.forEach {
                var baseData: BaseData = it
                baseData.videoType = 0 // 历史记录
                mVideoResource.add(baseData)
            }
        }
        if (allData != null && allData.isNotEmpty()) {
            allData.forEach {
                var baseData: BaseData = it
                baseData.videoType = 2 // 所有影片
                mVideoResource.add(baseData)
            }
        }
        return mVideoResource
    }

    /**
     * type 0 第一个   1、在第一排有资源  2、在第一排无资源  3、在第二排
     *
     * @param hisData
     * @param catalogInfos
     */
    private fun initCatalogGridVideoData(
        hisData: List<BaseData>?,
        catalogInfos: CatalogInfoList?
    ): MutableList<Any> {
        val result = mutableListOf<Any>()
        if (hisData != null && hisData.isNotEmpty()) {
            for (i in 0 until 6) {
                var baseData: BaseData? = null
                if (i < hisData.size) {
                    baseData = hisData[i]
                    if (i == 0) {
                        baseData!!.type = 0
                        baseData.title = mActivity!!.getString(R.string.video_recent_history)
                    } else {
                        baseData!!.type = 1
                    }
                } else if (OperationConfigure.getLayoutType() !== LayoutType.LINEAR_LAYOUT.ordinal) {
                    baseData = BaseData(FileCategory.Video.ordinal)
                    baseData.type = 2
                }
                if (baseData != null) {
                    baseData.videoType = 0
                    result.add(baseData)
                }
            }
        }
        if (catalogInfos != null && catalogInfos.size > 0) {
            val size = catalogInfos.size
            for (i in 0 until size) {
                val catalogInfo = catalogInfos[i]
                var baseData: BaseData? = null
                if (catalogInfo is CatalogInfo) {
                    baseData = catalogInfo.datas[0]
                } else if (catalogInfo is BaseData) {
                    baseData = catalogInfo
                }
                if (baseData == null) {
                    continue
                }
                if (i == 0) {
                    baseData.type = 0
                    baseData.title = mActivity!!.getString(R.string.video_all_video)
                } else {
                    if (i < 6) {
                        baseData.type = 1
                    } else {
                        baseData.type = 3
                    }
                }
                baseData.videoType = 2
                result.add(catalogInfo)
            }
        }
        return result
    }
}
