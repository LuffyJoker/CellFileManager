package com.xgimi.filemanager.page

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.View
import com.blankj.utilcode.util.LogUtils
import com.chad.library.adapter.base.BaseQuickAdapter
import com.xgimi.dlna.proxy.AllShareProxy
import com.xgimi.dlna.proxy.IDeviceChangeListener
import com.xgimi.dlna.upnp.DMSDeviceBrocastFactory
import com.xgimi.dlna.upnp.Device
import com.xgimi.filemanager.FileManagerApplication
import com.xgimi.filemanager.R
import com.xgimi.filemanager.bean.BaseData
import com.xgimi.filemanager.bean.CatalogInfo
import com.xgimi.filemanager.bean.DeviceInfo
import com.xgimi.filemanager.config.DisplayMode
import com.xgimi.filemanager.config.OperationConfigure
import com.xgimi.filemanager.constants.Constants
import com.xgimi.filemanager.contentprovider.MediaContentObserver.ContentObserverListener
import com.xgimi.filemanager.enums.FileCategory
import com.xgimi.filemanager.filehelper.OperationEvent
import com.xgimi.filemanager.helper.*
import com.xgimi.filemanager.listerners.OnFileItemClickListener
import com.xgimi.filemanager.listerners.OnItemSelectedListener
import com.xgimi.filemanager.menus.XgimiMenuItem
import com.xgimi.filemanager.prenster.CategoryDataProxy
import com.xgimi.filemanager.utils.MediaOpenUtil
import com.xgimi.gimiskin.cell.setStyle
import com.xgimi.view.cell.Cell
import com.xgimi.view.cell.CellDataBinding
import com.xgimi.view.cell.CellEvent
import com.xgimi.view.cell.component.TextComponent
import com.xgimi.view.cell.layout.Gravity
import com.xgimi.view.cell.layout.LinearLayout
import org.simple.eventbus.EventBus
import java.util.*

/**
 *    author : joker.peng
 *    e-mail : joker.peng@xgimi.com
 *    date   : 2020/8/6 19:50
 *    desc   :
 */
