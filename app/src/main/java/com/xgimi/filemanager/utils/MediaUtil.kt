package com.xgimi.filemanager.utils

import android.content.Context
import android.database.Cursor
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.media.MediaScannerConnection
import android.media.MediaScannerConnection.MediaScannerConnectionClient
import android.media.ThumbnailUtils
import android.net.Uri
import android.os.SystemClock
import android.provider.MediaStore
import com.blankj.utilcode.util.LogUtils
import com.blankj.utilcode.util.StringUtils
import com.xgimi.filemanager.bean.MediaEntity
import java.io.File
import java.io.FileOutputStream
import java.util.*
import java.util.concurrent.Executors
import java.util.concurrent.Semaphore

/**
 *    author : joker.peng
 *    e-mail : joker.peng@xgimi.com
 *    date   : 2020/8/17 16:18
 *    desc   :
 */
object MediaUtil {
    private val TAG = "MediaUtil"
    /**
     * 音乐列表缓存
     */
    private val mMusicCache: MutableList<MediaEntity> = ArrayList<MediaEntity>()
    /**
     * 音乐专辑封面缓存
     */
    private val mMusicThumbnailCache: MutableMap<String, String?> = Hashtable()
    /**
     * 音乐列表缓存是否发生改变
     */
    private var mMusicCacheIsChanged = true
    /**
     * 音乐专辑封面缓存是否发生改变
     */
    private var mMusicThumbnailCacheIsChanged = true
    /**
     * 扫描音乐线程池
     */
    private val mSingleThreadPool = Executors.newSingleThreadExecutor()
    /**
     * MediaScannerConnection是否连接上
     */
    private var onMediaScannerConnected = false
    /**
     * 带扫描路径缓存
     * 00029.mkv 00036.mkv 00037.mkv
     * 00000.mov 00004.mov
     */
    private val mScanPathCache: MutableList<String> =
        ArrayList()
    private var mediaScannerConnection: MediaScannerConnection? = null
    private val mSemaphore = Semaphore(1)

    /**
     * 获取指定音乐的专辑封面
     *
     * @param context 上下文
     * @param path    路径
     * @return 封面路径
     */
    private fun getMusicAlbumArt(
        context: Context,
        path: String
    ): String? {
        val entitys: List<MediaEntity>? =
            queryMusics(context, MediaStore.Audio.Media.DATA + "=?", arrayOf(path))
        return if (entitys != null && entitys.size > 0) {
            getAlbumArtByAlbunid(context, entitys[0].albumid)
        } else null
    }

    /**
     * 获取所以音乐封面图片
     *
     * @param context 上下文
     * @return 缓存音乐封面图片路径
     */
    private fun getAllMusicAlbumArt(context: Context): Map<String, String?>? {
        if (mMusicThumbnailCacheIsChanged) {
            val mediaEntityList = getAllMusicList(context)
            for (entity in mediaEntityList) {
                val albumArt = getAlbumArtByAlbunid(context, entity.albumid)
                LogUtils.i("entity.path=" + entity.path.toString() + "---albumArt=" + albumArt)
                if (!StringUtils.isEmpty(albumArt)) mMusicThumbnailCache[entity.path!!] = albumArt
            }
        }
        return mMusicThumbnailCache
    }

    /**
     * 根据路径获取缓存的音乐封面图片
     *
     * @param path 路径
     * @return 缓存封面路径
     */
    private fun getCacheAlbumArt(path: String): String? {
        return mMusicThumbnailCache[path]
    }

