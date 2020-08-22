package com.xgimi.filemanager.bean

import android.content.Context
import android.os.Build
import androidx.documentfile.provider.DocumentFile
import com.xgimi.filemanager.utils.DocumentsUtil
import java.io.*

/**
 *    author : joker.peng
 *    e-mail : joker.peng@xgimi.com
 *    date   : 2020/8/10 16:02
 *    desc   :
 */
class XFile {
    private var mFile: File? = null
    private var mDocumentFile: DocumentFile? = null
    private var mContext: Context? = null
    private var isSupportDocumentFile = false

    constructor(context: Context?, path: String?) : this(context, File(path))

    constructor(context: Context?, file: File?) {
        mContext = context
        mFile = file
        isSupportDocumentFile = isSupportDocumentFile()
        if (isSupportDocumentFile) {
            mDocumentFile = DocumentsUtil.getDocumentFile(context!!, mFile!!, false)
        }
    }

    fun getName(): String? {
        return mFile!!.name
    }

    fun getUsableSpace(): Long {
        return mFile!!.usableSpace
    }

    fun length(): Long {
        return mFile!!.length()
    }

    fun isDirectory(): Boolean {
        return mFile!!.isDirectory
    }

    fun listFiles(): Array<File?>? {
        return mFile!!.listFiles()
    }

    fun listFiles(filter: FilenameFilter?): Array<File?>? {
        return mFile!!.listFiles(filter)
    }

    fun exists(): Boolean {
        return mFile!!.exists()
    }

    fun canWrite(): Boolean {
        if (!exists()) {
            return false
        }
        return if (isSupportDocumentFile) {
            mDocumentFile != null && mDocumentFile!!.canWrite()
        } else {
            mFile!!.canWrite()
        }
    }

    fun mkdirs(): Boolean {
        return if (isSupportDocumentFile) {
            mDocumentFile = DocumentsUtil.getDocumentFile(mContext!!, mFile!!, true, true)
            mDocumentFile != null && mDocumentFile!!.exists()
        } else {
            mFile!!.mkdirs()
        }
    }

    @Throws(IOException::class)
    fun createNewFile(): Boolean {
        return if (isSupportDocumentFile) {
            mDocumentFile = DocumentsUtil.getDocumentFile(mContext!!, mFile!!, true, false)
            mDocumentFile != null && mDocumentFile!!.exists()
        } else {
            mFile!!.createNewFile()
        }
    }

    fun delete(): Boolean {
        if (!exists()) {
            return false
        }
        return if (isSupportDocumentFile) {
            mDocumentFile!!.delete()
        } else {
            mFile!!.delete()
        }
    }

    fun renameTo(dest: File?): Boolean {
        return DocumentsUtil.renameTo(mContext!!, mFile!!, dest!!)
    }

    @Throws(FileNotFoundException::class)
    fun getInputStream(): InputStream? {
        return if (isSupportDocumentFile) {
            if (mDocumentFile != null && mDocumentFile!!.canWrite()) {
                mContext!!.contentResolver.openInputStream(mDocumentFile!!.uri)
            } else null
        } else {
            FileInputStream(mFile)
        }
    }

    @Throws(FileNotFoundException::class)
    fun getOutputStream(): OutputStream? {
        return if (isSupportDocumentFile) {
            if (mDocumentFile != null && mDocumentFile!!.canWrite()) {
                mContext!!.contentResolver.openOutputStream(mDocumentFile!!.uri)
            } else null
        } else {
            FileOutputStream(mFile)
        }
    }

    private fun isSupportDocumentFile(): Boolean {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.P &&
                DocumentsUtil.isOnExtSdCard(mContext!!, mFile!!)
    }
}