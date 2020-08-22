package com.xgimi.filemanager.popwin

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.graphics.Bitmap
import android.os.Handler
import android.util.AttributeSet
import android.view.View
import android.widget.PopupWindow
import com.blankj.utilcode.util.ScreenUtils
import com.xgimi.filemanager.R
import com.xgimi.filemanager.listerners.OnMenuClickListener
import com.xgimi.filemanager.listerners.XgimiMenuListener
import com.xgimi.filemanager.menus.XgimiMenuItem
import com.xgimi.filemanager.utils.DeviceUtil
import com.xgimi.filemanager.views.BlurBgView
import com.xgimi.gimiskin.cell.setStyle
import com.xgimi.view.cell.Cell
import com.xgimi.view.cell.CellView
import com.xgimi.view.cell.GlobalConfig
import com.xgimi.view.cell.Threads
import com.xgimi.view.cell.action.ScaleToAction
import com.xgimi.view.cell.component.FocusComponent
import com.xgimi.view.cell.component.ImageComponent
import com.xgimi.view.cell.component.TextComponent
import com.xgimi.view.cell.layout.Gravity
import com.xgimi.view.cell.layout.LinearLayout

/**
 *    author : joker.peng
 *    e-mail : joker.peng@xgimi.com
 *    date   : 2020/8/10 18:45
 *    desc   :
 */
class XgimiMenuPop : PopupWindow {

    private var mAnchorView: View? = null
    private var mCurrentXgimiMenuItem: XgimiMenuItem? = null
    private var mMenuClickListener: OnMenuClickListener? = null
    private var mContext: Context? = null
    private var shotBitmap: Bitmap? = null
    private var mHandler: Handler = Handler()
    private var forceDismiss = false
    private var mXgimiMenuListener: XgimiMenuListener? = null
    private var mThread: Thread? = null

    private val root: Cell by lazy {
        Cell(LinearLayout(LinearLayout.VERTICAL, 12).setAlign(Gravity.CENTER))
            .addComponent(ImageComponent()).apply {
                GlobalConfig.addBehavior(id, GlobalConfig.BEHAVIOR_NO_EFFECTS)
            }
    }

    private val cellView: CellView by lazy {
        CellView(mContext)
    }

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        mContext = context
        initView()
        this.isFocusable = true
        this.isOutsideTouchable = true
        this.animationStyle = R.style.menuanimationpreview
    }

    private fun initView() {
        width = 310
        height = ScreenUtils.getScreenHeight()
        mAnchorView = getWrapperActivity(mContext)?.window?.decorView?.findViewById(android.R.id.content)
        cellView.init(mContext)
        contentView = cellView
        cellView.root = root
    }

    fun setXgimiMenuListener(listener: XgimiMenuListener) {
        mXgimiMenuListener = listener
    }

    private fun getWrapperActivity(context: Context?): Activity? {
        if (context is Activity) {
            return context
        } else if (context is ContextWrapper) {
            return getWrapperActivity(context.baseContext)
        }
        return null
    }

    fun show() {
        if (isShowing) {
            return
        }
        updateBlurBack()
        showAsDropDown(
            mAnchorView,
            0,
            -ScreenUtils.getScreenHeight()
        )
        mXgimiMenuListener?.onMenuStartShowing()
    }

    fun hide() {
        forceDismiss = true
        mThread?.interrupt()
        mThread = null
        dismiss()
    }

    private fun updateBlurBack() {
        Threads.postToBackground {
            var blurBgView = BlurBgView(mContext!!)
            shotBitmap = ScreenUtils.screenShot(mContext as Activity)
            val blurB: Bitmap? = blurBgView.doBlur(
                mContext,
                Bitmap.createBitmap(shotBitmap!!, 0, 0, width, ScreenUtils.getScreenHeight()),
                22,
                0.2f
            )
            root.findComponent<ImageComponent>(0).res = blurB
        }
    }

    private fun initMenuList(xgimiMenuItem: XgimiMenuItem?) {

        xgimiMenuItem?.subMenus?.forEachIndexed { _, xgimiMenuItem ->
            Cell(
                FocusComponent().setColorRes(R.color.color_brand_1).setStrokeWidth(-1f).setCorner(
                    8f
                ).setWithPadding(false))
                .addCell(
                    Cell(
                        TextComponent().setText(xgimiMenuItem.name).setEllipsis(true).setStyle(
                            R.style.font_crosshead_medium_3
                        )
                    ).setMask(true)
                )
                .setPadding(12)
                .setFocusable(true)
                .setEffects(true)
                .setOnFocusChangeListener { self, _ ->
                    self.getCell(0).findComponent<TextComponent>(0).setMarquee(self.isFocused)
                    ScaleToAction().to(if (self.isFocused) 1.2f else 1f).start(self)
                }
                .addTo(root, LinearLayout.Params(212, 80, Gravity.CENTER))
        }
    }

    fun setMenus(menus: XgimiMenuItem?) {
        menus?.apply {
            complementMenu()
        }
        setMenus(menus, -1)
    }

    private fun setMenus(xgimiMenuItem: XgimiMenuItem?, actiom: Int) {
        if (xgimiMenuItem?.subMenus != null) {
            mCurrentXgimiMenuItem = xgimiMenuItem
            if ("false".equals(
                    DeviceUtil.getProp("persist.xgimi.app.animation.enable", "true"),
                    ignoreCase = true
                )
            ) {
                //animation = false
            }
        }

        initMenuList(xgimiMenuItem)
    }

    fun setOnMenuClickListener(onMenuClickListener: OnMenuClickListener?) {
        mMenuClickListener = onMenuClickListener
    }

    fun destroy() {
        mHandler.removeCallbacksAndMessages(null)
        mThread?.interrupt()
        mThread = null
        shotBitmap?.recycle()
    }
}