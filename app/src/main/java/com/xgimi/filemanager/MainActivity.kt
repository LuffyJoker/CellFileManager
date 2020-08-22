package com.xgimi.filemanager

import android.os.Bundle
import android.view.animation.AccelerateInterpolator
import android.view.animation.OvershootInterpolator
import com.blankj.utilcode.util.ToastUtils
import com.xgimi.filemanager.enums.FileCategory
import com.xgimi.filemanager.listerners.OnMenuClickListener
import com.xgimi.filemanager.listerners.XgimiMenuListener
import com.xgimi.filemanager.menus.XgimiMenuItem
import com.xgimi.filemanager.page.*
import com.xgimi.gimiskin.cell.setStyle
import com.xgimi.gimiskin.sdk.SkinEngine
import com.xgimi.gimiskin.sdk.SkinTypefaceCache
import com.xgimi.view.cell.*
import com.xgimi.view.cell.Supports
import com.xgimi.view.cell.action.Animator
import com.xgimi.view.cell.action.EaseCubicInterpolator2
import com.xgimi.view.cell.action.MeasureToAction
import com.xgimi.view.cell.component.ColorComponent
import com.xgimi.view.cell.component.TextComponent
import com.xgimi.view.cell.layout.AbsoluteLayout
import com.xgimi.view.cell.layout.Gravity
import com.xgimi.view.cell.layout.LinearLayout
import com.xgimi.view.cell.layout.PageLayout
import com.xgimi.view.cell.utils.SimpleFocusAdapter

class MainActivity : BaseActivity() {

    private lateinit var root: Cell

    private val titles: List<String> = mutableListOf("全部", "视频", "音乐", "图片", "文档")
    /**
     * 两次 back 事件时间间隔
     */
    private val BACK_DELAY = 2000
    /**
     * 上次按返回键时间
     */
    private var mExitTime: Long = 0

    private lateinit var tabBar: Cell
    private lateinit var tabSlider: Cell
    private lateinit var pageContainer: Cell
    private lateinit var pageScroller: PageLayout.Scroller
    private var onEntranceTabBar = -1L

    /**
     * 当前显示的页面，默认为 AllPage
     */
    private lateinit var currentPage: BasePage

    private val globeFocusChangeListener = object : FocusManager.OnFocusChangeListener {
        override fun onFocusChanged(from: Cell?, to: Cell?, dir: Int) {
            val fromTabBar = from?.isChildOf(tabBar) ?: true
            var toTabBar = false
            to?.also { newFocus ->
                toTabBar = newFocus.isChildOf(tabBar)
                if (toTabBar) {
                    pageScroller.to(newFocus.indexOfParent(), Animator.DURATION_SLOW)
                    if (fromTabBar) Supports.follow(tabSlider, to)
                }
            }
            if (null == from || null == to) return
            if (fromTabBar && !toTabBar) { // focus move out tabBar
                tabSlider.cancelHolderAnim(true)
                tabSlider.holder = MeasureToAction()
                    .to(from.width.shr(1), 8)
                    .gravity(Gravity.BOTTOM_CENTER)
                    .interpolator(EaseCubicInterpolator2.ease(), AccelerateInterpolator())
                    .duration(Animator.DURATION_FAST)
                    .start(tabSlider)
            } else if (!fromTabBar && toTabBar) { // focus move in tabBar
                onEntranceTabBar = System.currentTimeMillis()
                tabSlider.cancelHolderAnim(true)
                tabSlider.holder = MeasureToAction()
                    .to(to.width, to.height)
                    .gravity(Gravity.BOTTOM_CENTER)
                    .interpolator(EaseCubicInterpolator2.ease(), OvershootInterpolator())
                    .start(tabSlider)
            }
        }
    }

    override fun createCell() {

    }

    override fun initData() {

    }

    override fun initView(savedInstanceState: Bundle?) {
        setEnableMenu(true)
        root = Cell(LinearLayout(LinearLayout.VERTICAL, true))
            .setLifeCycleListener(object : CellEvent.LifeCycleAdapter() {
                override fun onAttach(self: Cell) {
                    val fm = root.render.focusManager
                    fm.addFocusChangeListener(globeFocusChangeListener)
                    fm.addFocusTrace(tabBar, false)
                    self.requestFocus()
                }
            })
        iniTipsView()
        initTabBar()
        initPages()
        cellView.root = root
    }

