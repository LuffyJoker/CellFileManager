package com.xgimi.filemanager.searcher

import com.blankj.utilcode.util.StringUtils
import com.xgimi.filemanager.bean.BaseData
import com.xgimi.filemanager.enums.FileCategory
import com.xgimi.filemanager.helper.ResourceHelper
import java.io.File

/**
 *    author : joker.peng
 *    e-mail : joker.peng@xgimi.com
 *    date   : 2020/8/19 10:44
 *    desc   :
 */
class KeyWordSearcher : Searcher {

    /**
     * 搜索文件类型，视频、音乐、图片、文档等
     */
    private var mSearchType = 0
    /**
     * 关键字
     */
    private var mKeyword: String? = null
    /**
     * 资源管理类
     */
    private var mResourceHelper: ResourceHelper? = null

    constructor(
        rootPath: String?,
        scanPath: String?,
        type: Int,
        keyword: String?,
        resoureHelper: ResourceHelper?
    ) : super(rootPath, scanPath) {
        mSearchType = type
        mKeyword = keyword
        mResourceHelper = resoureHelper
    }

    /**
     * 开始搜索
     */
    override fun search() {
        if (mSearchType == 0) {
            super.search()
        } else {
            dataList = ResourceHelper.searchFileSource(mSearchType, mKeyword!!)?.toMutableList()!!
            completedSearch()
        }
    }

    /**
     * 处理文件
     * @param file 待处理文件
     */
    override fun handFile(file: File?) {
        val baseData = BaseData(mRootPath, file!!)
        if (StringUtils.isEmpty(baseData.name) || StringUtils.isEmpty(mKeyword)) return
        if (!canShowFile(baseData)) {
            return
        }
        if (baseData.name!!.toLowerCase().contains(mKeyword!!.toLowerCase())) dataList.add(baseData)
        if (baseData.category == FileCategory.Folder.ordinal) scanFilesByRecursion(baseData.path)
    }

    /**
     * 扫描完成
     */
    override fun completedSearch() {}
}
