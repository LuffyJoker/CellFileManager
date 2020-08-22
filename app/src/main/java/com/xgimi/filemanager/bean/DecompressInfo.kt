package com.xgimi.filemanager.bean

/**
 *    author : joker.peng
 *    e-mail : joker.peng@xgimi.com
 *    date   : 2020/8/7 11:12
 *    desc   :
 */
class DecompressInfo {
    var filePath: String? = null
    var destinationPath: String? = null

    constructor(filePath: String?, destinationPath: String?) {
        this.filePath = filePath
        this.destinationPath = destinationPath
    }
}