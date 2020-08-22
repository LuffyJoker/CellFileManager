package com.xgimi.filemanager.searcher

import android.content.Context
import android.os.Handler
import com.xgimi.filemanager.bean.BaseData
import com.xgimi.filemanager.contentprovider.BatchOperator
import com.xgimi.filemanager.enums.FileCategory
import com.xgimi.filemanager.helper.Comparators
import com.xgimi.filemanager.helper.ResourceHelper
import java.util.*

/**
 *    author : joker.peng
 *    e-mail : joker.peng@xgimi.com
 *    date   : 2020/8/18 17:29
 *    desc   :
 */
class LocalFileSearcher : Runnable {

    companion object {
        /**
         * 开始扫描 Handler what
         */
        const val SearchLocalFileDone = 2000
        /**
         * 扫描完成 Handler what
         */
        const val SearchLocalFileStart = 2001
    }

    /**
     * 搜索方式
     */
    private var mSearchMode: SearchMode? = null
    /**
     * 排序方式
     */
    private var mSortMode = 0
    /**
     * 扫描根目录
     */
    private var mRootPath: String? = null
    /**
     * 当前扫描目录
     */
    private var mCurrentPath: String? = null
    /**
     * 搜索文件类型
     */
    private var mFileType = 0
    /**
     * 关键字
     */
    private var mKeyWord: String? = null
    /**
     * 数据库批量操作类
     */
    private var mBatchOperator: BatchOperator? = null
    /**
     * 资源管理类
     */
    private var mResourceHelper: ResourceHelper? = null
    /**
     * 搜索监听
     */
    private var mSearchListener: SearchListener? = null

    private var mHandler: Handler? = null
    /**
     * 搜索类
     */
    private var mSearcher: Searcher

    /**
     * 搜索类型
     * 层扫描、全盘扫描、关键字扫描
     */
    enum class SearchMode {
        Layer, FullScan, Keyword
    }

    private constructor(builder: Builder) {
        mSearchMode = builder.builderSearchMode
        mSortMode = builder.builderSortMode
        mRootPath = builder.builderRootPath
        mCurrentPath = builder.builderCurrentPath
        mFileType = builder.builderFileType
        mKeyWord = builder.builderKeyWord
        mBatchOperator = builder.buildBatchOperator
        mSearchListener = builder.buildSearchListener
        mHandler = builder.buildHandler
        mSearcher = getSearcher()
    }

    /**
     * 根据类型获取扫描类
     *
     * @return
     */
    private fun getSearcher(): Searcher {
        return when (mSearchMode) {
            SearchMode.FullScan -> FullSearcher(
                mRootPath,
                mCurrentPath,
                mBatchOperator,
                mResourceHelper,
                true
            )
            SearchMode.Keyword -> KeyWordSearcher(
                mRootPath,
                mCurrentPath,
                mFileType,
                mKeyWord,
                mResourceHelper
            )
            else -> LayerSearcher(mRootPath, mCurrentPath)
        }
    }

    /**
     * 获取扫描结果
     *
     * @return
     */
    fun getDataList(): List<BaseData> {
        return mSearcher.dataList
    }

    /**
     * 获取常用文件:视频、图片、音乐、文档、文件夹
     *
     * @return
     */
    fun getCommonFile(): List<BaseData>? {
        val dataList = getDataList()
        if (dataList == null || dataList.isEmpty()) {
            return null
        }
        val tempDataList: MutableList<BaseData> = ArrayList()
        tempDataList.addAll(dataList)
        val it = tempDataList.iterator()
        while (it.hasNext()) {
            val category = it.next().category
            if (category != FileCategory.Video.ordinal && category != FileCategory.Picture.ordinal && category != FileCategory.Music.ordinal && category != FileCategory.Document.ordinal && category != FileCategory.Folder.ordinal
            ) {
                it.remove()
            }
        }
        return tempDataList
    }

    /**
     * 获取当前扫描路径
     *
     * @return
     */
    fun getCurrentPath(): String? {
        return mCurrentPath
    }

    /**
     * 停止扫描
     */
    fun stopSearch() {
        mSearcher.stoped = true
    }

    /**
     * 删除监听
     */
    fun removeListener() {
        mSearchListener = null
    }

    override fun run() { //开始扫描
        onSearchStart()
        mSearcher.search()
        if (!mSearcher.stoped!!) {
            Collections.sort(
                getDataList(),
                Comparators.getForFile(mSortMode)
            )
            onSearchCompleted()
        }
    }

    /**
     * 开始搜索
     */
    private fun onSearchStart() {
        mHandler?.apply {
            var startMsg = this.obtainMessage()
            startMsg.what = SearchLocalFileStart
            startMsg.obj = mCurrentPath
            this.sendMessage(startMsg)
        }
        mSearchListener?.onSearchStart()
    }

    /**
     * 扫描结束
     */
    private fun onSearchCompleted() { //扫描结束
        if (mHandler != null) {
            val msg = mHandler!!.obtainMessage()
            msg.obj = mCurrentPath
            msg.what = SearchLocalFileDone
            mHandler!!.sendMessage(msg)
        }
        if (mSearchListener != null) {
            mSearchListener!!.onSearchCompleted()
        }
    }

    interface SearchListener {
        fun onSearchStart()
        fun onSearchCompleted()
    }

    class Builder(context: Context) {
        //搜索方式
        var builderSearchMode: SearchMode? = null
        // 排序方式
        var builderSortMode = 0
        //根目录
        var builderRootPath: String? = null
        //当前目录
        var builderCurrentPath: String? = null
        //搜索文件类型
        var builderFileType = 0
        //关键字
        var builderKeyWord: String? = null
        var buildBatchOperator: BatchOperator? = null
        val mContext: Context
        var buildSearchListener: SearchListener? = null
        var buildHandler: Handler? = null

        fun searchRootPath(root: String?): Builder {
            builderRootPath = root
            return this
        }

        fun searchPath(path: String?): Builder {
            builderCurrentPath = path
            return this
        }

        fun searchMode(searchMode: SearchMode?): Builder {
            builderSearchMode = searchMode
            return this
        }

        fun sortMode(sortMode: Int): Builder {
            builderSortMode = sortMode
            return this
        }

        fun searchFileType(type: Int): Builder {
            builderFileType = type
            return this
        }

        fun searchKeyWord(key: String?): Builder {
            builderKeyWord = key
            return this
        }

        fun dbBatchOperator(batchOperator: BatchOperator?): Builder {
            buildBatchOperator = batchOperator
            return this
        }

        fun addSearchListener(searchListener: SearchListener?): Builder {
            buildSearchListener = searchListener
            return this
        }

        fun setHandler(handler: Handler?): Builder {
            buildHandler = handler
            return this
        }

        fun build(): LocalFileSearcher {
            if (SearchMode.FullScan === builderSearchMode && buildBatchOperator == null) {
                buildBatchOperator = BatchOperator(mContext)
            }
            return LocalFileSearcher(this)
        }

        init {
            mContext = context.applicationContext
        }
    }
}
