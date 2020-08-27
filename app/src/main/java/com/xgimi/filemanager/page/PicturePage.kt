package com.xgimi.filemanager.page

import android.app.Activity
import android.util.Log
import android.view.KeyEvent
import android.view.View
import com.blankj.utilcode.util.LogUtils
import com.chad.library.adapter.base.BaseQuickAdapter
import com.xgimi.dlna.proxy.AllShareProxy
import com.xgimi.dlna.proxy.IDeviceChangeListener
import com.xgimi.dlna.upnp.DMSDeviceBrocastFactory
import com.xgimi.dlna.upnp.Device
import com.xgimi.filemanager.R
import com.xgimi.filemanager.bean.BaseData
import com.xgimi.filemanager.bean.DeviceInfo
import com.xgimi.filemanager.config.DisplayMode
import com.xgimi.filemanager.config.OperationConfigure
import com.xgimi.filemanager.contentprovider.MediaContentObserver
import com.xgimi.filemanager.enums.FileCategory
import com.xgimi.filemanager.filehelper.OperationEvent
import com.xgimi.filemanager.helper.CellCreateHelper
import com.xgimi.filemanager.helper.Comparators
import com.xgimi.filemanager.helper.MountHelper
import com.xgimi.filemanager.helper.ResourceHelper
import com.xgimi.filemanager.listerners.OnFileItemClickListener
import com.xgimi.filemanager.listerners.OnItemSelectedListener
import com.xgimi.filemanager.menus.XgimiMenuItem
import com.xgimi.filemanager.prenster.CategoryDataProxy
import com.xgimi.gimiskin.cell.setStyle
import com.xgimi.view.cell.Cell
import com.xgimi.view.cell.CellDataBinding
import com.xgimi.view.cell.CellEvent
import com.xgimi.view.cell.Layout
import com.xgimi.view.cell.component.TextComponent
import com.xgimi.view.cell.layout.Gravity
import com.xgimi.view.cell.layout.LinearLayout

/**
 *    author : joker.peng
 *    e-mail : joker.peng@xgimi.com
 *    date   : 2020/8/6 19:50
 *    desc   :
 */
