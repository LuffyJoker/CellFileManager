package com.xgimi.filemanager.utils

import android.content.Context
import android.net.Uri
import android.os.Build
import android.preference.PreferenceManager
import android.provider.DocumentsContract
import android.util.Log
import androidx.documentfile.provider.DocumentFile
import java.io.*
import java.util.*

/**
 *    author : joker.peng
 *    e-mail : joker.peng@xgimi.com
 *    date   : 2020/8/10 16:02
 *    desc   :
 */
object DocumentsUtil {
    private val TAG: String = DocumentsUtil::class.java.simpleName

    val OPEN_DOCUMENT_TREE_CODE = 8000

    private val sExtSdCardPaths: MutableList<String> = ArrayList()

    fun cleanCache() {
        sExtSdCardPaths.clear()
    }

    /**
     * Get a list of external SD card paths. (Kitkat or higher.)
     *
     * @return A list of external SD card paths.
     */
    private fun getExtSdCardPaths(context: Context): Array<String> {
        if (sExtSdCardPaths.size > 0) {
            return sExtSdCardPaths.toTypedArray()
        }
        for (file in context.getExternalFilesDirs("external")) {
            if (file != null && file != context.getExternalFilesDir("external")) {
                val index = file.absolutePath.lastIndexOf("/Android/data")
                if (index < 0) {
                    Log.w(
                        TAG,
                        "Unexpected external file dir: " + file.absolutePath
                    )
                } else {
                    var path = file.absolutePath.substring(0, index)
                    try {
                        path = File(path).canonicalPath
                    } catch (e: IOException) { // Keep non-canonical path.
                    }
                    sExtSdCardPaths.add(path)
                }
            }
        }
        if (sExtSdCardPaths.isEmpty()) {
            sExtSdCardPaths.add("/storage/sdcard")
        }
        return sExtSdCardPaths.toTypedArray()
    }

    /**
     * Determine the main folder of the external SD card containing the given file.
     *
     * @param file the file.
     * @return The main folder of the external SD card containing this file, if the file is on an SD
     * card. Otherwise,
     * null is returned.
     */
    fun getExtSdCardFolder(
        context: Context,
        file: File
    ): String? {
        return try {
            getExtSdCardFolder(context, file.canonicalPath)
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }

    fun getExtSdCardFolder(
        context: Context,
        path: String
    ): String? {
        val extSdPaths = getExtSdCardPaths(context)
        for (i in extSdPaths.indices) {
            if (path.startsWith(extSdPaths[i])) {
                return extSdPaths[i]
            }
        }
        return null
    }

    /**
     * Determine if a file is on external sd card. (Kitkat or higher.)
     *
     * @param file The file.
     * @return true if on external sd card.
     */
    fun isOnExtSdCard(c: Context, file: File): Boolean {
        return getExtSdCardFolder(c, file) != null
    }

    /**
     * Get a DocumentFile corresponding to the given file (for writing on ExtSdCard on Android 5).
     * If the file is not
     * existing, it is created.
     *
     * @param file The file.
     * @return The DocumentFile
     */
    fun getDocumentFile(
        context: Context,
        file: File,
        needCreate: Boolean
    ): DocumentFile? {
        return getDocumentFile(context, file, needCreate, true)
    }

    fun getDocumentFile(
        context: Context,
        file: File,
        needCreate: Boolean,
        isDirectory: Boolean
    ): DocumentFile? {
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT) {
            return DocumentFile.fromFile(file)
        }
        val baseFolder = getExtSdCardFolder(context, file)
        var originalDirectory = false
        if (baseFolder == null) {
            return null
        }
        var relativePath: String? = null
        try {
            val fullPath = file.canonicalPath
            if (baseFolder != fullPath) {
                relativePath = fullPath.substring(baseFolder.length + 1)
            } else {
                originalDirectory = true
            }
        } catch (e: IOException) {
            return null
        } catch (f: Exception) {
            originalDirectory = true
            f.printStackTrace()
            //continue
        }
        val `as` = PreferenceManager.getDefaultSharedPreferences(context).getString(
            baseFolder,
            null
        )
        var treeUri: Uri? = null
        if (`as` != null) {
            treeUri = Uri.parse(`as`)
        }
        if (treeUri == null) {
            return null
        }
        // start with root of SD card and then parse through document tree.
        var document: DocumentFile? = DocumentFile.fromTreeUri(context, treeUri) ?: return null
        if (originalDirectory) {
            return document
        }
        val parts = relativePath!!.split("/".toRegex()).toTypedArray()
        for (i in parts.indices) {
            var nextDocument = document!!.findFile(parts[i])
            if (nextDocument == null) {
                nextDocument = if (needCreate) {
                    if (i < parts.size - 1 || isDirectory) {
                        document.createDirectory(parts[i])
                    } else {
                        document.createFile("*", parts[i])
                    }
                } else {
                    return null
                }
            }
            document = nextDocument
        }
        return document
    }

