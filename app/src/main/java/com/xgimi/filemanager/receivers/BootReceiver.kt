package com.xgimi.filemanager.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.xgimi.filemanager.services.DeviceLoadService
import com.xgimi.system.GmSystemManager.getProp

/**
 *    author : joker.peng
 *    e-mail : joker.peng@xgimi.com
 *    date   : 2020/8/19 17:44
 *    desc   :
 */
class BootReceiver : BroadcastReceiver() {
    companion object {
        private val TAG = "BootReceiver"
        const val BOOT_PROP = "mstar.com.xgimi.boot"
        const val START_TIME = "start_time"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        val mIntent = Intent(context, DeviceLoadService::class.java)
        if (Intent.ACTION_MEDIA_MOUNTED == action) {
            Log.e(TAG, "ACTION_MEDIA_MOUNTED flag:" + intent.flags)
            val path = intent.data!!.path
            Log.e(TAG, "ACTION_MEDIA_MOUNTED path:$path")
            Log.e("BootReceiver", "ACTION_MEDIA_MOUNTED$path")
            mIntent.putExtra("MountPath", path)
            mIntent.action = DeviceLoadService.ONMOUNT
            Log.e(
                "BootReceiver",
                "系统属性" + getProp("persist.xgimi.filemanager.udisk_dialog", false, false)
            )
            if (!getProp("persist.xgimi.filemanager.udisk_dialog", false, false)) {
                showPopup(context.applicationContext, path)
            }
        } else if (Intent.ACTION_MEDIA_EJECT == action) {
            val path = intent.data!!.path
            Log.e(TAG, "ACTION_MEDIA_EJECT flag:" + intent.flags)
            Log.d(TAG, "ACTION_MEDIA_EJECT path:$path")
            Log.e("BootReceiver", "ACTION_MEDIA_EJECT$path")
            mIntent.putExtra("MountPath", path)
            mIntent.action = DeviceLoadService.ONUNMOUNT
        }
        context.startService(mIntent)
    }

    private fun showPopup(context: Context, path: String?) {
//        UsbPopupHelper.getInstance(context).showPopup(path)
    }

    private fun closePopup(context: Context, path: String) {
//        UsbPopupHelper.getInstance(context).hidePopup(path)
    }
}