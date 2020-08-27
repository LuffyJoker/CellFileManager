package com.xgimi.filemanager.utils

import com.xgimi.filemanager.listerners.IIOTask
import rx.Observable
import rx.Subscription
import rx.schedulers.Schedulers

/**
 *    author : joker.peng
 *    e-mail : joker.peng@xgimi.com
 *    date   : 2020/8/26 16:26
 *    desc   :
 */
object RxjavaUtil {

    /***
     * 在IO线程中执行任务
     * @param <T>
    </T> */
    fun <T> doInIOThread(ioTask: IIOTask): Subscription? {
        return Observable.just<IIOTask>(ioTask)
            .observeOn(Schedulers.io())
            .subscribe(
                { iioTask -> iioTask.doInIOThread() }
            ) { throwable -> throwable.printStackTrace() }
    }
}