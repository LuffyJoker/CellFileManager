package com.xgimi.filemanager.bean

import java.io.File

/**
 *    author : joker.peng
 *    e-mail : joker.peng@xgimi.com
 *    date   : 2020/8/10 15:57
 *    desc   :
 */
class FileInfo {
    var fileName: String? = null

    var filePath: String? = null

    var fileSize: Long = 0

    var IsDir = false

    var Count = 0

    var ModifiedDate: Long = 0

    var Selected = false

    var canRead = false

    var canWrite = false

    var isHidden = false

    var dbId: Long = 0  // id in the database, if is from database

    var childFile = 0

    var childDir = 0

    constructor()

    constructor(filePath: String?) {
        init(File(filePath))
    }

    constructor(file: File?) {
        init(file)
    }


    private fun init(file: File?) {
        if (file != null && file.exists()) {
            canRead = file.canRead()
            canWrite = file.canWrite()
            isHidden = file.isHidden
            fileName = file.name
            ModifiedDate = file.lastModified()
            IsDir = file.isDirectory
            filePath = file.path
            fileSize = file.length()
        }
    }
}