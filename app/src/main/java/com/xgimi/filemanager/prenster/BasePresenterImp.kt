package com.xgimi.filemanager.prenster

import rx.Subscription
import rx.subscriptions.CompositeSubscription

/**
 *    author : joker.peng
 *    e-mail : joker.peng@xgimi.com
 *    date   : 2020/8/17 11:19
 *    desc   :
 */
open class BasePresenterImp {

    var mCompositeSubscription: CompositeSubscription? = CompositeSubscription()

    fun unSubscribe() {
        if (mCompositeSubscription != null) {
            mCompositeSubscription!!.unsubscribe()
            mCompositeSubscription = null
        }
    }

    fun addSubscribe(subscription: Subscription?) {
        if (mCompositeSubscription == null) {
            mCompositeSubscription = CompositeSubscription()
        }
        mCompositeSubscription!!.add(subscription)
    }
}