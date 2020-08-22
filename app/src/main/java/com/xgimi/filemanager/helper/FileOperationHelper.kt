package com.xgimi.filemanager.helper

import android.content.Context
import android.os.AsyncTask
import android.text.TextUtils
import android.util.Log
import android.widget.Toast
import com.blankj.utilcode.util.LogUtils
import com.blankj.utilcode.util.ShellUtils
import com.blankj.utilcode.util.StringUtils
import com.blankj.utilcode.util.ToastUtils
import com.hierynomus.smbj.share.DiskShare
import com.xgimi.filemanager.FileManagerApplication
import com.xgimi.filemanager.R
import com.xgimi.filemanager.bean.FileInfo
import com.xgimi.filemanager.bean.XFile
import com.xgimi.filemanager.constants.Constants
import com.xgimi.filemanager.filehelper.OperationEvent
import com.xgimi.filemanager.interfaces.OnDepressInfo
import com.xgimi.filemanager.listerners.IOperationListener
import com.xgimi.filemanager.utils.FileNameUtil
import com.xgimi.filemanager.utils.FileUtil
import com.xgimi.filemanager.utils.ZipProcess
import com.xgimi.samba.ShareItem
import com.xgimi.samba.bean.ShareFile
import java.io.*
import java.util.*

/**
 *    author : joker.peng
 *    e-mail : joker.peng@xgimi.com
 *    date   : 2020/8/10 15:56
 *    desc   :
 */
class FileOperationHelper {

    private val LOG_TAG = "FileOperation"

    private val mCurFileNameList: ArrayList<FileInfo> = ArrayList<FileInfo>()

    private var mMoving = false

    private var mOperationListener: IOperationListener? = null

    private var mFilter: FilenameFilter? = null

    private var context: Context? = null

    @Volatile
    private var isSambaPasting = false

    private var isRomEnough = true

    constructor(cnt: Context?, l: IOperationListener?) {
        context = cnt
        mOperationListener = l
    }


    fun createFolder(path: String, name: String): Boolean {
        LogUtils.v(LOG_TAG, "CreateFolder >>> $path,$name")
        val f = File(makePath(path, name))
        return if (f.exists()) {
            false
        } else f.mkdir()
    }


    /**
     * 粘贴
     *
     * @param files
     * @param path
     * @return
     */
    fun pasteFiles(files: ArrayList<FileInfo>, path: String): Boolean {
        copyFileList(files)
        if (mCurFileNameList.size == 0) {
            return false
        }
        asyncExecute(Runnable {
            for (f in mCurFileNameList) {
                copyFileOrDir(f, path, false)
            }
            clear()
            if (mOperationListener != null) {
                mOperationListener?.onOperationFinish(OperationEvent.Paste, path)
            }
        })
        return true
    }

    /**
     * 复制samba的歌词或字幕文件
     *
     * @param item
     * @param path
     */
    fun pasteSambaSubtitle(item: ShareItem, path: String) {
        if (context == null) {
            context = FileManagerApplication.getInstance().applicationContext
        }
        val destPlace = XFile(context, makePath(path, item.name))
        if (destPlace.exists()) {
            return
        }
        asyncExecute(Runnable { copySambaFile(item, path, mOperationListener) })
    }