class PicturePage(
    title: String,
    category: Int,
    context: Activity
) : BasePage(context),
    MediaContentObserver.ContentObserverListener,
    OnFileItemClickListener,
    OnItemSelectedListener {

    /**
     * 所有显示的设备
     */
    private var deviceDataList = mutableListOf<DeviceInfo>()

    /**
     * DLNA工具
     */
    private lateinit var mAllShareProxy: AllShareProxy

    private lateinit var mBroadcastFactory: DMSDeviceBrocastFactory

    private var mCategoryDataProxy: CategoryDataProxy

    companion object {
        private const val GROUP_SIZE = 6
        const val TAG_ICON = "TAG_ICON"
        const val TAG_STROKE = "TAG_STROKE"
        const val TAG_NAME = "TAG_NAME"
        const val TAG_DES = "TAG_DES"
        const val TAG_FOCUS = "TAG_FOCUS"
        const val TAG_SHADOW = "TAG_SHADOW"
        const val DELAY = 1000L
    }

    private val root: Cell = Cell(LinearLayout(LinearLayout.VERTICAL)).setPadding(96, 96, 96, 0)

    fun getRootCell(): Cell {
        return root
    }

    init {
        NAME = title
        mCategoryDataProxy = CategoryDataProxy(context, category)
        mCategoryDataProxy.register(mHandler, this)
        headerHideEnable = true
        loadData(isForceRefresh = false, isDelay = false)
    }

    private fun loadData(isForceRefresh: Boolean, isDelay: Boolean) {
        mCategoryDataProxy.loadData(object : CategoryDataProxy.CategoryDataloadListener {
            override fun onSuccess(data: List<Any>?) {
                initCellView(data)
            }

            override fun onError(throwable: Throwable?) {

            }
        }, isForceRefresh, isDelay)
    }

    private val onClickListener: CellEvent.OnClickListener = CellEvent.OnClickListener { p0 ->

    }

    private val longPressListener = object : CellEvent.OnLongPressListener {
        override fun onLongPress(cell: Cell, event: Int): Boolean {
            if (event == KeyEvent.KEYCODE_DPAD_CENTER) {
                return true
            }
            return false
        }
    }

    private fun initCellView(data: List<Any>?) {

        if (data != null && data.isNotEmpty()) {
            var musicContainer = Cell(LinearLayout(LinearLayout.VERTICAL, GROUP_SIZE, 72, 10))
            data?.forEachIndexed { _, any ->
                musicContainer.addCell(
                    CellCreateHelper.getItemCell(
                        any,
                        onClickListener,
                        longPressListener
                    )
                )
            }
            root.addCell(
                musicContainer,
                LinearLayout.Params(
                    LinearLayout.Params.FULL,
                    LinearLayout.Params.WRAP
                ).setMarginTop(48)
            )
        } else {
            root.addCell(
                CellCreateHelper.textCell(
                    context.resources.getString(R.string.empty),
                    R.style.font_crosshead_medium_2
                )
            )
        }
    }

    override fun initMenus(): XgimiMenuItem? {
        val mMenu = XgimiMenuItem()
        val category = getCategory()
        //添加排序
        mMenu.addMenu(
            XgimiMenuItem(
                OperationEvent.Sortorder,
                if (OperationConfigure.getSortOrder() === Comparators.SortMode.NAME.ordinal) getString(
                    R.string.time_sort
                ) else getString(R.string.name_sort)
            )
        )
        val superMenus: MutableList<XgimiMenuItem> = super.initMenus()!!.subMenus
        superMenus.addAll(1, getCategoryMenu(category).subMenus)
        mMenu.addAllMenus(superMenus)
        return mMenu
    }

    override fun getCategory(): Int {
        return mCategoryDataProxy.getCategory()
    }

    /**
     * 获取不同类别对应的菜单
     *
     * @param category
     * @return
     */
    private fun getCategoryMenu(category: Int): XgimiMenuItem {
        val mMenu = XgimiMenuItem()
        if (category == FileCategory.Video.ordinal) {
            mMenu.addMenu(
                XgimiMenuItem(
                    OperationEvent.ChangeDisplayMode,
                    if (OperationConfigure.getDisplayMode() === DisplayMode.FileMode.ordinal) getString(
                        R.string.folder_mode
                    ) else getString(R.string.play_mode)
                )
            )
        }
        var stringId = 0
        when (category) {
            FileCategory.Video.ordinal -> {
                stringId = R.string.search_video_file
            }
            FileCategory.Music.ordinal -> {
                stringId = R.string.search_audio_file
            }
            FileCategory.Picture.ordinal -> {
                stringId = R.string.search_picture_file
            }
            FileCategory.Document.ordinal -> {
                stringId = R.string.search_document_file
            }
        }
        mMenu.addMenu(XgimiMenuItem(OperationEvent.SearchFile, getString(stringId)))
        if (category == FileCategory.Video.ordinal && mCategoryDataProxy.hasExtraData()) {
            mMenu.addMenu(
                XgimiMenuItem(
                    OperationEvent.ClearHistory,
                    getString(R.string.clear_history)
                )
            )
        }
        return mMenu
    }

    override fun onMenuClicked(menu: XgimiMenuItem): Boolean {
        when (menu.operationType) {
            OperationEvent.ChangeDisplayMode -> OperationConfigure.changeDisplayMode()
            OperationEvent.Sortorder -> OperationConfigure.changeSortOrder()
//            OperationEvent.ClearHistory -> MessageDialogBuilder(mContext)
//                .setTitle(R.string.del_history_dialog_notice)
//                .setMessage(R.string.del_history_dialog_content)
//                .addAction(R.string.dialog_right, object : ActionListener() {
//                    fun onClick(dialog: GMUIDialog, index: Int) {
//                        dialog.dismiss()
//                        mResourceHelper.clearAllHistory()
//                        isPlayedRefresh = true
//                        Toast.makeText(mContext, R.string.clear_record_success, Toast.LENGTH_SHORT)
//                            .show()
//                    }
//                })
//                .addAction(R.string.dialog_left, object : ActionListener() {
//                    fun onClick(dialog: GMUIDialog, index: Int) {
//                        dialog.dismiss()
//                    }
//                }).show()
        }
        return super.onMenuClicked(menu)
    }

    override fun onChange() {
        if (root.getCell(0).isVisible) {
            loadData(isForceRefresh = false, isDelay = false)
        }
    }

    override fun onItemClick(adapter: BaseQuickAdapter<*, *>, view: View, position: Int) {
        // TODO: 2020/8/17 点击事件
    }

    override fun onItemSelected(position: Int, item: Any?) {
        // TODO: 2020/8/17 选中事件
    }
}