    private fun initPages() {
        pageContainer =
            Cell(PageLayout(PageLayout.HORIZONTAL)).setFocusAdapter(SimpleFocusAdapter().skip(true))
        pageContainer.addCell(
            AllPage(this)
                .apply { currentPage = this }.getRootCell(),
            Layout.Params(Layout.Params.FULL, Layout.Params.FULL)
        )
        pageContainer.addCell(
            VideoPage(getString(R.string.video), FileCategory.Video.ordinal, this)
                .apply { currentPage = this }.getRootCell(),
            Layout.Params(Layout.Params.FULL, Layout.Params.FULL)
        )
        pageContainer.addCell(
            MusicPage(getString(R.string.music), FileCategory.Music.ordinal, this)
                .apply { currentPage = this }.getRootCell(),
            Layout.Params(Layout.Params.FULL, Layout.Params.FULL)
        )

        pageContainer.addCell(
            PicturePage(getString(R.string.picture), FileCategory.Picture.ordinal, this)
                .apply { currentPage = this }.getRootCell(),
            Layout.Params(Layout.Params.FULL, Layout.Params.FULL)
        )

        pageContainer.addCell(
            DocPage(getString(R.string.doc), FileCategory.Document.ordinal, this)
                .apply { currentPage = this }.getRootCell(),
            Layout.Params(Layout.Params.FULL, Layout.Params.FULL)
        )

        root.addCell(
            pageContainer,
            Layout.Params(Layout.Params.FULL, Layout.Params.FULL)
        )

        pageScroller = PageLayout.Scroller(pageContainer)
    }

    private fun initTabBar() {
        tabSlider = Cell(
            ColorComponent()
                .setColorRes(R.color.color_brand_1)
                .setCorner(SkinEngine.getDimension(R.dimen.attr_radius_2))
        )
        tabBar = Cell(LinearLayout(LinearLayout.HORIZONTAL))
            .setFocusAdapter(SimpleFocusAdapter().intercept(true, false, true, false))
            .setPadding(96, 0, 96, 0)
            .setOnScrollListener { _, dx, _ -> tabSlider.offset(dx, 0) } // fix slider offset

        titles.forEachIndexed { _, s ->
            tabBar.addCell(
                Cell(
                    TextComponent()
                        .setTypeface(SkinTypefaceCache.getCacheTypeface(SkinEngine.getString(R.string.bold)))
                        .setTextSize(SkinEngine.getDimension(R.dimen.font_crosshead_bold_2))
                ).setFocusable(true)
                    .setPadding(32, 0, 32, 0)
                    .setLayoutParams(LinearLayout.Params(Layout.Params.WRAP, 72, Gravity.CENTER))
                    .setHolder(s)
                    .apply {
                        this.findComponent<TextComponent>(0).text = s
                    })
        }

        val topContainer = Cell(LinearLayout(LinearLayout.VERTICAL, true))
            .setZ(1)
            .addCell(tabSlider, AbsoluteLayout.Params())
            .addCell(tabBar)

        root.addCell(
            topContainer,
            Layout.Params(Layout.Params.FULL, 72).setMarginTop(40)
        )
    }

    private fun iniTipsView() {
        val titleCell = Cell(
            TextComponent()
                .setText(getString(R.string.file_header_notice_pre))
                .setStyle(R.style.font_body_medium_1)
        )
        root.addCell(
            titleCell,
            LinearLayout.Params(656, 28, Gravity.RIGHT_TOP).setMargin(0, 30, 96, 0)
        )
    }

    override fun initMenu(isEdit: Boolean) {
        super.initMenu(isEdit)
        mXgimiMenuItem?.subMenus?.clear()
        if (!isEdit) {
            val xgimiMenuItem: XgimiMenuItem = currentPage.initMenus()!!
            if (xgimiMenuItem != null) {
                mXgimiMenuItem?.addAllMenus(xgimiMenuItem.subMenus)
            }
        } else {
            mXgimiMenuItem?.addAllMenus(currentPage.getEditMenu().subMenus)
        }
        setOnMenuClickListener(OnMenuClickListener { _, xgimiMenuItem ->
            val isClose: Boolean = currentPage.onMenuClicked(xgimiMenuItem)
            if (isClose) {
                hideMenu()
            }
        })
        mXgimiMenuPopWindow?.setXgimiMenuListener(object : XgimiMenuListener {
            override fun onMenuStartShowing() {
                currentPage.isMenuShowed = true
            }

            override fun onMenuStartHiding() {
                currentPage.isMenuShowed = false
            }
        })
    }

    override fun onBackPressed() {
        if (isMenuShowing()) {
            hideMenu()
            return
        }
        var onBackPressed = false
        val page = currentPage
        if (page != null) {
            onBackPressed = page.onBackPressed()
        }

        if (!onBackPressed) {
            if (System.currentTimeMillis() - mExitTime > BACK_DELAY) {
                mExitTime = System.currentTimeMillis()
                ToastUtils.showShort(R.string.press_again_exit)
                return
            }
            super.onBackPressed()
        }
    }
}