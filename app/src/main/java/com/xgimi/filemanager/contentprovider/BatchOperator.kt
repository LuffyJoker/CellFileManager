package com.xgimi.filemanager.contentprovider

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.RemoteException
import java.util.*

/**
 *    author : joker.peng
 *    e-mail : joker.peng@xgimi.com
 *    date   : 2020/8/19 10:27
 *    desc   : 数据库批量操作类
 */
class BatchOperator {
    private var mContext: Context? = null
    /**
     * 批量操作的最大缓存
     */
    private val BUFFER_SIZE_MAX = 200

    /**
     * 缓存队列
     */
    private val mRowMap =
        HashMap<Uri, MutableList<ContentValues>>()

    /**
     * 优先缓存队列
     */
    private val mPriorityRowMap =
        HashMap<Uri, MutableList<ContentValues>>()

    constructor(context: Context?) {
        mContext = context
    }

    /**
     * 插入数据
     * @param tableUri 表名
     * @param values 待插入数据
     * @throws RemoteException
     */
    @Throws(RemoteException::class)
    fun insert(tableUri: Uri, values: ContentValues) {
        insert(tableUri, values, false)
    }


    /**
     * 插入数据
     * @param tableUri 表名
     * @param values 待插入数据
     * @param priority 是否优先
     * @throws RemoteException
     */
    @Throws(RemoteException::class)
    private fun insert(
        tableUri: Uri,
        values: ContentValues,
        priority: Boolean
    ) {
        val rowmap =
            if (priority) mPriorityRowMap else mRowMap
        var list = rowmap[tableUri]
        if (list == null) {
            list = ArrayList()
            rowmap[tableUri] = list
        }
        list.add(ContentValues(values))
        if (list.size >= BUFFER_SIZE_MAX) {
            flushAllPriority()
            flush(tableUri, list)
        }
    }

    /**
     * 缓存批量写入数据库
     * @throws RemoteException
     */
    @Throws(RemoteException::class)
    fun flushAllPriority() {
        for (tableUri in mPriorityRowMap.keys) {
            val list = mPriorityRowMap[tableUri]
            flush(tableUri, list)
        }
        for (tableUri in mRowMap.keys) {
            val list = mRowMap[tableUri]
            flush(tableUri, list)
        }
        mPriorityRowMap.clear()
        mRowMap.clear()
    }

    /**
     * 批量写入数据库
     * @throws RemoteException
     */
    @Throws(RemoteException::class)
    private fun flush(tableUri: Uri, list: MutableList<ContentValues>?) {
        if (list!!.isNotEmpty()) {
            var valuesArray: Array<ContentValues?>? = arrayOfNulls(list.size)
            valuesArray = list.toTypedArray()
            mContext!!.contentResolver.bulkInsert(tableUri, valuesArray!!)
            list.clear()
        }
    }
}
