package com.xgimi.filemanager.bean

/**
 *    author : joker.peng
 *    e-mail : joker.peng@xgimi.com
 *    date   : 2020/8/7 11:11
 *    desc   :
 */
class ProgressInfo {
    var orginal: String? = null
    var dest: String? = null
    var count: Long = 0
    var length: Long = 0

    constructor()

    fun constructor(orginal: String?, dest: String?, count: Long, length: Long) {
        this.orginal = orginal
        this.dest = dest
        this.count = count
        this.length = length
    }
}