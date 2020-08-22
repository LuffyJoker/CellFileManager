package com.xgimi.filemanager.cell

import com.xgimi.view.cell.Cell
import com.xgimi.view.cell.Layout
import com.xgimi.view.cell.component.ImageComponent
import com.xgimi.view.cell.component.TextComponent

/**
 *    author : joker.peng
 *    e-mail : joker.peng@xgimi.com
 *    date   : 2020/8/6 17:20
 *    desc   :
 */
interface IPoster {
    companion object {
        const val TYPE_DEFAULT = 0
        const val TYPE_MULTI_LAYER = 1

        // size(28) + marginTop(18) + marginBottom(18)
        const val HEIGHT_SUB_TITLE = 60
        val TITLE_MARGIN_TOP = -Cells.Theme.CORNER.toInt()

        const val FOCUS_STYLE_ICON = 1
        const val FOCUS_STYLE_POSTER = 0
    }

    data class Layer(
        val poster: Any?,
        val w: Int = Layout.Params.FULL,
        val h: Int = Layout.Params.FULL,
        val x: Int = 0, // left | top
        val y: Int = 0,
        val scalePivotX: Float = 0.5f, // percent of width
        val scalePivotY: Float = 0.5f, // percent of height
        val scaleX: Float = 1.0f,
        val scaleY: Float = 1.0f
    )

    var holder: Any?

    fun getStyleType():Int

    fun getRoot(): Cell

    fun getWidth(): Int

    fun getHeight(): Int

    fun setPosterSize(w: Int, h: Int): IPoster

    fun reSize(w: Int, h:Int): IPoster

    fun setImageLayer(order: Int, layer: Layer, alwaysAlpha: Boolean = false): IPoster

    fun getImageLayer(order: Int): ImageComponent

    fun clearImageLayer(): IPoster

    fun setSubTitle(text: String?): IPoster

    fun setSubTitleColor(color: Int): IPoster

    fun getSubTitle(): TextComponent

    fun setFocusStyle(style: Int)

    fun setSubTitleVisible(visible: Boolean, withAnim: Boolean = true): IPoster

    fun setSubScript(res: Any?): IPoster

    fun setEpisodes(text: String?): IPoster

    fun setShadowType(type: Int): IPoster

    fun onFocusChanged(focusAction: (IPoster, Int, Boolean) -> Unit): IPoster

    fun onClick(clickAction: (IPoster) -> Unit): IPoster
}