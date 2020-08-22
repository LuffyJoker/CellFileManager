package com.xgimi.filemanager

import android.content.Context
import androidx.lifecycle.LiveData
import com.xgimi.filemanager.bean.DeviceInfo
import com.xgimi.filemanager.interfaces.IDeviceInfoRepo
import com.xgimi.filemanager.utils.DeviceUtil

/**
 *    author : joker.peng
 *    e-mail : joker.peng@xgimi.com
 *    date   : 2020/8/7 13:56
 *    desc   :
 */
class DeviceInfoRepoImpl : IDeviceInfoRepo {
    override fun getDeviceList(context: Context): LiveData<MutableList<DeviceInfo>> {
        return DeviceUtil.getDeviceInfoCache()
    }
}