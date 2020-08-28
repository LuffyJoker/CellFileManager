package com.xgimi.dialog.options

import android.view.Gravity

/**
 *    author : joker.peng
 *    e-mail : joker.peng@xgimi.com
 *    date   : 2020/8/28 4:42 PM
 *    desc   :
 */
enum class DialogGravity(val index: Int) {

    NO_GRAVITY(Gravity.NO_GRAVITY),

    LEFT_TOP(Gravity.START or Gravity.TOP),

    CENTER_TOP(Gravity.CENTER_HORIZONTAL or Gravity.TOP),

    RIGHT_TOP(Gravity.END or Gravity.TOP),

    LEFT_CENTER(Gravity.START or Gravity.CENTER_VERTICAL),

    CENTER_CENTER(Gravity.CENTER),

    RIGHT_CENTER(Gravity.END or Gravity.CENTER_VERTICAL),

    LEFT_BOTTOM(Gravity.START or Gravity.BOTTOM),

    CENTER_BOTTOM(Gravity.CENTER_HORIZONTAL or Gravity.BOTTOM),

    RIGHT_BOTTOM(Gravity.END or Gravity.BOTTOM)
}
