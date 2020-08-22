package com.xgimi.filemanager.helper

import com.xgimi.filemanager.R
import com.xgimi.filemanager.bean.DeviceInfo
import com.xgimi.filemanager.enums.FileCategory
import com.xgimi.filemanager.utils.FileUtil
import java.util.*

/**
 *    author : joker.peng
 *    e-mail : joker.peng@xgimi.com
 *    date   : 2020/8/10 11:49
 *    desc   :
 */
object IconHelper {
    private val documentExtToIcons = HashMap<String, Int>()
    private val usbPopupDocumentExtToIcons = HashMap<String, Int>()
    private val deviceIcons = HashMap<Int, Int>()
    private val fileCategoryIcons = HashMap<Int, Int?>()

    // 文件扩展名找图标
    init {
        addItem(arrayOf("txt", "log", "xml", "ini", "lrc"), R.drawable.ic_icon_explorer_file_txt)
        addItem(arrayOf("doc", "docx"), R.drawable.ic_icon_explorer_file_word)
        addItem(arrayOf("ppt", "pptx"), R.drawable.ic_icon_explorer_file_ppt)
        addItem(arrayOf("xls", "xlsx"), R.drawable.ic_icon_explorer_file_excel)
        addItem(arrayOf("pdf"), R.drawable.ic_icon_explorer_file_pdf)
        addItem(arrayOf("zip"), R.drawable.ic_icon_explorer_package_zip)
        addItem(arrayOf("rar"), R.drawable.ic_icon_explorer_package_rar)
        addItem(arrayOf("iso"), R.drawable.ic_icon_explorer_package_iso)
        addItem(arrayOf("rar"), R.drawable.ic_icon_explorer_package_rar)
        addItem(arrayOf("tar"), R.drawable.ic_icon_explorer_package_tar)
        addItem(arrayOf("gz"), R.drawable.ic_icon_explorer_package_gz)
        addItem(arrayOf("apk"), R.drawable.ic_icon_explorer_file_apk)
        addUsbPopupItem(
            arrayOf("txt", "log", "xml", "ini", "lrc"),
            R.drawable.ic_icon_explorer_file_txt
        )
        addUsbPopupItem(arrayOf("doc", "docx"), R.drawable.ic_icon_explorer_file_ppt)
        addUsbPopupItem(arrayOf("ppt", "pptx"), R.drawable.ic_icon_explorer_file_ppt)
        addUsbPopupItem(arrayOf("xls", "xlsx"), R.drawable.ic_icon_explorer_file_excel)

        addUsbPopupItem(arrayOf("pdf"), R.drawable.ic_icon_explorer_file_pdf)
        addUsbPopupItem(arrayOf("zip"), R.drawable.ic_icon_explorer_package_zip)

        addUsbPopupItem(arrayOf("rar"), R.drawable.ic_icon_explorer_package_rar)
        addUsbPopupItem(arrayOf("iso"), R.drawable.ic_icon_explorer_package_iso)
        addUsbPopupItem(arrayOf("rar"), R.drawable.ic_icon_explorer_package_rar)
        addUsbPopupItem(arrayOf("tar"), R.drawable.ic_icon_explorer_package_tar)
        addUsbPopupItem(arrayOf("gz"), R.drawable.ic_icon_explorer_package_gz)

        deviceIcons[DeviceInfo.DeviceCategory.Usb.ordinal] = R.drawable.ic_icon_explorer_usbdrive
        deviceIcons[DeviceInfo.DeviceCategory.Samba.ordinal] =
            R.drawable.ic_icon_explorer_sharing_samba
        deviceIcons[DeviceInfo.DeviceCategory.Dlna.ordinal] =
            R.drawable.ic_icon_explorer_sharing_dlna
        deviceIcons[DeviceInfo.DeviceCategory.AddDevice.ordinal] = R.drawable.ic_icon_plus
        deviceIcons[DeviceInfo.DeviceCategory.LOCALUSB.ordinal] =
            R.drawable.ic_icon_explorer_memorycard

        fileCategoryIcons[FileCategory.Folder.ordinal] = R.drawable.ic_icon_explorer_sharing_folder
        fileCategoryIcons[FileCategory.Video.ordinal] = R.drawable.ic_icon_explorer_movie
        fileCategoryIcons[FileCategory.Music.ordinal] = R.drawable.ic_icon_explorer_music
        fileCategoryIcons[FileCategory.Picture.ordinal] = R.mipmap.new_file_picture_default

    }

    private fun addItem(exts: Array<String>?, resId: Int) {
        if (exts != null) {
            for (ext in exts) {
                documentExtToIcons[ext.toLowerCase()] = resId
            }
        }
    }

    private fun addUsbPopupItem(exts: Array<String>?, resId: Int) {
        if (exts != null) {
            for (ext in exts) {
                usbPopupDocumentExtToIcons[ext.toLowerCase()] = resId
            }
        }
    }

    fun getUsbPopupDocumentFileIcon(name: String?): Int {
        val ext: String? = FileUtil.getFileFormat(name!!)
        val i = usbPopupDocumentExtToIcons[ext?.toLowerCase()]
        return i ?: R.drawable.ic_icon_explorer_file_blank
    }

    private fun getDocumentFileIcon(name: String?): Int {
        val ext: String? = FileUtil.getFileFormat(name!!)
        val i = documentExtToIcons[ext?.toLowerCase()]
        return i ?: R.drawable.ic_icon_explorer_file_blank
    }

    fun getDeviceIcon(type: Int): Int {
        return deviceIcons[type]!!
    }

    fun getCategoryIcon(type: Int, name: String?): Int {
        return if (type != FileCategory.Document.ordinal && fileCategoryIcons.containsKey(type)) {
            fileCategoryIcons[type]!!
        } else {
            getDocumentFileIcon(name)
        }
    }

    fun getCategoryIcon(type: Int): Int {
        return getCategoryIcon(type, "")
    }
}