package com.xgimi.filemanager.bean

import java.util.*

/**
 *    author : joker.peng
 *    e-mail : joker.peng@xgimi.com
 *    date   : 2020/8/19 18:10
 *    desc   :
 */
class MusicFileInfo {

    //声明实现接口Parcelable
    var data: ArrayList<MusicDataInfo>? = null

    constructor() {
        data = ArrayList()
    }

    class MusicDataInfo(
        var musicType: Int,
        var musicName: String,
        var musicPath: String
    )
}