package com.xgimi.filemanager.utils

import android.app.Activity
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.widget.Toast
import com.blankj.utilcode.util.StringUtils
import com.google.gson.Gson
import com.xgimi.filemanager.R
import com.xgimi.filemanager.bean.BaseData
import com.xgimi.filemanager.bean.MusicFileInfo
import com.xgimi.filemanager.constants.Constants.SAMBA_PATH
import com.xgimi.filemanager.enums.FileCategory
import com.xgimi.filemanager.helper.FileOperationHelper
import com.xgimi.filemanager.helper.IntentBuilder
import com.xgimi.filemanager.helper.ResourceHelper
import java.util.*

/**
 *    author : joker.peng
 *    e-mail : joker.peng@xgimi.com
 *    date   : 2020/8/19 18:04
 *    desc   :
 */
object MediaOpenUtil {
    private val TAG = "MediaOpenUtils"
    val IMAGE_POSITION = "STATE_POSITION"
    val HTTP_IMAGE_PATH = "image_urls_path_lee_other_bbc"
    val IMAGE_PATH = "image_urls_path_lee"
    private val DLNA_NAME = "dlan-name-list"
    private val SAMBA_LRC_HOME = SAMBA_PATH
    private val SAMBA_SUBTITLE_HOME: String = SAMBA_PATH

    fun playPicture(context: Context, path: String?) {
        try {
            val intent = Intent()
            if (AppManager.isInstallAPK(context, "com.xgimi.image.browser")) {
                intent.component = ComponentName(
                    "com.xgimi.image.browser",
                    "com.xgimi.image.browser.activity.ImagePlayActivity"
                )
            } else {
                intent.component = ComponentName(
                    "com.xgimi.gimiplayer",
                    "com.xgimi.gimiplayer.activity.ImagePlayActivity"
                )
            }
            intent.putExtra(IMAGE_PATH, path)
            if (context !is Activity) {
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, R.string.open_fail, Toast.LENGTH_SHORT).show()
        }
    }

    fun playPictureList(
        context: Context,
        urlList: ArrayList<String?>?,
        position: Int
    ) {
        try {
            val intent = Intent()
            if (AppManager.isInstallAPK(context, "com.xgimi.image.browser")) {
                intent.component = ComponentName(
                    "com.xgimi.image.browser",
                    "com.xgimi.image.browser.activity.ImagePlayActivity"
                )
            } else {
                intent.component = ComponentName(
                    "com.xgimi.gimiplayer",
                    "com.xgimi.gimiplayer.activity.ImagePlayActivity"
                )
                // intent.setComponent(
//         new ComponentName("com.xgimi.filemanager", "com.xgimi.filemanager.test.GlideActivity"));
            }
            intent.putExtra(HTTP_IMAGE_PATH, urlList)
            intent.putExtra(IMAGE_POSITION, position)
            //            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, R.string.open_fail, Toast.LENGTH_SHORT).show()
        }
    }

