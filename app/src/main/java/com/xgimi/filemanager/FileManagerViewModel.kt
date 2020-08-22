package com.xgimi.filemanager

import androidx.lifecycle.LiveData
import com.xgimi.filemanager.bean.DeviceInfo

/**
 *    author : joker.peng
 *    e-mail : joker.peng@xgimi.com
 *    date   : 2020/8/7 13:53
 *    desc   :
 */
class FileManagerViewModel : BaseViewModel<DeviceInfoRepoImpl>() {

    override val mRepository: DeviceInfoRepoImpl by lazy {
        DeviceInfoRepoImpl()
    }

    private val installedApkList = mRepository.getDeviceList(getApplication())

    fun getDeviceList(): LiveData<MutableList<DeviceInfo>> {
        return installedApkList
    }
}