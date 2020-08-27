package com.xgimi.samba.bean

import android.os.SystemClock
import android.util.Log

/**
 *    author : joker.peng
 *    e-mail : joker.peng@xgimi.com
 *    date   : 2020/8/26 16:11
 *    desc   :
 */
class MethodTime {

    var start: Long = 0
    var tag: String? = null

    constructor(tag: String?) {
        this.tag = tag
    }

    fun start() {
        start = SystemClock.elapsedRealtime()
    }

    fun end(log: Boolean, method: String): Long {
        val time = SystemClock.elapsedRealtime() - start
        if (log) {
            Log.i(tag, "$method once time=$time")
        }
        return time
    }
}