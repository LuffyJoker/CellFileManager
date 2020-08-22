package com.xgimi.filemanager.filehelper

import com.xgimi.filemanager.R
import com.xgimi.filemanager.bean.BaseData
import com.xgimi.filemanager.bean.DeviceInfo
import java.util.*

/**
 *    author : joker.peng
 *    e-mail : joker.peng@xgimi.com
 *    date   : 2020/8/7 11:08
 *    desc   :
 */
class OperationEvent {
    companion object {
        const val OPERATION_NULL = 0
        const val CopyMode = 1.shl(1)
        const val CutMode = 1.shl(2)
        const val NewDir = 1.shl(3)
        const val NewFile = 1.shl(4)
        const val DelFile = 1.shl(5) //删除文件
        const val DelHistory = 1.shl(6) //删除单个播放记录
        const val ClearHistory = 1.shl(7) //清空所有播放记录
        const val Rename = 1.shl(8) //重命名
        const val FileDetail = 1.shl(9) //文件详情
        const val ChangeLayoutType = 1.shl(10) //文件详情
        const val Paste = 1.shl(11) //文件详情
        const val SearchFile = 1.shl(12)
        const val SearchDlna = 1.shl(13)
        const val DecompressFile = 1.shl(14)
        const val OpenAs = 1.shl(15)
        const val OpenSambaServer = 1.shl(16)
        const val CloseSambaServer = 1.shl(17)
        const val ClearUsb = 1.shl(18)
        const val HideFile = 1.shl(19)
        const val UnselectAll = 1.shl(20)
        const val SelectInvert = 1.shl(21)
        const val SelectAll = 1.shl(22)
        const val ShowFile = 1.shl(23)
        const val ChangeDisplayMode = 1.shl(24)
        const val unMountDevice = 1.shl(25) //删除设备
        const val Multiselect = 1.shl(26) //多选
        const val Sortorder = 1.shl(27) //排序
        const val Refresh = 1.shl(28) //刷新
        const val OpenMode = 1.shl(29) //打开方式
    }

    var operationType = OPERATION_NULL

    var newName: String? = null
    var icon = 0
    var title = 0
    var operationFinish = false
    var deviceInfo: DeviceInfo? = null
    var mOperationList: ArrayList<BaseData> = ArrayList<BaseData>()
    var selectPath: String? = null
    var pasteDestinction: String? = null
    var statue = 0
    var decompressInfo: DecompressInfo? = null
    var hasIcon = false

    constructor()

    fun constructor(operationType: Int, disc: Int) {
        this.operationType = operationType
        title = disc
        hasIcon = false
        operationFinish = false
    }

    fun constructor(operationType: Int, disc: Int, icon: Int) {
        this.operationType = operationType
        title = disc
        this.icon = icon
        hasIcon = true
        operationFinish = false
    }

    constructor(operationType: Int) {
        this.operationType = operationType.and(this.operationType)
        operationFinish = false
        when (operationType) {
            CopyMode -> {
                title = R.string.copy
            }
            CutMode -> {
                title = R.string.cut
            }
            NewDir -> {
                title = R.string.new_dir
            }
            NewFile -> {
                title = R.string.newfile
            }
            DelFile -> {
                title = R.string.delfile
            }
            DelHistory -> {
                title = R.string.delhistory
            }
            ClearHistory -> {
                title = R.string.clearhistory
            }
            Rename -> {
                title = R.string.rename
            }
            FileDetail -> {
                title = R.string.detail
            }
        }
    }

    fun clearFlag() {
        operationType = OPERATION_NULL
    }

    class DecompressInfo(var filePath: String, var destinationPath: String)
}