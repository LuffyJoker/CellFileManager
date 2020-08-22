package com.xgimi.filemanager.contentprovider

import android.database.ContentObserver
import android.os.Handler

/**
 *    author : joker.peng
 *    e-mail : joker.peng@xgimi.com
 *    date   : 2020/8/17 11:23
 *    desc   :
 */
class MediaContentObserver : ContentObserver {

    interface ContentObserverListener {
        fun onChange()
    }

    private var mContentObserverListener: ContentObserverListener? = null
    private var mHandler: Handler? = null

    constructor(handler: Handler?, listener: ContentObserverListener?) : super(handler) {
        mHandler = handler
        mContentObserverListener = listener
    }

    override fun onChange(selfChange: Boolean) {
        notifyFileChanged()
    }

    @Synchronized
    fun notifyFileChanged() {
        mHandler!!.removeCallbacks(notify)
        mHandler!!.postDelayed(notify, 1000)
    }

    private val notify =
        Runnable { if (mContentObserverListener != null) mContentObserverListener!!.onChange() }
}