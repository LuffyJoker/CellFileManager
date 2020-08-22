package com.xgimi.filemanager.interfaces

import android.content.Context
import androidx.lifecycle.LiveData
import com.xgimi.filemanager.bean.DeviceInfo

/**
 *    author : joker.peng
 *    e-mail : joker.peng@xgimi.com
 *    date   : 2020/8/7 13:55
 *    desc   :
 */
interface IDeviceInfoRepo {
    fun getDeviceList(context: Context): LiveData<MutableList<DeviceInfo>>
}