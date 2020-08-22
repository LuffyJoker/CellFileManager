package com.xgimi.filemanager.bean

import java.io.Serializable

/**
 *    author : joker.peng
 *    e-mail : joker.peng@xgimi.com
 *    date   : 2020/8/17 16:18
 *    desc   :
 */
class MediaEntity : Serializable {
    private val serialVersionUID = 1773561717720989352L

    //id标识
    var id = 0

    // 显示名称
    var title: String? = null

    // 文件名称
    var display_name: String? = null

    // 音乐文件的路径
    var path: String? = null

    // 媒体播放总时间
    var duration = 0

    // 专辑
    var albums: String? = null

    // 艺术家
    var artist: String? = null

    //歌手
    var singer: String? = null
    var size: Long = 0
    var albumid: Long = 0
    var albumArt: String? = null

    override fun toString(): String {
        return "MediaEntity{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", display_name='" + display_name + '\'' +
                ", path='" + path + '\'' +
                ", duration=" + duration +
                ", albums='" + albums + '\'' +
                ", artist='" + artist + '\'' +
                ", singer='" + singer + '\'' +
                ", size=" + size +
                ", albumid=" + albumid +
                ", albumArt='" + albumArt + '\'' +
                '}'
    }
}