    /**
     * 查询音乐
     *
     * @param context       上下文
     * @param selection     查询条件
     * @param selectionArgs 查询条件
     * @return 音乐列表
     */
    private fun queryMusics(
        context: Context,
        selection: String?,
        selectionArgs: Array<String>?
    ): List<MediaEntity>? {
        var cursor: Cursor? = null
        var mediaList: MutableList<MediaEntity> =
            ArrayList<MediaEntity>()
        try {
            cursor = context.contentResolver.query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, arrayOf(
                    MediaStore.Audio.Media._ID,
                    MediaStore.Audio.Media.TITLE,
                    MediaStore.Audio.Media.DISPLAY_NAME,
                    MediaStore.Audio.Media.DURATION,
                    MediaStore.Audio.Media.ARTIST,
                    MediaStore.Audio.Media.DATA,
                    MediaStore.Audio.Media.SIZE,
                    MediaStore.Audio.Media.ALBUM_ID
                ), selection, selectionArgs, MediaStore.Audio.Media.DATE_ADDED + " DESC"
            )
            if (cursor != null) {
                mediaList = ArrayList<MediaEntity>()
                var mediaEntity: MediaEntity
                while (cursor.moveToNext()) {
                    mediaEntity = MediaEntity()
                    mediaEntity.id =
                        cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media._ID))
                    mediaEntity.title =
                        cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE))
                    mediaEntity.display_name =
                        cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DISPLAY_NAME))
                    mediaEntity.duration =
                        cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media.DURATION))
                    mediaEntity.size =
                        cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.SIZE))
                    mediaEntity.albumid =
                        cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID))
                    mediaEntity.artist =
                        cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST))
                    mediaEntity.path =
                        cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA))
                    LogUtils.d(TAG, mediaEntity.toString())
                    mediaList.add(mediaEntity)
                }
            } else {
                LogUtils.d(TAG, "The getMediaList cursor is null.")
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            cursor?.close()
        }
        return mediaList
    }

    /**
     * 删除指定音乐信息
     *
     * @param context
     * @param path
     */
    private fun removeMusic(
        context: Context,
        path: String
    ) {
        if (mMusicThumbnailCache.containsKey(path)) mMusicThumbnailCache.remove(path)
        context.contentResolver.delete(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            MediaStore.Audio.Media.DATA + "=?",
            arrayOf(path)
        )
    }

    /**
     * 获取所有音乐缓存
     *
     * @param context 上下文
     * @return 音乐列表
     */
    private fun getAllMusicList(context: Context): List<MediaEntity> {
        if (mMusicCacheIsChanged) {
            mMusicCache.clear()
            mMusicCache.addAll(queryMusics(context, null, null)!!)
            mMusicCacheIsChanged = false
        }
        return mMusicCache
    }

    /**
     * 根据id获取音乐封面
     *
     * @param context  上下文
     * @param album_id 专辑id
     * @return 专辑封面路径
     */
    private fun getAlbumArtByAlbunid(
        context: Context,
        album_id: Long
    ): String? {
        val mUriAlbums = "content://media/external/audio/albums"
        val projection = arrayOf("album_art")
        var cur: Cursor? = null
        var album_art: String? = null
        try {
            cur = context.contentResolver.query(
                Uri.parse("$mUriAlbums/$album_id"),
                projection, null, null, null
            )
            if (cur!!.count > 0 && cur.columnCount > 0) {
                cur.moveToNext()
                album_art = cur.getString(0)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            cur?.close()
        }
        return album_art
    }

    /**
     * 扫描指定路径的音乐文件
     *
     * @param context 上下文
     * @param path    路径
     */
    @Synchronized
    private fun scanMusicFile(
        context: Context,
        path: String
    ) {
        synchronized(onMediaScannerConnected) {
            if (mediaScannerConnection == null) {
                mediaScannerConnection = MediaScannerConnection(context, client)
                mediaScannerConnection!!.connect()
            }
            if (!onMediaScannerConnected) {
                mScanPathCache.add(path)
            } else startScanMusicfiles(path)
        }
    }

    private val client: MediaScannerConnectionClient = object : MediaScannerConnectionClient {
        /**
         * 当client和MediaScaner扫描完成后  进行关闭我们的连接
         * @param path 路径
         * @param uri 数据库uri
         */
        override fun onScanCompleted(path: String, uri: Uri) {
            LogUtils.i(TAG, "mPathText=$path---uri=$uri")
            mMusicCacheIsChanged = true
            mMusicThumbnailCacheIsChanged = true
        }

        /**
         * 连接成功就开始进行扫描。
         */
        override fun onMediaScannerConnected() {
            LogUtils.i(TAG, "mPathText=onMediaScannerConnected")
            synchronized(onMediaScannerConnected) {
                onMediaScannerConnected = true
                if (mScanPathCache.size > 0) for (path in mScanPathCache) startScanMusicfiles(
                    path
                )
                mScanPathCache.clear()
            }
        }
    }

    /**
     * 开始音乐扫描
     *
     * @param path 路径
     */
    @Synchronized
    private fun startScanMusicfiles(path: String) {
        mSingleThreadPool.execute { mediaScannerConnection!!.scanFile(path, "audio/mpeg") }
    }

    /**
     * 释放资源
     */
    private fun release() {
        if (mediaScannerConnection != null && mediaScannerConnection!!.isConnected) {
            mediaScannerConnection!!.disconnect()
            mediaScannerConnection = null
        }
    }

    var VIDEO_THUMBNAIL_PATH = "" //视频缩略图保存路径


    /**
     * 获取视频缩略图路径
     *
     * @param filePath
     * @return
     */
    fun getVideoThumbnailPath(filePath: String): String? {
        val cacheDir = getVideoThumbnailCacheDir(filePath)
        val file =
            File(cacheDir + File.separator + filePath.hashCode())
        if (file.exists()) {
            return file.path
        }
        var start = SystemClock.elapsedRealtime()
        val bitmap: Bitmap?
        try {
            bitmap = getVideoThumb(filePath)
            LogUtils.i(
                "JSize",
                (if (bitmap == null) "bitmap==null " else "bitmap!=null ") + " filePath=" + filePath
            )

            LogUtils.i(
                "JSize",
                "load video thumbnail time=" + (SystemClock.elapsedRealtime() - start)
            )
            start = SystemClock.elapsedRealtime()
            val savePath = saveBitmap(bitmap, cacheDir, filePath)
            LogUtils.i("JSize", "saveBitmap time=" + (SystemClock.elapsedRealtime() - start))
            bitmap?.recycle()
            System.gc()
            return savePath
        } catch (e: IllegalArgumentException) {
            e.printStackTrace()
        } catch (e: RuntimeException) {
            e.printStackTrace()
        }
        return null
    }

    /**
     * 获取视频文件截图
     *
     * @param path 视频文件的路径
     * @return Bitmap 返回获取的Bitmap
     */
    fun getVideoThumb(path: String): Bitmap? {
        return createVideoThumbnail(
            path,
            MediaStore.Images.Thumbnails.MICRO_KIND
        )
    }

    fun init() {}

    fun createVideoThumbnail(filePath: String, kind: Int): Bitmap? {
        if (StringUtils.isEmpty(filePath)) return null
        val file = File(filePath)
        if (!file.exists()) return null
        if (isProblemVideo(file.name)) return null
        var bitmap: Bitmap? = null
        var mmr: MediaMetadataRetriever? = null
        try {
            mSemaphore.acquire()
            LogUtils.i("MediaMetadata", "start load video thumbnail  filePath=$filePath")
            mmr = MediaMetadataRetriever()
            mmr.setDataSource(file.absolutePath)
            // 取得视频的长度(单位为秒)
            val duration = mmr.extractMetadata(
                MediaMetadataRetriever.METADATA_KEY_DURATION
            )
            if (!StringUtils.isEmpty(duration)) {
                var seconds = duration.toLong() / 1000
                seconds = (seconds / 2 + 2) * 1000 * 1000
                // 取得缩略图
                bitmap = mmr.getFrameAtTime(
                    seconds,
                    MediaMetadataRetriever.OPTION_CLOSEST_SYNC
                )
            } else {
                bitmap = mmr.frameAtTime
            }
        } catch (ex: IllegalArgumentException) {
            ex.printStackTrace()
            // Assume this is a corrupt video file
        } catch (e: InterruptedException) {
            e.printStackTrace()
        } catch (ex: RuntimeException) {
            ex.printStackTrace()
        } catch (ex: Exception) {
            ex.printStackTrace()
        } finally {
            try {
                mmr?.release()
            } catch (ex: RuntimeException) {
                ex.printStackTrace()
            }
        }
        LogUtils.i("MediaMetadata", "end load video thumbnail  filePath=$filePath")
        mSemaphore.release()
        if (bitmap == null) return null
        if (kind == MediaStore.Images.Thumbnails.MINI_KIND) { // Scale down the bitmap if it's too large.
            val width = bitmap.width
            val height = bitmap.height
            val max = Math.max(width, height)
            if (max > 512) {
                val scale = 512f / max
                val w = Math.round(scale * width)
                val h = Math.round(scale * height)
                bitmap = Bitmap.createScaledBitmap(bitmap, w, h, true)
            }
        } else if (kind == MediaStore.Images.Thumbnails.MICRO_KIND) {
            bitmap = ThumbnailUtils.extractThumbnail(
                bitmap,
                96,
                96,
                ThumbnailUtils.OPTIONS_RECYCLE_INPUT
            )
        }
        return bitmap
    }

    private fun isProblemVideo(name: String): Boolean {
        return ".mts" == name.substring(name.lastIndexOf("."))
    }

    /**
     * 保存视频缩略图
     *
     * @param bitmap
     * @param name
     */
    private fun saveBitmap(
        bitmap: Bitmap?,
        cacheDir: String,
        name: String
    ): String? {
        if (bitmap == null) return null
        val file = File(cacheDir)
        if (file == null || !file.exists()) file.mkdirs()
        val f = File(file, name.hashCode().toString() + "")
        if (f.exists()) {
            f.delete()
        }
        var out: FileOutputStream? = null
        try {
            out = FileOutputStream(f)
            bitmap.compress(Bitmap.CompressFormat.PNG, 80, out)
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            try {
                if (out != null) {
                    out.flush()
                    out.close()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return if (f.exists()) {
            f.absolutePath
        } else null
    }

    fun getVideoThumbnailCacheDir(filepath: String): String {
        try {
            if (!StringUtils.isEmpty(filepath)) {
                val names =
                    filepath.split(File.separator.toRegex()).toTypedArray()
                if (names != null && names.size > 2) {
                    return if ("usb" == names[2] && names.size > 3) VIDEO_THUMBNAIL_PATH + File.separator + names[3] else VIDEO_THUMBNAIL_PATH + File.separator + names[2]
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return VIDEO_THUMBNAIL_PATH
    }

    fun removeVideoThumbnailCache(mountPath: String) {
        LogUtils.i("delSourceNew", "removeVideoThumbnailCache")
        if (!StringUtils.isEmpty(mountPath) && mountPath.contains(File.separator)) {
            val file = File(
                VIDEO_THUMBNAIL_PATH + File.separator + mountPath.substring(
                    mountPath.lastIndexOf(
                        File.separator
                    )
                )
            )
            if (file.exists()) {
                LogUtils.i("delSourceNew", "deleteDir")
                deleteDir(file)
            }
        }
    }

    fun checkeVideoThumbnail(mountlist: List<String>) {
        val file = File(VIDEO_THUMBNAIL_PATH)
        if (file.exists()) {
            val files = file.listFiles() ?: return
            for (f in files) {
                var exists = false
                for (i in mountlist.indices) {
                    LogUtils.i("mountlist=" + mountlist[i])
                    if (mountlist[i].endsWith(f.name)) {
                        exists = true
                        break
                    }
                }
                if (!exists) deleteDir(f)
            }
        }
    }

    private fun deleteDir(file: File) {
        if (file.exists()) { //判断文件是否存在
            if (file.isFile) { //判断是否是文件
                file.delete() //删除文件
            } else if (file.isDirectory) { //否则如果它是一个目录
                val files = file.listFiles() //声明目录下所有的文件 files[];
                for (i in files.indices) { //遍历目录下所有的文件
                    deleteDir(files[i]) //把每个文件用这个方法进行迭代
                }
                file.delete() //删除文件夹
            }
        } else {
            println("所删除的文件不存在")
        }
    }
}
