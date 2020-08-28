package com.xgimi.dialog

import android.app.Dialog
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import com.xgimi.dialog.options.*

/**
 *    author : joker.peng
 *    e-mail : joker.peng@xgimi.com
 *    date   : 2020/8/28 4:39 PM
 *    desc   :
 */
open class BaseDialog(context: Context) : Dialog(context) {

    private val dialogOptions: DialogOptions = DialogOptions()

    /**
     * 继承该基类时可以通过覆写该属性，以提供一个修改DialogOptions的操作
     */
    protected open var compileOverrideOptions: (DialogOptions.() -> Unit)? = null

    /**
     * 使用时修改DialogOptions的操作
     */
    private var runtimeOverrideOptions: (DialogOptions.() -> Unit)? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        compileOverrideOptions?.run { this.invoke(dialogOptions) }
        runtimeOverrideOptions?.run { this.invoke(dialogOptions) }
        dialogOptions.windowFeature?.run { requestWindowFeature(this) }
        val rootView = LayoutInflater.from(context).inflate(dialogOptions.layoutId, null)
        setContentView(rootView)
        dialogOptions.convertListener?.invoke(ViewHolder(rootView))
    }

    override fun onStart() {
        super.onStart()

        window?.let { window ->
            if (dialogOptions.systemAlert) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    window.setType(WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY)
                } else {
                    window.setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT)
                }
            }
            window.attributes?.run {
                //调节灰色背景透明度[0-1]，默认0.3f
                dimAmount = dialogOptions.dimAmount
                //设置dialog宽度
                width = if (dialogOptions.width == 0) WindowManager.LayoutParams.WRAP_CONTENT else dialogOptions.width
                //设置dialog高度
                height = if (dialogOptions.height == 0) WindowManager.LayoutParams.WRAP_CONTENT else dialogOptions.height
                //如果设置了asView，那么设置dialog的x，y值，将dialog显示在view附近
                if (dialogOptions.showOnAnchor) {
                    // TODO 有错误
                    gravity = DialogGravity.NO_GRAVITY.index
                    x = dialogOptions.dialogViewX
                    y = dialogOptions.dialogViewY
                } else {
                    gravity = dialogOptions.dialogGravity.index
                }
            }
        }
        //设置是否点击外部不消失
        setCancelable(dialogOptions.enableCancel)
        //设置是否点击屏幕区域不消失（点击返回键可消失）
        setCanceledOnTouchOutside(dialogOptions.touchOutsideCancel)
    }

    /**
     * 设置一个在使用时覆写DialogOptions的操作
     */
    fun setRuntimeOverrideOptions(runtimeOverrideOptions: (DialogOptions.() -> Unit)?) {
        this.runtimeOverrideOptions = runtimeOverrideOptions
    }

    /**
     * 在window中进行显示
     */
    fun showOnWindow(dialogGravity: DialogGravity? = null) {
        dialogOptions.showOnAnchor = false
        dialogGravity?.run { dialogOptions.dialogGravity = this }
        this.show()
    }

    /**
     * 以view为锚点进行显示
     */
    fun showOnAnchor(view: View,
                     horizontalPosition: HorizontalPosition? = null,
                     verticalPosition: VerticalPosition? = null,
                     horizontalOffset: Int? = null, verticalOffset: Int? = null) {
        horizontalPosition?.run { dialogOptions.horizontalPosition = this }
        verticalPosition?.run { dialogOptions.verticalPosition = this }
        horizontalOffset?.run { dialogOptions.horizontalOffset = this }
        verticalOffset?.run { dialogOptions.verticalOffset = this }
        dialogOptions.showOnAnchor = true
        dialogOptions.calculateDialogViewXY(view)
        this.show()
    }
}