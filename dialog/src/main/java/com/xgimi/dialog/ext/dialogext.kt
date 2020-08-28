package com.xgimi.dialog.ext

import android.content.Context
import com.xgimi.dialog.BaseDialog
import com.xgimi.dialog.options.DialogOptions

/**
 *    author : joker.peng
 *    e-mail : joker.peng@xgimi.com
 *    date   : 2020/8/28 4:40 PM
 *    desc   :
 */

fun baseDialog(context: Context, runtimeOverrideOptions: (DialogOptions.() -> Unit)? = null): BaseDialog {
    return BaseDialog(context).apply { setRuntimeOverrideOptions(runtimeOverrideOptions) }
}