    fun pasteSambaFile(item: ShareItem, path: String?) {
        asyncExecute(Runnable {
            isSambaPasting = true
            copySambaFile(item, path, mOperationListener)
            isSambaPasting = false
            clear()
            if (mOperationListener != null) {
                mOperationListener?.onOperationFinish(OperationEvent.Paste, path)
            }
            if (!isSambaPasting) {
                try {
                    (item as ShareFile).diskShare.close()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        })
    }

    /**
     * 复制单个文件或文件夹
     *
     * @param f
     * @param dest
     * @param delete
     */
    private fun copyFileOrDir(
        f: FileInfo?,
        dest: String?,
        delete: Boolean
    ) {
        if (Constants.CancelCopy) {
            return
        }
        if (f == null || StringUtils.isEmpty(f.filePath) || dest == null) {
            Log.e(LOG_TAG, "CopyFile: null parameter")
            return
        }
        if (context == null) {
            context = FileManagerApplication.getInstance().applicationContext
        }
        val file = XFile(context, f.filePath)
        var allCopy = true
        if (file.isDirectory()) {
            Log.e(LOG_TAG, "isDirectory")
            var destPath = makePath(dest, f.fileName!!)
            var destFile = XFile(context, destPath)
            var i = 1
            while (destFile.exists()) {
                destPath = makePath(dest, f.fileName.toString() + " " + i++)
                destFile = XFile(context, destPath)
            }
            if (f.IsDir) {
                val success: Boolean = destFile.mkdirs()
                if (success) {
                    ShellUtils.execCmd(arrayOf("chmod 777 $destPath"), false)
                }
                if (mOperationListener != null) {
                    mOperationListener?.onCreateNewDirectory(destPath)
                }
                Log.e(LOG_TAG, "isDirectory$success")
            }
            val listFiles: Array<File?>? = file.listFiles(mFilter)
            if (listFiles != null) {
                for (child in listFiles) {
                    if (!child?.isHidden!! && FileUtil.isNormalFile(child?.absolutePath!!)) {
                        copyFileOrDir(FileInfo(child), destPath, delete)
                    }
                }
            }
        } else { // 此处，文件复制失败 destFile 会返回 null，此时，将 allCopy 设置为 false。避免不断的一直弹出 toast 提示
            if (isRomEnough) {
                val destFile = copyFile(f.filePath!!, dest, mOperationListener)
                if (destFile == null) {
                    allCopy = false
                    isRomEnough = false
                } else if (delete) {
                    deleteFileOrDir(f)
                }
            }
        }
        if (delete && file.exists() && file.isDirectory() && allCopy) {
            file.delete()
        }
    }


    //注意samba InputStream为统一获取的DisShare InputStream,与samba绑定.不单独进行close
    private fun copySambaFile(
        item: ShareItem?,
        dest: String?,
        listener: IOperationListener?
    ) {
        if (context == null) {
            context = FileManagerApplication.getInstance().applicationContext
        }
        if (item == null || StringUtils.isEmpty(item.getPath()) || dest == null) {
            Log.e(LOG_TAG, "CopyFile: null parameter")
            return
        }
        if (item is DiskShare) {
            return
        }
        if (item.isDirectory()) {
            var destPath = makePath(dest, item.getName())
            var destFile = XFile(context, destPath)
            var i = 1
            while (destFile.exists()) {
                destPath = makePath(dest, item.getName().toString() + " " + i++)
                destFile = XFile(context, destPath)
            }
            val success: Boolean = destFile.mkdirs()
            if (success) {
                ShellUtils.execCmd(arrayOf("chmod 777 $destPath"), false)
            }
            listener?.onCreateNewDirectory(destPath)
            Log.e(LOG_TAG, "isDirectory$success")
            for (shareItem in item.getFileList()) {
                copySambaFile(shareItem, destPath, listener)
            }
        } else {
            var destPath: String? = null
            var fi: InputStream? = null
            var fo: OutputStream? = null
            val src: String = item.getPath()
            try {
                val shareFile: ShareFile = item as ShareFile
                val length: Long = shareFile.getFileSize()
                val destPlace = XFile(context, dest)
                if (!destPlace.exists()) {
                    if (!destPlace.mkdirs()) {
                        return
                    }
                }
                val useableSpace: Long = destPlace.getUsableSpace()
                if (length > useableSpace) {
                    if (listener != null) {
                        listener.onPasteFileFailure(
                            false,
                            src,
                            IOperationListener.ON_LACK_STORAGE_SPACE
                        )
                    }
                    return
                }
                destPath = makePath(dest, shareFile.name)
                var destFile = XFile(context, destPath)
                var i = 1
                while (destFile.exists()) {
                    val formatName: String? = FileUtil.getFileFormat(shareFile.name)
                    val destName: String =
                        FileUtil.getFileNameNoFormat(shareFile.name).toString() + " " + i++ + if (StringUtils.isEmpty(
                                formatName
                            )
                        ) "" else ".$formatName"
                    destPath = makePath(dest, destName)
                    destFile = XFile(context, destPath)
                }
                if (!destFile.createNewFile()) {
                    return
                }
                fi = shareFile.inputStream
                fo = destFile.getOutputStream()
                val count = 102400
                val buffer = ByteArray(count)
                var allCount: Long = 0
                var times = 0
                var read: Int
                listener?.onPasteProgressChange(shareFile.getPath(), destPath, 0, 0)
                while (fi.read(buffer, 0, count).also {
                        read = it
                    } != -1 && !Constants.CancelCopy) { //给予一次IO容错(samba复制大文件可能会出现IO错误)
                    try {
                        fo?.write(buffer, 0, read)
                    } catch (e: IOException) {
                        fo?.flush()
                        (fo as FileOutputStream?)!!.fd.sync()
                        fo?.write(buffer, 0, read)
                    }
                    allCount += read.toLong()
                    times++
                    if (shareFile.fileSize < 102400 * 10 * 10) {
                        listener?.onPasteProgressChange(
                            shareFile.getPath(),
                            destPath,
                            allCount,
                            length
                        )
                    } else {
                        if (times >= 20) {
                            listener?.onPasteProgressChange(
                                shareFile.path,
                                destPath,
                                allCount,
                                length
                            )
                            times = 0
                        }
                    }
                }
                fo?.flush()
                if (fo is FileOutputStream) {
                    (fo as FileOutputStream?)!!.fd.sync()
                }
                if (Constants.CancelCopy) {
                    destFile.delete()
                    destPath = null
                } else {
                    listener?.onPasteFileSuccess(false, destPath, length)
                }
            } catch (e: IOException) {
                e.printStackTrace()
                if (e.message!!.contains("ENOSPC")) {
                    listener?.onPasteFileFailure(
                        false,
                        src,
                        IOperationListener.ON_LACK_STORAGE_SPACE
                    )
                } else listener?.onPasteFileFailure(false, src, IOperationListener.ON_FILE_ERROR)
            } catch (e: Exception) {
                e.printStackTrace()
                listener?.onPasteFileFailure(false, src, IOperationListener.ON_FILE_ERROR)
            } finally {
                try {
                    fo?.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
    }


    /**
     * 复制单个文件
     *
     * @param src
     * @param dest
     * @param listener
     * @return
     */
    private fun copyFile(src: String, dest: String, listener: IOperationListener?): String? {
        if (context == null) {
            context = FileManagerApplication.getInstance().applicationContext
        }
        val file = XFile(context, src)
        var destPath: String? = null
        val length: Long = file.length()
        if (file.exists() && !file.isDirectory()) {
            var fi: InputStream? = null
            var fo: OutputStream? = null
            try {
                val destPlace = XFile(context, dest)
                if (!destPlace.exists()) {
                    if (!destPlace.mkdirs()) {
                        return null
                    }
                }
                val useableSpace: Long = destPlace.getUsableSpace()
                if (length > useableSpace) {
                    listener?.onPasteFileFailure(
                        false,
                        src,
                        IOperationListener.ON_LACK_STORAGE_SPACE
                    )
                    return null
                }
                destPath = makePath(dest, file.getName()!!)
                var destFile = XFile(context, destPath)
                var i = 1
                while (destFile.exists()) {
                    val formatName: String? = FileUtil.getFileFormat(file.getName()!!)
                    val destName: String =
                        FileUtil.getFileNameNoFormat(file.getName()!!).toString() + " " + i++ + if (StringUtils.isEmpty(
                                formatName
                            )
                        ) "" else ".$formatName"
                    destPath = makePath(dest, destName)
                    destFile = XFile(context, destPath)
                }
                Log.d("updateProgressInfo", "from:$src\n  to:$destPath")
                if (!destFile.createNewFile()) {
                    return null
                }
                fi = file.getInputStream()
                if (fi == null) {
                    return null
                }
                fo = destFile.getOutputStream()
                val count = 102400
                val buffer = ByteArray(count)
                var allCount: Long = 0
                var times = 0
                var read: Int
                while (fi.read(buffer, 0, count).also {
                        read = it
                    } != -1 && !Constants.CancelCopy) {
                    fo?.write(buffer, 0, read)
                    allCount += read.toLong()
                    times++
                    if (file.length() < 102400 * 10 * 10) {
                        listener?.onPasteProgressChange(src, destPath, allCount, length)
                    } else {
                        if (times >= 20) {
                            listener?.onPasteProgressChange(src, destPath, allCount, length)
                            times = 0
                        }
                    }
                }
                Log.d(LOG_TAG, "刷新前目标文件长度:" + destFile.length())
                fo?.flush()
                if (fo is FileOutputStream) {
                    (fo as FileOutputStream?)!!.fd.sync()
                }
                Log.d(LOG_TAG, "刷新后目标文件长度:" + destFile.length())
                if (Constants.CancelCopy) {
                    destFile.delete()
                    destPath = null
                } else {
                    if (listener != null) {
                        listener.onPasteFileSuccess(false, destPath, length)
                    }
                }
            } catch (e: FileNotFoundException) {
                e.printStackTrace()
                if (listener != null) {
                    listener.onPasteFileFailure(false, src, IOperationListener.ON_FILE_NOT_EXIST)
                }
            } catch (e: IOException) {
                e.printStackTrace()
                if (listener != null) {
                    listener.onPasteFileFailure(false, src, IOperationListener.ON_FILE_ERROR)
                }
            } finally {
                try {
                    fo?.close()
                    fi?.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
        return destPath
    }

    /**
     * 批量移动文件
     *
     * @param files
     * @param path
     * @return
     */
    fun moveFiles(files: ArrayList<FileInfo>, path: String?): Boolean {
        if (mMoving || TextUtils.isEmpty(path) || !canMove(path)) {
            return false
        }
        mMoving = true
        copyFileList(files)
        asyncExecute(Runnable {
            for (f in mCurFileNameList) {
                moveFile(f, path)
            }
            clear()
            mMoving = false
            mOperationListener?.onOperationFinish(OperationEvent.DelFile, path)

        })
        return true
    }

    /**
     * 移动单个文件
     *
     * @param f
     * @param dest
     * @return
     */
    private fun moveFile(f: FileInfo?, dest: String?): Boolean {
        Log.v(LOG_TAG, "MoveFile >>> " + f?.filePath.toString() + "," + dest)
        if (f == null || dest == null) {
            Log.e(LOG_TAG, "CopyFile: null parameter")
            return false
        }
        if (context == null) {
            context = FileManagerApplication.getInstance().applicationContext
        }
        val isSameCard: Boolean = FileUtil.isSameCard(context, f.filePath!!, dest)
        Log.e("MoveFile", "isSameCard>>>>>>>$isSameCard")
        val file = XFile(context, f.filePath)
        val newPath = makePath(dest, f.fileName!!)
        if (isSameCard) {
            try {
                val success: Boolean = file.renameTo(File(newPath))
                if (success) {
                    mOperationListener?.onPasteProgressChange(f.filePath, newPath, 1, 1)
                    Log.e("MoveFile", "rename success>>>>>>>>>>>>>>>>>>>>>>>>>>")
                    mOperationListener?.onRenameFileSuccess(f.filePath, newPath)
                }
                return success
            } catch (e: SecurityException) {
                Log.e(LOG_TAG, "Fail to move file,$e")
            }
        } else {
            Log.e(LOG_TAG, "Fail to copy file")
            copyFileOrDir(f, dest, true)
        }
        return false
    }

    /**
     * 是否能移动文件
     *
     * @param path
     * @return
     */
    private fun canMove(path: String?): Boolean {
        for (f in mCurFileNameList) {
            if (!f.IsDir) {
                continue
            }
            if (containsPath(f.filePath!!, path)) {
                return false
            }
        }
        return true
    }

    // if path1 contains path2
    private fun containsPath(path1: String, path2: String?): Boolean {
        var path = path2
        while (path != null) {
            if (path.equals(path1, ignoreCase = true)) {
                return true
            }
            if (path == Constants.ROOT_PATH) {
                break
            }
            path = File(path).parent
        }
        return false
    }

    /**
     * 删除文件
     *
     * @param files
     * @return
     */
    fun deleteFiles(
        files: ArrayList<FileInfo>,
        dest: String?
    ): Boolean {
        copyFileList(files)
        asyncExecute(Runnable {
            for (f in mCurFileNameList) {
                deleteFileOrDir(f)
            }
            clear()
            mOperationListener?.onOperationFinish(OperationEvent.DelFile, dest)

        })
        return true
    }

    /**
     * 删除单个文件或文件夹
     *
     * @param f
     */
    private fun deleteFileOrDir(f: FileInfo?) {
        if (f == null || StringUtils.isEmpty(f.filePath)) {
            Log.e(LOG_TAG, "DeleteFile: null parameter")
            return
        }
        if (context == null) {
            context = FileManagerApplication.getInstance().applicationContext
        }
        val file = XFile(context, f.filePath)
        val directory: Boolean = file.isDirectory()
        var fileList: Array<File?>? = null
        if (file.exists()) {
            fileList = file.listFiles(mFilter)
        }
        if (directory && fileList != null) {
            for (child in file.listFiles(mFilter)!!) {
                if (FileUtil.isNormalFile(child?.absolutePath!!)) {
                    deleteFileOrDir(FileInfo(child))
                }
            }
        }
        val success: Boolean = file.delete()
        if (success) {
            mOperationListener?.onDelFileSuccess(f.IsDir, f.filePath, f.fileSize)
        }
    }


    private fun copyFileList(files: ArrayList<FileInfo>) {
        synchronized(mCurFileNameList) {
            mCurFileNameList.clear()
            for (f in files) {
                if (f == null) {
                    continue
                }
                Log.e("copyFileList", "copyFileListmmmmmmm" + f.filePath)
                mCurFileNameList.add(f)
            }
        }
    }

    private fun makePath(path: String, name: String): String {
        return if (path.endsWith(File.separator)) {
            path + name
        } else path + File.separator + name
    }

    private fun asyncExecute(r: Runnable) {
        object : AsyncTask<Any?, Any?, Any?>() {
            override fun doInBackground(vararg params: Any?): Any? {
                synchronized(mCurFileNameList) { r.run() }
                return null
            }
        }.execute()
    }

    fun clear() {
        synchronized(mCurFileNameList) { mCurFileNameList.clear() }
    }

    /**
     * 重命名文件
     *
     * @return
     */
    fun renameFile(
        context: Context?,
        filePath: String,
        newName: String
    ): Boolean {
        var context = context
        if (context == null) {
            context = FileManagerApplication.getInstance().applicationContext
        }
        val fileName =
            filePath.substring(filePath.lastIndexOf(File.separator) + 1)
        if (!FileNameUtil.isValidFileName(newName)) {
            Toast.makeText(context, R.string.illegal_path, Toast.LENGTH_SHORT).show()
            return false
        }
        if (fileName == newName) {
            return true
        }
        val parentPath = filePath.substring(0, filePath.lastIndexOf("/") + 1)
        val destFilePath = parentPath + newName
        if (File(destFilePath).exists()) {
            Toast.makeText(context, R.string.rename_file_exist, Toast.LENGTH_SHORT).show()
            return false
        }
        val originalFile = XFile(context, filePath)
        val file = File(destFilePath)
        var ret: Boolean = originalFile.renameTo(file)
        Log.i("jal", "ret1=$ret")
        if (!ret) {
            ret = file.exists()
            Log.i("jal", "ret2=$ret")
        }
        if (ret) { //更新数据库信息
            mOperationListener?.onRenameFileSuccess(filePath, destFilePath)
            ToastUtils.showShort(R.string.rename_file_success)
            return true
        }
        Toast.makeText(context, R.string.rename_file_failure, Toast.LENGTH_SHORT).show()
        return false
    }

    /**
     * 创建新文件夹
     *
     * @return
     */
    fun createNewDir(
        context: Context?,
        newName: String,
        destPath: String
    ): Boolean {
        var context = context
        if (context == null) {
            context = FileManagerApplication.getInstance().applicationContext
        }
        if (StringUtils.isEmpty(newName)) {
            Toast.makeText(context, R.string.file_name_not_allow_null, Toast.LENGTH_SHORT).show()
            return false
        }
        if (!FileNameUtil.isValidFileName(newName)) {
            Toast.makeText(context, R.string.illegal_path, Toast.LENGTH_SHORT).show()
            return false
        }
        val destFilePath = destPath + File.separator + newName
        val destFile = XFile(context, destFilePath)
        if (destFile.exists()) {
            Toast.makeText(context, R.string.file_already_exist, Toast.LENGTH_SHORT).show()
            return false
        }
        val success: Boolean = destFile.mkdirs()
        if (success) {
            ShellUtils.execCmd(arrayOf("chmod 777 $destFilePath"), false)
            Toast.makeText(context, R.string.create_dir_success, Toast.LENGTH_SHORT).show()
            mOperationListener?.onCreateNewDirectory(destFilePath)
            return true
        }
        ToastUtils.showShort(R.string.create_dir_failure)
        return false
    }

    fun decompressFile(filePath: String) {
        var destPath = filePath.substring(0, filePath.lastIndexOf("."))
        val i = 1
        while (File(destPath).exists()) {
            destPath += "" + i
        }
        if (context == null) {
            context = FileManagerApplication.getInstance().applicationContext
        }
        extractProcess(context, filePath, destPath)
    }

    private fun extractProcess(
        context: Context?,
        src: String,
        dest: String
    ) {
        val sbCmd = StringBuilder("7z ")
        sbCmd.append("x ") //7z e || 7z x
        //input file mPathText
        sbCmd.append("'$src' ") //7z x 'aaa/bbb.zip'
        //output mPathText
        sbCmd.append("'-o$dest' ") //7z x 'a.zip' '-o/out/'
        ZipProcess(context, sbCmd.toString(), dest, object : OnDepressInfo {
            override fun onDepressSuccess(destPath: String?) {
                mOperationListener?.onDecompressSuccess(destPath)
            }

            override fun onDepressFailure() {
            }
        }).start()
    }

    fun destroy() {
        mOperationListener = null
        context = null
    }
}