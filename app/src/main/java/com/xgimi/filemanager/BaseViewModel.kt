package com.xgimi.filemanager

import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.xgimi.filemanager.ext.tryCatch
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 *    author : joker.peng
 *    e-mail : joker.peng@xgimi.com
 *    date   : 2020/8/7 11:54
 *    desc   :
 */
abstract class BaseViewModel<T> : AndroidViewModel(FileManagerApplication.getInstance()) {

    abstract val mRepository: T

    val exception: MutableLiveData<Throwable> = MutableLiveData()

    protected fun launchMain(block: suspend CoroutineScope.() -> Unit) {
        viewModelScope.launch(Dispatchers.Main) { block() }
    }

    protected fun launchIO(block: suspend CoroutineScope.() -> Unit) {
        viewModelScope.launch(Dispatchers.IO) { block() }
    }
}