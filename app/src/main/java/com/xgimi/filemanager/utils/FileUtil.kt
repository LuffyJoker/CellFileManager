package com.xgimi.filemanager.utils

import android.content.Context
import android.os.Environment
import android.util.Log
import com.blankj.utilcode.util.StringUtils
import com.xgimi.filemanager.helper.MountHelper
import java.io.*
import java.text.SimpleDateFormat
import java.util.*

/**
 *    author : joker.peng
 *    e-mail : joker.peng@xgimi.com
 *    date   : 2020/8/10 11:50
 *    desc   :
 */
object FileUtil {
    /**
     * 写文本文件 在Android系统中，文件保存在 /data/data/PACKAGE_NAME/files 目录下
     *
     * @param context
     */
    fun write(
        context: Context,
        fileName: String?,
        content: String?
    ) {
        var content = content
        if (content == null) {
            content = ""
        }
        try {
            val fos =
                context.openFileOutput(fileName, Context.MODE_PRIVATE)
            fos.write(content.toByteArray())
            fos.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * 读取文本文件
     *
     * @param context
     * @param fileName
     * @return
     */
    fun read(context: Context, fileName: String?): String? {
        try {
            val `in` = context.openFileInput(fileName)
            return readInStream(`in`)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return ""
    }

    @Throws(IOException::class)
    fun read(file: File?): String? {
        val `in` = FileInputStream(file)
        val outStream = ByteArrayOutputStream()
        val buffer = ByteArray(512)
        var length = -1
        while (`in`.read(buffer).also { length = it } != -1) {
            outStream.write(buffer, 0, length)
        }
        outStream.close()
        `in`.close()
        return outStream.toString()
    }

    @Throws(IOException::class)
    fun write(file: File?, str: String) {
        val fout = FileOutputStream(file)
        val bytes = str.toByteArray()
        fout.write(bytes)
        fout.close()
    }

    fun readInStream(inStream: FileInputStream): String? {
        try {
            val outStream = ByteArrayOutputStream()
            val buffer = ByteArray(512)
            var length = -1
            while (inStream.read(buffer).also { length = it } != -1) {
                outStream.write(buffer, 0, length)
            }
            outStream.close()
            inStream.close()
            return outStream.toString()
        } catch (e: IOException) {
            Log.i("FileTest", e.message)
        }
        return null
    }

    /**
     * 根据文件绝对路径获取文件名
     *
     * @param filePath
     * @return
     */
    fun getFileName(filePath: String): String? {
        if (!StringUtils.isEmpty(filePath)) {
            val pos = filePath.lastIndexOf(File.separator)
            if (pos != -1) {
                return filePath.substring(pos + 1)
            }
        }
        return ""
    }

    /**
     * 根据文件的绝对路径获取文件名但不包含扩展名
     *
     * @param filePath
     * @return
     */
    fun getFileNameNoFormat(filePath: String): String? {
        if (StringUtils.isEmpty(filePath)) {
            return ""
        }
        var point = filePath.lastIndexOf('.')
        point = if (point == -1) filePath.length else point
        return filePath.substring(filePath.lastIndexOf(File.separator) + 1, point)
    }

    /**
     * 获取文件扩展名
     *
     * @param fileName
     * @return
     */
    fun getFileFormat(fileName: String): String? {
        if (StringUtils.isEmpty(fileName)) {
            return ""
        }
        val point = fileName.lastIndexOf('.')
        return if (point == -1) {
            ""
        } else fileName.substring(point + 1)
    }

    /**
     * 获取文件所在文件夹路径
     *
     * @param filepath
     * @return
     */
    fun getFileDir(filepath: String): String? {
        val pos = filepath.lastIndexOf('/')
        return if (pos != -1) {
            filepath.substring(0, pos)
        } else ""
    }

    class FileInfo {
        var length: String? = null
        var modificationTime: String? = null
    }

    fun getFileInfo(path: String?): FileInfo? {
        try {
            val file = File(path)
            if (file.exists() && file.isFile) {
                val fileInfo = FileInfo()
                fileInfo.length = formatFileSize(file.length())
                fileInfo.modificationTime = getModifyTimeStr(file)
                return fileInfo
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    /**
     * 转换文件大小
     *
     * @param fileS
     * @return B/KB/MB/GB
     */
    fun formatFileSize(fileS: Long): String? {
        val kb: Long = 1024
        val mb = kb * 1024
        val gb = mb * 1024
        val tb = gb * 1024
        return if (fileS >= tb) {
            String.format("%.1f TB", fileS.toFloat() / tb)
        } else if (fileS >= gb) {
            String.format("%.1f GB", fileS.toFloat() / gb)
        } else if (fileS >= mb) {
            val f = fileS.toFloat() / mb
            String.format(if (f > 100) "%.0f MB" else "%.1f MB", f)
        } else if (fileS >= kb) {
            val f = fileS.toFloat() / kb
            String.format(if (f > 100) "%.0f KB" else "%.1f KB", f)
        } else {
            String.format("%d B", fileS)
        }
    }

    fun formatTime(time: Long): String? {
        val df = SimpleDateFormat("yyyy-MM-dd ")
        return df.format(time)
    }

    /**
     * 获取目录文件大小
     *
     * @param file
     * @return
     */
    fun getFileSize(file: File?): Long {
        if (file == null || !file.exists()) {
            return 0
        }
        if (file.isFile) {
            return file.length()
        }
        val children = file.listFiles()
        var total: Long = 0
        if (children != null) {
            for (child in children) {
                total += getFileSize(child)
            }
        }
        return total
    }

    /**
     * 获取目录文件个数
     *
     * @return
     */
    fun getFileList(dir: File): Long {
        var count: Long = 0
        val files = dir.listFiles()
        count = files.size.toLong()
        for (file in files) {
            if (file.isDirectory) {
                count = count + getFileList(file) // 递归
                count--
            }
        }
        return count
    }

    @Throws(IOException::class)
    fun toBytes(`in`: InputStream): ByteArray? {
        val out = ByteArrayOutputStream()
        var ch: Int
        while (`in`.read().also { ch = it } != -1) {
            out.write(ch)
        }
        val buffer = out.toByteArray()
        out.close()
        return buffer
    }

    /**
     * 新建目录
     *
     * @param directoryName
     * @return
     */
    fun createDirectory(directoryName: String): String? {
        var filePath = ""
        if (directoryName != "") {
            val path = Environment.getExternalStorageDirectory()
            val newPath =
                File(path.toString() + File.separator + directoryName)
            newPath.mkdir()
            filePath = newPath.absolutePath
        }
        return filePath
    }

    /**
     * 检查是否安装SD卡
     *
     * @return
     */
    fun checkSaveLocationExists(): Boolean {
        val sDCardStatus = Environment.getExternalStorageState()
        val status: Boolean
        status = sDCardStatus == Environment.MEDIA_MOUNTED
        return status
    }

    /**
     * 删除目录(包括：目录里的所有文件)
     *
     * @param fileName
     * @return
     */
    fun deleteDirectory(fileName: String): Boolean {
        val status: Boolean
        val checker = SecurityManager()
        status = if (fileName != "") {
            val path = Environment.getExternalStorageDirectory()
            val newPath = File(path.toString() + fileName)
            checker.checkDelete(newPath.toString())
            if (newPath.isDirectory) {
                val listfile = newPath.list()
                try {
                    for (i in listfile.indices) {
                        val deletedFile =
                            File(newPath.toString() + "/" + listfile[i].toString())
                        deletedFile.delete()
                    }
                    newPath.delete()
                    true
                } catch (e: Exception) {
                    e.printStackTrace()
                    false
                }
            } else {
                false
            }
        } else {
            false
        }
        return status
    }

    /**
     * 删除文件
     *
     * @param fileName
     * @return
     */
    fun deleteFile(fileName: String): Boolean {
        val status: Boolean
        val checker = SecurityManager()
        status = if (fileName != "") {
            val path = Environment.getExternalStorageDirectory()
            val newPath = File(path.toString() + fileName)
            checker.checkDelete(newPath.toString())
            if (newPath.isFile) {
                try {
                    newPath.delete()
                    true
                } catch (se: SecurityException) {
                    se.printStackTrace()
                    false
                }
            } else {
                false
            }
        } else {
            false
        }
        return status
    }

    /**
     * 删除空目录
     *
     *
     * 返回 0代表成功 ,1 代表没有删除权限, 2代表不是空目录,3 代表未知错误
     *
     * @return
     */
    fun deleteBlankPath(path: String?): Int {
        val f = File(path)
        if (!f.canWrite()) {
            return 1
        }
        if (f.list() != null && f.list().size > 0) {
            return 2
        }
        return if (f.delete()) {
            0
        } else 3
    }

    /**
     * 重命名
     *
     * @param oldName
     * @param newName
     * @return
     */
    fun reNamePath(oldName: String, newName: String): Boolean {
        val b = false
        if (oldName == newName) {
            return b
        }
        val f = File(oldName)
        return if (!f.exists()) {
            b
        } else f.renameTo(File(newName))
    }

    /**
     * 删除文件
     *
     * @param filePath
     */
    fun deleteFileWithPath(filePath: String?): Boolean {
        val checker = SecurityManager()
        val f = File(filePath)
        checker.checkDelete(filePath)
        if (f.isFile) {
            f.delete()
            return true
        }
        return false
    }

    /**
     * 列出root目录下所有子目录
     *
     * @return 绝对路径
     */
    fun listPath(root: String?): List<String>? {
        val allDir: MutableList<String> =
            ArrayList()
        val checker = SecurityManager()
        val path = File(root)
        checker.checkRead(root)
        if (path.isDirectory) {
            for (f in path.listFiles()) {
                if (f.isDirectory) {
                    allDir.add(f.absolutePath)
                }
            }
        }
        return allDir
    }

    enum class PathStatus {
        SUCCESS, EXITS, ERROR
    }

    /**
     * 获取父目录路径
     *
     * @param path
     * @return
     */
    fun getParentDirPath(path: String?): String? {
        var parentDirPath = ""
        if (path != null && path.isNotEmpty()) {
            val lastIndex = path.lastIndexOf("/")
            if (lastIndex > 0) {
                parentDirPath = path.substring(0, lastIndex)
            } else if (lastIndex == 0) {
                parentDirPath = "/"
            }
        }
        return parentDirPath
    }

    /**
     * 获取父目录名
     *
     * @param path
     * @return
     */
    fun getParentDirName(path: String): String? {
        var parentDirName = ""
        val lastIndex = path.lastIndexOf("/")
        if (lastIndex > 0) {
            val parentPath = path.substring(0, lastIndex)
            val secondIndex = parentPath.lastIndexOf("/")
            parentDirName = parentPath.substring(secondIndex + 1, parentPath.length)
        } else if (lastIndex == 0) {
            parentDirName = "/"
        }
        return parentDirName
    }

    private val ANDROID_SECURE = "/mnt/sdcard/.android_secure"

    fun isNormalFile(fullName: String): Boolean {
        return fullName != ANDROID_SECURE
    }

    fun isSameCard(
        context: Context?,
        pathFrom: String,
        pathTo: String
    ): Boolean {
        val fromVolume = getRootPath(pathFrom)
        return !StringUtils.isEmpty(fromVolume) && !StringUtils.isEmpty(
                pathTo
            ) && pathTo.startsWith(fromVolume!!)
    }

    fun isLocalDevice(rootPath: String): Boolean {
        return rootPath.startsWith("/mnt/sdcard") || rootPath.startsWith("/mnt/usb") || rootPath.startsWith(
                "/storage/emulated"
            )
    }

    fun getRootPath(original: String): String? {
        val mountList: List<String>? = MountHelper.getMountPathList()
        val rootVolume: String
        if (mountList?.size!! > 0) {
            for (mountPath in mountList) {
                if (original.startsWith(mountPath)) {
                    rootVolume = mountPath
                    return rootVolume
                }
            }
        }
        return null
    }

    fun getModifyTimeStr(file: File?): String? {
        if (file == null || !file.exists()) {
            return ""
        }
        val date = Date(file.lastModified())
        val df = SimpleDateFormat("yyyy-MM-dd ")
        return df.format(date)
    }

    fun getModifyTimeLong(path: String?): Long {
        try {
            if (!StringUtils.isEmpty("path")) {
                val file = File(path)
                if (file != null && file.exists()) {
                    return file.lastModified()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return 0
    }

    /**
     * 访问资源文件
     *
     * @param context
     * @param assetsFileName
     * @param targetPath
     * @return
     */
    fun CopyAssetData(
        context: Context,
        assetsFileName: String?,
        targetPath: String?
    ): Boolean {
        val file = File(targetPath)
        if (file.exists()) {
            file.delete()
        }
        try {
            val inputStream = context.assets.open(assetsFileName!!)
            val output = FileOutputStream(targetPath)
            val buf = ByteArray(10240)
            var count = 0
            while (inputStream.read(buf).also { count = it } > 0) {
                output.write(buf, 0, count)
            }
            output.close()
            inputStream.close()
        } catch (e: IOException) {
            e.printStackTrace()
            Log.e("copyfail", e.toString())
            return false
        }
        return true
    }
}
