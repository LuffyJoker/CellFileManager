package com.xgimi.dialog.options

import android.view.LayoutInflater
import android.view.View
import android.view.Window
import androidx.annotation.LayoutRes
import com.xgimi.dialog.ext.UtilsExtension.Companion.unDisplayViewSize

/**
 *    author : joker.peng
 *    e-mail : joker.peng@xgimi.com
 *    date   : 2020/8/28 4:42 PM
 *    desc   :
 */

class DialogOptions {

    /**
     * 布局文件id
     */
    @LayoutRes
    var layoutId = -1

    /**
     * Dialog 显示的View
     */
    var contentView: View? = null

    /**
     * Convert监听
     */
    internal var convertListener: ((viewHolder: ViewHolder) -> Unit)? = null

    /**
     * Window属性
     */
    var windowFeature: Int? = Window.FEATURE_NO_TITLE

    /**
     * 是否允许取消对话框
     */
    var enableCancel = true

    /**
     * 点击外部区域是否取消对话框
     */
    var touchOutsideCancel = true

    /**
     * 系统级别对话框
     */
    var systemAlert = false

    /**
     * 对话框宽度
     */
    var width = 0

    /**
     * 对话框高度
     */
    var height = 0

    /**
     * 背景透明度比例
     */
    var dimAmount = 0.3f

    /**
     * 当dialog依附于window时的位置（默认居中）
     */
    var dialogGravity = DialogGravity.CENTER_CENTER

    /**
     * 当dialog依附于view时的横向位置（默认左对齐）
     */
    var horizontalPosition = HorizontalPosition.ALIGN_LEFT

    /**
     * 当dialog依附于view时的纵向位置（默认在上方）
     */
    var verticalPosition = VerticalPosition.ABOVE

    /**
     * 当dialog依附在view上时横向的偏移量
     */
    var horizontalOffset = 0

    /**
     * 当dialog依附在view上时纵向的偏移量
     */
    var verticalOffset = 0

    /**
     * 依附于锚点显示时计算出的对话框X坐标
     */
    internal var dialogViewX = 0

    /**
     * 依附于锚点显示时计算出的对话框Y坐标
     */
    internal var dialogViewY = 0

    /**
     * 是否依附于锚点显示
     */
    internal var showOnAnchor = false

    /**
     * 依附于锚点显示时，计算 dialogViewX 和 dialogViewY
     */
    internal fun calculateDialogViewXY(view: View) {

        //获取到dialogView的宽高
        var dialogViewSize = when {
            layoutId != -1 -> {
                unDisplayViewSize(LayoutInflater.from(view.context).inflate(layoutId, null))
            }
            contentView != null -> {
                unDisplayViewSize(contentView!!)
            }
            else -> {
                throw IllegalStateException("view must not be null !!!")
            }
        }

        val dialogViewWidth = dialogViewSize[0]
        val dialogViewHeight = dialogViewSize[1]

        // 设置 view 的数据
        val viewWidth = view.width
        val viewHeight = view.height
        val viewX = view.x.toInt()
        val viewY = view.y.toInt()

        // 计算dialogView的横坐标
        dialogViewX = when (horizontalPosition) {
            HorizontalPosition.LEFT -> viewX - if (width != 0) width else dialogViewWidth + horizontalOffset
            HorizontalPosition.ALIGN_RIGHT -> viewX + viewWidth - (if (width != 0) width else dialogViewWidth) + horizontalOffset
            HorizontalPosition.CENTER -> viewX - ((if (width != 0) width else dialogViewWidth) - viewWidth) / 2 + horizontalOffset
            HorizontalPosition.ALIGN_LEFT -> viewX + horizontalOffset
            HorizontalPosition.RIGHT -> viewX + viewWidth + horizontalOffset
        }
        // 计算dialogView的纵坐标
        dialogViewY = when (verticalPosition) {
            VerticalPosition.ABOVE -> viewY - (if (height != 0) height else dialogViewHeight) + verticalOffset
            VerticalPosition.ALIGN_BOTTOM -> viewY + viewHeight - (if (height != 0) height else dialogViewHeight) + verticalOffset
            VerticalPosition.CENTER -> viewY - ((if (height != 0) height else dialogViewHeight) - viewHeight) / 2 + verticalOffset
            VerticalPosition.ALIGN_TOP -> viewY + verticalOffset
            VerticalPosition.BELOW -> viewY + viewHeight + verticalOffset
        }
    }

    /**
     * 设置Convert监听
     */
    fun setConvertListener(convertListener: (viewHolder: ViewHolder) -> Unit) {
        this.convertListener = convertListener
    }
}