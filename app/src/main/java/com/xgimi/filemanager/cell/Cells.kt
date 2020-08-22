package com.xgimi.filemanager.cell

import android.graphics.Color
import com.xgimi.filemanager.R
import com.xgimi.filemanager.cell.IPoster.Companion.TYPE_MULTI_LAYER
import com.xgimi.gimiskin.sdk.SkinEngine
import com.xgimi.gimiskin.sdk.SkinTypefaceCache
import com.xgimi.view.cell.Cell
import com.xgimi.view.cell.Layout
import com.xgimi.view.cell.component.ColorComponent
import com.xgimi.view.cell.component.ImageComponent
import com.xgimi.view.cell.component.TextComponent
import com.xgimi.view.cell.layout.Gravity
import com.xgimi.view.cell.layout.LinearLayout

/**
 *    author : joker.peng
 *    e-mail : joker.peng@xgimi.com
 *    date   : 2020/8/6 17:19
 *    desc   :
 */
object Cells {

    class Theme {
        companion object {
            val CORNER = SkinEngine.getDimension(R.dimen.attr_radius_1)
            const val WIDTH = 6f

            /**
             * blue : #288CE9
             * white: #FFFFFF
             */
            const val COLOR_FOCUS_FRAME = "#FFFFFF"

            /**
             * 顶部（状态栏+导航栏）高度
             */
            const val TOP_CONTAINER_HEIGHT = 176
        }
    }

    fun createTab(text: String? = null): Cell {
        val tc = TextComponent()
            .setTypeface(SkinTypefaceCache.getCacheTypeface(SkinEngine.getString(R.string.bold)))
            .setTextSize(SkinEngine.getDimension(R.dimen.font_crosshead_bold_2))
            .setText(text)
        return Cell(tc)
            .setFocusable(true)
            .setPadding(32, 0, 32, 0)
            .setLayoutParams(LinearLayout.Params(Layout.Params.WRAP, 72, Gravity.CENTER))
    }
}