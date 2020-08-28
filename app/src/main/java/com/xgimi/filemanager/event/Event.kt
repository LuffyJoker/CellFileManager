package com.xgimi.filemanager.event

/**
 *    author : joker.peng
 *    e-mail : joker.peng@xgimi.com
 *    date   : 2020/8/19 17:56
 *    desc   :
 */
class Event {
    companion object {
        val tag_PageScrollUp = "tag_pagescrollup" //页面滚动置顶,需要控制tablayout的显示和隐藏

        val tag_SmartTabFocus = "tag_smarttabfocus"
        val tag_OperationRename = "tag_OperationRename" //修改文件名编辑文件

        val tag_Folder_Click = "tag_Folder_Click" //文件夹点击

        val tag_Unmount_Samba = "tag_Unmount_Samba"

        const val CONNECT_TO_SAMBA = "connect_to_samba"
    }

    class Normal {
        var i0 = 0
        var i1 = 0
        var f0 = 0f
        var str0: String? = null
        var str1: String? = null
        var str2: String? = null
        var bool = false
        var obj0: Any? = null

        constructor() : super() {}
        constructor(i0: Int) {
            this.i0 = i0
        }

        constructor(bool: Boolean) {
            this.bool = bool
        }

        constructor(str0: String?) {
            this.str0 = str0
        }

        constructor(i0: Int, i1: Int) {
            this.i0 = i0
            this.i1 = i1
        }

        constructor(str0: String?, str1: String?) {
            this.str0 = str0
            this.str1 = str1
        }

        constructor(bool: Boolean, str0: String?) {
            this.bool = bool
            this.str0 = str0
        }

        constructor(
            f0: Float,
            str0: String?,
            str1: String?,
            str2: String?
        ) {
            this.f0 = f0
            this.str0 = str0
            this.str1 = str1
            this.str2 = str2
        }

        constructor(i0: Int, bool: Boolean) {
            this.i0 = i0
            this.bool = bool
        }

        constructor(o: Any?) {
            obj0 = o
        }
    }

}
