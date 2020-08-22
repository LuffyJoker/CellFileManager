package com.xgimi.filemanager.bean

import android.text.TextUtils
import java.util.*

/**
 *    author : joker.peng
 *    e-mail : joker.peng@xgimi.com
 *    date   : 2020/8/10 14:59
 *    desc   :
 */
class CatalogInfoList : ArrayList<Any>() {
    fun containsKey(key: String): Boolean {
        if (!TextUtils.isEmpty(key) && size > 0) {
            for (i in 0 until size) if (get(i) is CatalogInfo && key == (get(i) as CatalogInfo).name) return true
        }
        return false
    }

    operator fun get(key: String): Any? {
        for (i in 0 until size) if (get(i) is CatalogInfo && key == (get(i) as CatalogInfo).name) return get(
            i
        )
        return null
    }

    fun getAllChildSize(): Int {
        var size = 0
        for (i in 0 until size) {
            size += if (get(i) is CatalogInfo) (get(i) as CatalogInfo).datas.size else 1
        }
        return size
    }
}