class VideoPage(
    title: String,
    category: Int,
    context: Activity
) : BasePage(context),
    IDeviceChangeListener,
    ContentObserverListener,
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

    private var isPlayedRefresh = false

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

    private val root: Cell by lazy {
        Cell(LinearLayout(LinearLayout.VERTICAL)).setPadding(96, 96, 96, 0)
    }

    fun getRootCell(): Cell {
        return root
    }

    //本地应用Adapter
    private lateinit var deviceItemAdapter: CellDataBinding.UpdateAdapter<DeviceInfo>

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
        if (getCategory() == FileCategory.Video.ordinal) {
            isPlayedRefresh = true
        }
        var item = p0.holder
        if (item is BaseData) {
            FileManagerApplication.getInstance().reportFileCevent(item.path!!)
            if (item.category == FileCategory.Video.ordinal) {
                MediaOpenUtil.playVideo(context, item, item.path!!)
            }
        }
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

        if (data.isNullOrEmpty()) { // 空数据
            root.addCell(
                CellCreateHelper.textCell(
                    context.resources.getString(R.string.empty),
                    R.style.font_crosshead_medium_2
                )
            )
        } else {
            var his = data?.filter {
                (it as BaseData).videoType == 0
            }
            if (!his.isNullOrEmpty()) {
                // 历史记录
                root.addCell(
                    Cell(
                        TextComponent()
                            .setText(getString(R.string.video_recent_history))
                            .setStyle(R.style.font_body_bold_4)
                    ),
                    LinearLayout.Params(112, 28, Gravity.LEFT)
                )
                var historyContainer = Cell(LinearLayout(LinearLayout.VERTICAL, GROUP_SIZE, 72, 8))

                his?.forEachIndexed { _, any ->
                    historyContainer.addCell(
                        CellCreateHelper.getItemCell(
                            any,
                            onClickListener,
                            longPressListener
                        )
                    )
                }
                root.addCell(
                    historyContainer,
                    LinearLayout.Params(
                        LinearLayout.Params.FULL,
                        LinearLayout.Params.WRAP
                    ).setMarginTop(48)
                )
            }

            var local = data?.filter {
                (it as BaseData).videoType == 2
            }
            if (!local.isNullOrEmpty()) {
                // 本地影片
                root.addCell(
                    Cell(
                        TextComponent().setText(getString(R.string.video_all_video))
                            .setStyle(R.style.font_body_bold_4)
                    ),
                    LinearLayout.Params(112, 28, Gravity.LEFT).setMarginTop(96)
                )

                var localContainer = Cell(LinearLayout(LinearLayout.VERTICAL, GROUP_SIZE, 72, 8))

                local?.forEachIndexed { _, any ->
                    localContainer.addCell(
                        CellCreateHelper.getItemCell(
                            any,
                            onClickListener,
                            longPressListener
                        )
                    )
                }
                root.addCell(
                    localContainer, LinearLayout.Params(
                        LinearLayout.Params.FULL,
                        LinearLayout.Params.WRAP
                    ).setMarginTop(48)
                )
            }
        }
    }

    /**
     * 初始化添加设备按钮
     */
    private fun initAddDevice() {
        val deviceInfo = DeviceInfo()
        deviceInfo.deviceType = DeviceInfo.DeviceCategory.AddDevice.ordinal
        deviceInfo.deviceName = getString(R.string.add_device_add)
        deviceInfo.rootPath = "add Device"
        addDevice(deviceInfo)
    }

    /**
     * 初始化U盘设备
     */
    private fun initUsbDevice() {
        MountHelper.loadUSBDevice(context)
        val usbList: MutableList<DeviceInfo> = MountHelper.getLocalDeviceList()
        for (deviceInfo in usbList) {
            addDevice(deviceInfo)
        }
    }

    /**
     * 初始化 samba 设备
     */
    private fun initSambaDevice() {
        val sambaInfoList = ResourceHelper.getAllSambaDeviceInfo()
        if (sambaInfoList != null) {
            for (deviceInfo in sambaInfoList) {
                addDevice(deviceInfo)
            }
        }
    }

    /**
     * 初始化dlna设备
     */
    private fun initDlnaDevice() {
        mAllShareProxy = AllShareProxy.getInstance(context)
        mBroadcastFactory = DMSDeviceBrocastFactory(context)
        mBroadcastFactory.registerListener(this)
        mAllShareProxy.resetSearch()
        mHandler.postDelayed(Runnable {
            if (mAllShareProxy != null) {
                mAllShareProxy.startSearch()
            }
        }, DELAY)
    }

    /**
     * 添加设备
     *
     * @param deviceInfo
     */
    private fun addDevice(deviceInfo: DeviceInfo?): Boolean {
        if (deviceInfo != null && !deviceDataList.contains(deviceInfo)) {
            deviceDataList.add(deviceInfo)
            return true
        }
        return false
    }

    /**
     * DLNA设备发生变化
     *
     * @param isSelDeviceChange
     */
    override fun onDeviceChange(isSelDeviceChange: Boolean) {
        LogUtils.e("device page", "isSelDeviceChange == $isSelDeviceChange")
        updateDlnaDeviceList()
    }

    /**
     * 更新DLNA设备
     */
    private fun updateDlnaDeviceList() {
        var dataIsChange = false
        val iterator: MutableIterator<DeviceInfo> =
            deviceDataList.iterator()
        while (iterator.hasNext()) {
            val deviceInfo = iterator.next()
            if (deviceInfo.deviceType == DeviceInfo.DeviceCategory.Dlna.ordinal) {
                iterator.remove()
                dataIsChange = true
            }
        }
        val list: List<Device>? = mAllShareProxy.dmsDeviceList
        if (list != null && list.isNotEmpty()) {
            for (dlna in list) {
                val device = DeviceInfo()
                val dlnaDeviceName: String = dlna.getFriendlyName()
                Log.e("findDlnaDevice", ">>>>>>>>>>>>>>>>>>>>>>name:$dlnaDeviceName")
                device.deviceName = dlnaDeviceName
                device.deviceType = DeviceInfo.DeviceCategory.Dlna.ordinal
                device.rootPath = dlnaDeviceName
                dataIsChange = addDevice(device)
            }
        }
        if (dataIsChange) {
            LogUtils.e("device page", "updateDlnaDeviceList to setDatas")
//            setDatas(deviceDataList)
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