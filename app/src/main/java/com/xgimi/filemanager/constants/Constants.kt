package com.xgimi.filemanager.constants

import android.os.Environment
import java.io.File

/**
 *    author : joker.peng
 *    e-mail : joker.peng@xgimi.com
 *    date   : 2020/8/7 11:20
 *    desc   :
 */
object Constants {

    val SAMBA_PATH = Environment.getExternalStorageDirectory().absolutePath + File.separator + "samba"

    const val FROM = "filemanager"
    const val ARG_VIEWPAGER_ID = "viewpager_id"
    const val ARG_POSITION = "position"

    const val ROOT_PATH_STR = "RootPath"
    const val DEVICE_NAME = "DeviceName"
    const val PATH = "PATH"
    const val NAME = "NAME"
    const val DEVICE_TYPE = "DeviceType"
    const val DISPLAY_NAME = "DisplayName"

    const val GROUP_SIZE = 6

    /**
     * 根路径
     */
    const val ROOT_PATH = "/"
    /**
     * 图片限制大小
     */
    var DEFAULTPICTURESIZE = 1024 * 50 //50K

    /**
     * 视频限制大小
     */
    var DEFAULTVIDEOSIZE = 10485760 //10M

    /**
     * 500M
     */
    var _500M = 500 * 1024 * 1024.toLong()
    /**
     * 音效限制大小
     */
    var DEFAULTAUDIOSIZE = 1048576 //1M

    /**
     * 取消粘贴
     */
    var CancelCopy = false
    /**
     * Handler what
     */
    const val ToConnectSamba = 6020
    /**
     * Extra key
     */
    const val CATEGORY = "category"
    const val TITLE = "title"
    const val CATEGORY_DATA = "PictureList"
    const val PARENT_DIR_NAME = "ParentDirName"

    //文件扫描
    const val SearchDlnaFileDone = 5000
    const val LayoutType = "LayoutType"
    const val Sortorder = "Sortorder"
    const val DisplayMode = "DisplayMode"

    //焦点框放大倍数
    const val DEFAULT_CARD_FOCUS_BORDER_W_SCALE = 1.28f
    const val DEFAULT_CARD_FOCUS_BORDER_H_SCALE = 1.28f
    const val DEFAULT_LIST_FOCUS_BORDER_W_SCALE = 1.05f
    const val DEFAULT_LIST_FOCUS_BORDER_H_SCALE = 1.2f
    const val DEFAULT_BUTTON_FOCUS_BORDER_W_SCALE = 1.4f
    const val DEFAULT_BUTTON_FOCUS_BORDER_H_SCALE = 1.96f

    const val DEFAULT_BUTTON_FOCUS_BORDER_W_SCALE_8_0 = 1.5f
    const val DEFAULT_BUTTON_FOCUS_BORDER_H_SCALE_8_0 = 2.2f

    const val SMB_MOUNT_PATH = "/mnt/samba/"
    const val FILE_SEPARATOR = "/"
}