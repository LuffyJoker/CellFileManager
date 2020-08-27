package com.xgimi.filemanager.samba

import com.xgimi.filemanager.bean.BaseData
import com.xgimi.filemanager.exceptions.ShareException
import com.xgimi.filemanager.helper.ResourceHelper
import com.xgimi.samba.ShareItem
import com.xgimi.samba.SmbDevice
import com.xgimi.samba.bean.MethodTime
import com.xgimi.samba.core.ShareClient
import com.xgimi.samba.hpptd.NanoStreamer
import rx.Observable
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import java.net.ConnectException
import java.util.*
import java.util.concurrent.TimeoutException

/**
 *    author : joker.peng
 *    e-mail : joker.peng@xgimi.com
 *    date   : 2020/8/24 16:45
 *    desc   :
 */
object ShareClientController {

    const val STATUS_LOGON_FAILURE = -1001
    const val STATUS_CONNECT_FAILURE = -1002
    const val STATUS_CONNECT_TIMEOUT = -1003

    /**
     * 登陆 Samba
     * @param context
     * @param ip
     * @param user
     * @param passWorld
     * @return
     */
    fun signInSamba(ip: String?, user: String?, passWorld: String?): Observable<SmbDevice>? {
        return Observable.create(Observable.OnSubscribe<SmbDevice> { subscriber ->
            try {
                val shareClient = ShareClient(ip, user, passWorld, "")
                val name = shareClient.netBiosName
                val sambaDevice = SmbDevice(name ?: ip, ip, user, passWorld)
                ResourceHelper.saveSambaDeviceInfo(sambaDevice)
                subscriber.onNext(sambaDevice)
                shareClient.closeConnect()
            } catch (e: Throwable) {
                e.printStackTrace()
                if (subscriber.isUnsubscribed) {
                    return@OnSubscribe
                }
                if (e is ConnectException) {
                    subscriber.onError(ShareException(STATUS_CONNECT_FAILURE))
                } else if (e is TimeoutException) {
                    subscriber.onError(ShareException(STATUS_CONNECT_TIMEOUT))
                } else {
                    subscriber.onError(ShareException(STATUS_LOGON_FAILURE))
                }
            }
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
    }

    /**
     * 连接到 samba 服务器
     * @param ip
     * @param user
     * @param passWorld
     * @return
     */
    fun connect(ip: String?, user: String?, passWorld: String?): Observable<ShareClient?>? {
        return Observable.create(Observable.OnSubscribe<ShareClient?> { subscriber ->
            try {
                subscriber.onNext(ShareClient(ip, user, passWorld, ""))
            } catch (e: Throwable) {
                e.printStackTrace()
                if (subscriber.isUnsubscribed) {
                    return@OnSubscribe
                }
                if (e is ConnectException) {
                    subscriber.onError(ShareException(STATUS_CONNECT_FAILURE))
                } else if (e is TimeoutException) {
                    subscriber.onError(ShareException(STATUS_CONNECT_TIMEOUT))
                } else {
                    subscriber.onError(ShareException(STATUS_LOGON_FAILURE))
                }
            }
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
    }

    private val methodTime: MethodTime = MethodTime("jal")

    fun getShareItems(shareItem: ShareItem): Observable<MutableList<BaseData>>? {
        return Observable.create<MutableList<BaseData>>(Observable.OnSubscribe<MutableList<BaseData>?> { subscriber ->
            try {
                methodTime.start()
                val shareItems = shareItem.fileList
                var baseDataList: MutableList<BaseData>? = null
                if (shareItems != null) {
                    baseDataList = ArrayList()
                    for (item in shareItems) {
                        try {
                            baseDataList.add(BaseData(item))
                        } catch (e: java.lang.Exception) {
                            e.printStackTrace()
                        }
                    }
                }
                methodTime.end(true, "call")
                subscriber.onNext(baseDataList)
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
                if (subscriber.isUnsubscribed) {
                    return@OnSubscribe
                }
                subscriber.onError(e)
            }
        }).subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }

    fun closeConnect(shareClient: ShareClient?) {
        Thread(Runnable { shareClient?.closeConnect() }).start()
    }

    fun closeShareClient() {

        Thread(Runnable {
            try {
                ShareClient.closeAllConnect()
                NanoStreamer.INSTANCE().stop()
            } catch (e: Exception) {
                e.printStackTrace()
            }

        }).start()
    }
}