    fun mkdirs(context: Context?, dir: File): Boolean {
        var res = dir.mkdirs()
        if (!res) {
            if (isOnExtSdCard(context!!, dir)) {
                val documentFile: DocumentFile? = getDocumentFile(context, dir, true, true)
                res = documentFile != null && documentFile.canWrite()
            }
        }
        return res
    }

    @Throws(IOException::class)
    fun createNewFile(context: Context?, file: File): Boolean {
        var res = file.createNewFile()
        if (isAndroidP() && !res) {
            if (isOnExtSdCard(context!!, file)) {
                val documentFile: DocumentFile? = getDocumentFile(context, file, true, false)
                res = documentFile != null && documentFile.canWrite()
            }
        }
        return res
    }

    fun delete(context: Context?, file: File): Boolean {
        var ret = file.delete()
        if (!ret && DocumentsUtil.isOnExtSdCard(context!!, file)) {
            val f: DocumentFile? = DocumentsUtil.getDocumentFile(
                context,
                file,
                false
            )
            if (f != null) {
                ret = f.delete()
            }
        }
        return ret
    }

    private fun canWrite(file: File): Boolean {
        var res = file.exists() && file.canWrite()
        if (!res && !file.exists()) {
            try {
                res = if (!file.isDirectory) {
                    file.createNewFile() && file.delete()
                } else {
                    file.mkdirs() && file.delete()
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
        return res
    }

    fun canWrite(context: Context?, file: File): Boolean {
        var res = canWrite(file)
        if (isAndroidP() && !res && DocumentsUtil.isOnExtSdCard(context!!, file)) {
            val documentFile: DocumentFile? =
                DocumentsUtil.getDocumentFile(
                    context,
                    file,
                    false
                )
            res = documentFile != null && documentFile.canWrite()
        }
        return res
    }

    fun renameTo(
        context: Context,
        src: File,
        dest: File
    ): Boolean {
        var res = src.renameTo(dest)
        if (!res && isOnExtSdCard(context, dest)) {
            val srcDoc: DocumentFile?
            srcDoc = if (isOnExtSdCard(context, src)) {
                getDocumentFile(context, src, false)
            } else {
                DocumentFile.fromFile(src)
            }
            val destDoc =
                getDocumentFile(context, dest.parentFile, true, src.isDirectory)
            if (srcDoc != null && destDoc != null) {
                try {
                    if (src.parent == dest.parent) {
                        res = DocumentsContract.renameDocument(
                            context.contentResolver, srcDoc.uri,
                            dest.name
                        ) != null
                        if (!res) {
                            res = dest.exists()
                        }
                    } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        res = DocumentsContract.moveDocument(
                            context.contentResolver,
                            srcDoc.uri,
                            srcDoc.parentFile!!.uri,
                            destDoc.uri
                        ) != null
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
        return res
    }

    fun getInputStream(context: Context, destFile: File): InputStream? {
        var `in`: InputStream? = null
        try {
            if (!canWrite(destFile) && isOnExtSdCard(context, destFile)) {
                val file: DocumentFile? =
                    DocumentsUtil.getDocumentFile(
                        context,
                        destFile,
                        false
                    )
                if (file != null && file.canWrite()) {
                    `in` = context.contentResolver.openInputStream(file.uri)
                }
            } else {
                `in` = FileInputStream(destFile)
            }
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        }
        return `in`
    }

    fun getOutputStream(
        context: Context,
        destFile: File
    ): OutputStream? {
        var out: OutputStream? = null
        try {
            if (!canWrite(destFile) && isOnExtSdCard(context, destFile)) {
                val file: DocumentFile? =
                    DocumentsUtil.getDocumentFile(
                        context,
                        destFile,
                        false
                    )
                if (file != null && file.canWrite()) {
                    out = context.contentResolver.openOutputStream(file.uri)
                }
            } else {
                out = FileOutputStream(destFile)
            }
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        }
        return out
    }

    fun saveTreeUri(
        context: Context?,
        rootPath: String,
        uri: Uri
    ): Boolean {
        val file = DocumentFile.fromTreeUri(context!!, uri)
        if (file != null && file.canWrite()) {
            val perf = PreferenceManager.getDefaultSharedPreferences(context)
            perf.edit().putString(rootPath, uri.toString()).apply()
            return true
        } else {
            Log.e(TAG, "no write permission: $rootPath")
        }
        return false
    }

    fun checkWritableRootPath(
        context: Context?, rootPath: String?
    ): Boolean {
        val root = File(rootPath)
        val res = root.canWrite()
        return if (isAndroidP() && !res) {
            if (isOnExtSdCard(context!!, root)) {
                val documentFile: DocumentFile? = getDocumentFile(context, root, false)
                documentFile == null || !documentFile.canWrite()
            } else {
                val perf = PreferenceManager.getDefaultSharedPreferences(context)
                val documentUri = perf.getString(rootPath, "")
                if (documentUri == null || documentUri.isEmpty()) {
                    true
                } else {
                    val file = DocumentFile.fromTreeUri(context!!, Uri.parse(documentUri))
                    !(file != null && file.canWrite())
                }
            }
        } else res
    }

    fun isAndroidP(): Boolean {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.P
    }
}