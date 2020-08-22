package com.xgimi.filemanager.interfaces

/**
 *    author : joker.peng
 *    e-mail : joker.peng@xgimi.com
 *    date   : 2020/8/10 16:31
 *    desc   :
 */
interface OnDepressInfo {
    fun onDepressSuccess(destPath: String?)
    fun onDepressFailure()
}