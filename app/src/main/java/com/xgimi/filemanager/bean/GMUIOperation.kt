package com.xgimi.filemanager.bean

/**
 *    author : joker.peng
 *    e-mail : joker.peng@xgimi.com
 *    date   : 2020/8/14 16:58
 *    desc   :
 */
class GMUIOperation {
    var mStr: String? = null
    var mStrRes = 0
    var mOperationType = 0

    constructor()

    constructor(type: Int, str: String?) {
        mOperationType = type
        mStr = str
    }

    constructor(type: Int, str: Int) {
        mOperationType = type
        mStrRes = str
    }
}