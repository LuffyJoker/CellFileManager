package com.xgimi.filemanager.bean

import com.xgimi.samba.bean.CategoryBean
import java.util.*

/**
 *    author : joker.peng
 *    e-mail : joker.peng@xgimi.com
 *    date   : 2020/8/10 15:01
 *    desc   :
 */
class CatalogInfo : CategoryBean {

    private val serialVersionUID = 1252710799060177463L
    // 类别
    var type = 0
    var name: String? = null
    var title: String? = null
    var datas: MutableList<BaseData> = ArrayList()

    constructor()

    constructor(name: String) {
        this.name = name
    }

    fun add(baseData: BaseData) {
        datas.add(baseData)
    }

    override fun equals(o: Any?): Boolean {
        if (this === o) return true
        if (o == null || CategoryBean::class.java !== o.javaClass) return false
        val that = o as CatalogInfo
        if (category !== that.category) return false
        return if (name != null) name == that.name else that.name == null
    }

    override fun hashCode(): Int {
        var result = category
        result = 31 * result + if (name != null) name.hashCode() else 0
        return result
    }
}