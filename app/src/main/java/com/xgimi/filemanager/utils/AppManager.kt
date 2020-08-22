package com.xgimi.filemanager.utils

import android.app.ActivityManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
import android.net.Uri
import android.util.Log

/**
 *    author : joker.peng
 *    e-mail : joker.peng@xgimi.com
 *    date   : 2020/8/19 18:06
 *    desc   :
 */
object AppManager {

    /**
     * 打开app
     */
    val MSG_OPEN_TYPE_APP = 0
    /**
     * 发送广播
     */
    val MSG_OPEN_TYPE_BROADCAST = 1
    /**
     * 打开activity
     */
    val MSG_OPEN_TYPE_ACTIVITY = 2
    /**
     * 发送toast
     */
    val MSG_OPEN_TYPE_TOAST = 3
    /**
     * 启动服务
     */
    val MSG_OPEN_TYPE_SERVICE = 4
    /**
     * 启动爱奇艺
     */
    val MSG_OPEN_TYPE_IQIYI = 5

    /**
     * 获取app的图标
     *
     * @param context
     * @param package_name
     * @return
     */
    fun getAppIcon(context: Context, package_name: String?): Drawable? {
        val packageManager = context.packageManager
        var drawable: Drawable? = null
        try {
            val info =
                packageManager.getApplicationInfo(package_name, PackageManager.GET_META_DATA)
            drawable = info.loadIcon(packageManager)
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }
        return drawable
    }

    /**
     * 是否安装指定应用
     *
     * @param context     上下文
     * @param packageName 包名
     * @return
     */
    fun isInstallAPK(
        context: Context,
        packageName: String
    ): Boolean {
        var result = false
        val packageInfoList =
            context.packageManager.getInstalledPackages(0)
        for (info in packageInfoList) {
            if (info.packageName == packageName) {
                result = true
                break
            }
        }
        return result
    }

    /**
     * 根据包名启动app
     *
     * @param context     The Context
     * @param packageName 包名
     */
    fun launchAPP(context: Context, packageName: String?) {
        try {
            val intent = context.packageManager.getLaunchIntentForPackage(packageName!!)
            intent!!.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * 启动指定activity
     *
     * @param context
     * @param packageName
     * @param activityName
     */
    fun launchActivity(context: Context, packageName: String?, activityName: String?): Boolean {
        try {
            val cn = ComponentName(packageName!!, activityName!!)
            val intent = Intent()
            intent.component = cn
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        } catch (e: Exception) {
            Log.i("AppsManager", "e=" + e.message)
            e.printStackTrace()
            return false
        }
        return true
    }

    fun getApkIconInLauncher(
        context: Context,
        pkgname: String
    ): Bitmap? {
        Log.i("JIcon", "-------------pkgname=$pkgname")
        val contentResolver = context.contentResolver
        val uri =
            Uri.parse("content://com.xgimi.home.provider.commonappprovider/query_app")
        val cursor = contentResolver.query(uri, null, pkgname, null, null)
        var icon: ByteArray? = null
        while (cursor != null && cursor.moveToNext()) {
            icon = cursor.getBlob(cursor.getColumnIndex("icon"))
        }
        cursor!!.close()
        return if (icon != null && icon.size > 0) BitmapFactory.decodeByteArray(
            icon,
            0,
            icon.size
        ) else null
    }

    fun isTopApp(context: Context, pkgName: String): Boolean {
        try {
            val am =
                context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            val list = am.getRunningTasks(100)
            if (list != null && list.size > 0 && pkgName == list[0].topActivity!!.packageName) {
                return true
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return false
    }

    /**
     * intent有效性检查
     *
     * @param context
     * @param intent
     * @return
     */
    fun isIntentAvailable(
        context: Context,
        intent: Intent?,
        mode: Int
    ): Boolean {
        val packageManager = context.applicationContext.packageManager
        val list: List<ResolveInfo>?
        list = if (MSG_OPEN_TYPE_BROADCAST == mode) {
            packageManager.queryBroadcastReceivers(
                intent,
                PackageManager.MATCH_DEFAULT_ONLY
            )
        } else if (MSG_OPEN_TYPE_ACTIVITY == mode) {
            packageManager.queryIntentActivities(
                intent,
                PackageManager.MATCH_DEFAULT_ONLY
            )
        } else if (MSG_OPEN_TYPE_SERVICE == mode) {
            packageManager.queryIntentServices(
                intent,
                PackageManager.MATCH_DEFAULT_ONLY
            )
        } else {
            return true
        }
        return list != null && list.size > 0
    }
}
