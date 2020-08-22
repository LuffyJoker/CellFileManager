package com.xgimi.filemanager.searcher

import com.xgimi.filemanager.bean.BaseData
import java.io.File

/**
 *    author : joker.peng
 *    e-mail : joker.peng@xgimi.com
 *    date   : 2020/8/19 10:48
 *    desc   :
 */
class LayerSearcher : Searcher {

    /**
     * 排序
     */
    constructor(rootPath: String?, scanPath: String?) : super(rootPath, scanPath)

    override fun handFile(file: File?) {
        val baseData = BaseData(mRootPath, file!!)
        if (canShowFile(baseData)) dataList.add(baseData)
    }

    override fun completedSearch() {}
}