    fun playPictureList(
        context: Context?,
        fileLists: List<BaseData>?,
        current: BaseData
    ) {
        val list = ArrayList<String?>()
        var picturePosition = 0
        if (fileLists == null || fileLists.size == 0) {
            list.add(current.path)
        } else {
            for (baseData in fileLists) {
                try {
                    if (baseData.category == FileCategory.Picture.ordinal) {
                        val filePath = baseData.path!!.trim { it <= ' ' }
                        list.add(filePath)
                        if (baseData.equals(current)) {
                            picturePosition = list.size - 1
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
        playPictureList(context!!, list, picturePosition)
    }

    fun playMusicList(
        context: Context,
        fileLists: List<BaseData>?,
        current: BaseData
    ) {
        var helper: FileOperationHelper? = null
        if (!hasXgimiFm(context)) {
            IntentBuilder.viewFile(context, current.path!!)
        } else {
            val playlist = MusicFileInfo()
            var musicPosition = 0
            if (fileLists == null || fileLists.isEmpty()) {
                val filePath = current.path!!.trim { it <= ' ' }
                val fileName = current.name!!.trim { it <= ' ' }
                val musicInfo: MusicFileInfo.MusicDataInfo =
                    MusicFileInfo.MusicDataInfo(0, fileName, filePath)
                playlist.data?.add(musicInfo)
            } else {
                if (current.shareItem != null) {
                    helper = FileOperationHelper(context, null)
                }
                for (baseData in fileLists) {
                    try {
                        if (baseData.category == FileCategory.Music.ordinal) {
                            val filePath = baseData.path!!.trim { it <= ' ' }
                            val fileName = baseData.name!!.trim { it <= ' ' }
                            val musicInfo: MusicFileInfo.MusicDataInfo =
                                MusicFileInfo.MusicDataInfo(0, fileName, filePath)
                            playlist.data?.add(musicInfo)
                            if (baseData == current) {
                                musicPosition = playlist.data?.size!! - 1
                            }
                        } else if (current.shareItem != null && baseData.name!!.contains("lrc") && helper != null) {
                            helper.pasteSambaSubtitle(baseData.shareItem!!, SAMBA_LRC_HOME)
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
            playMusicList(context, playlist, musicPosition)
        }
    }

    fun playMusicList(context: Context, play_url: MusicFileInfo, position: Int) {
        val intent = Intent()
        val bundle = Bundle()
        val gson = Gson()
        bundle.putString("play_url", gson.toJson(play_url))
        bundle.putInt("position", position)
        intent.putExtra("playList", bundle)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        Log.e(
            "playMusicList",
            "playMusicList>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>size:" + play_url.data?.size.toString() + "position" + position
        )
        try {
            intent.component = ComponentName(
                "com.xgimi.doubanfm",
                "com.xgimi.doubanfm.localplayer.LocalMusicActivity"
            )
            context.startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
            try {
                intent.component =
                    ComponentName("com.xgimi.doubanfm", "com.xgimi.doubanfm.activity.MusicActivity")
                context.startActivity(intent)
            } catch (e1: Exception) {
                e1.printStackTrace()
                Toast.makeText(context, R.string.open_fail, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private val URL_LIST = "badiu-daye-other-wocao-list"

    /**
     * 打开视频同时保存记录信息
     *
     * @param context
     * @param bd
     */
    fun playVideo(
        context: Context,
        bd: BaseData,
        subtitlePath: String
    ) {
        val videoRecord: BaseData? = ResourceHelper.checkRecordExist(bd.path!!)
        bd.lastPlayTime = System.currentTimeMillis()
        playVideo(
            context, bd.path, subtitlePath,
            if (videoRecord == null || videoRecord.lastPlayPosition == videoRecord.duration) 0 else videoRecord.lastPlayPosition
        )
    }

    private fun playVideo(
        context: Context,
        path: String?,
        subtitlePath: String,
        time: Long
    ) {
        try {
            val intent = Intent()
            intent.component = ComponentName(
                "com.xgimi.gimiplayer",
                "com.xgimi.gimiplayer.activity.VideoPlayerActivity"
            )
            intent.putExtra("video-path", path)
            intent.putExtra(
                "subtitle-path",
                if (StringUtils.isEmpty(subtitlePath)) path else subtitlePath
            )
            intent.putExtra("start-time", time)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, R.string.open_fail, Toast.LENGTH_SHORT).show()
        }
    }

    fun playVideoList(
        context: Context?,
        fileLists: List<BaseData>?,
        current: BaseData
    ) {
        val list = ArrayList<String?>()
        val nameList = ArrayList<String?>()
        var helper: FileOperationHelper? = null
        var videoPosition = 0
        if (current.shareItem != null) {
            try {
                helper = FileOperationHelper(context, null)
                val videoName = current.shareItem!!.name
                    .substring(0, current.shareItem!!.name.lastIndexOf('.'))
                for (data in fileLists!!) {
                    val fileName = data.shareItem!!.name
                    if (fileName.contains(videoName) &&
                        fileName.endsWith("aas") || fileName.endsWith("srt")
                        || fileName.endsWith("idx")
                        || fileName.endsWith("ssa")
                        || fileName.endsWith("ass")
                        || fileName.endsWith("smi")
                    ) {
                        helper.pasteSambaSubtitle(data.shareItem!!, SAMBA_SUBTITLE_HOME)
                    }
                }
                //                helper = new FileOperationHelper(context, null);
//                helper.pasteSambaSubtitle(current.getShareItem(), SAMBA_SUBTITLE_HOME);
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        if (fileLists == null || fileLists.size == 0) {
            list.add(current.path)
            nameList.add(current.name)
        } else {
            for (baseData in fileLists) {
                try {
                    if (baseData.category == FileCategory.Video.ordinal) {
                        val filePath = baseData.path!!.trim { it <= ' ' }
                        val name = baseData.name
                        list.add(filePath)
                        nameList.add(name)
                        if (baseData.equals(current)) {
                            videoPosition = list.size - 1
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
        playVideoList(context!!, list, nameList, videoPosition)
    }

    fun playVideoList(
        context: Context,
        urls: ArrayList<String?>,
        names: ArrayList<String?>?,
        position: Int
    ) {
        try { /* 新建一个Intent对象 */
            val intent = Intent()
            intent.putStringArrayListExtra(URL_LIST, urls)
            intent.putStringArrayListExtra(DLNA_NAME, names)
            /* 指定intent要启动的类 */intent.component = ComponentName(
                "com.xgimi.gimiplayer",
                "com.xgimi.gimiplayer.activity.VideoPlayerActivity"
            )
            /* 启动一个新的Activity */intent.putExtra("position", position)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            for (i in urls.indices) {
                Log.d(TAG, "viewUrl:$urls")
            }
            Log.d(TAG, "position:$position")
            context.startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, R.string.open_fail, Toast.LENGTH_SHORT).show()
        }
    }

    fun hasXgimiFm(context: Context): Boolean {
        var hasSuiXinTing = true
        try {
            val pm = context.packageManager
            pm.getPackageInfo("com.xgimi.doubanfm", PackageManager.GET_ACTIVITIES)
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
            hasSuiXinTing = false
        }
        return hasSuiXinTing
    }
}
