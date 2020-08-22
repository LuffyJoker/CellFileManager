package com.xgimi.filemanager

import android.os.Bundle
import com.xgimi.filemanager.ext.tryCatch
import com.xgimi.gimiskin.cell.BaseSkinMixActivity
import kotlinx.coroutines.*

/**
 *    author : joker.peng
 *    e-mail : joker.peng@xgimi.com
 *    date   : 2020/8/7 11:51
 *    desc   :
 */
open class IBaseActivity : BaseSkinMixActivity(), CoroutineScope by MainScope() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setWindowSkinBackgroundColor(R.color.color_bg_pure_0)
    }

    override fun onDestroy() {
        super.onDestroy()
        cancel()
    }

    fun launchMain(delayTime: Long = 0, func: () -> Unit) {
        tryCatch {
            launch(Dispatchers.Main) {
                if (delayTime > 0)
                    delay(delayTime)
                func()
            }
        }
    }

    fun launchIO(delayTime: Long = 0, func: () -> Unit) {
        tryCatch {
            launch(Dispatchers.IO) {
                if (delayTime > 0)
                    delay(delayTime)
                func()
            }
        }
    }
}
