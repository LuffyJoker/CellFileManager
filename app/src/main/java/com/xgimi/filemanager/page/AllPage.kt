package com.xgimi.filemanager.page

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import com.blankj.utilcode.util.LogUtils
import com.blankj.utilcode.util.NetworkUtils
import com.blankj.utilcode.util.ToastUtils
import com.xgimi.dlna.proxy.AllShareProxy
import com.xgimi.dlna.proxy.IDeviceChangeListener
import com.xgimi.dlna.upnp.DMSDeviceBrocastFactory
import com.xgimi.dlna.upnp.Device
import com.xgimi.filemanager.*
import com.xgimi.filemanager.bean.DeviceInfo
import com.xgimi.filemanager.bean.DeviceInfo.DeviceCategory
import com.xgimi.filemanager.config.LayoutType
import com.xgimi.filemanager.config.OperationConfigure
import com.xgimi.filemanager.constants.Constants.DEVICE_NAME
import com.xgimi.filemanager.constants.Constants.DEVICE_TYPE
import com.xgimi.filemanager.constants.Constants.DISPLAY_NAME
import com.xgimi.filemanager.constants.Constants.GROUP_SIZE
import com.xgimi.filemanager.constants.Constants.PATH
import com.xgimi.filemanager.constants.Constants.ROOT_PATH_STR
import com.xgimi.filemanager.filehelper.OperationEvent
import com.xgimi.filemanager.helper.CellCreateHelper
import com.xgimi.filemanager.helper.MountHelper
import com.xgimi.filemanager.helper.ResourceHelper
import com.xgimi.filemanager.menus.XgimiMenuItem
import com.xgimi.view.cell.Cell
import com.xgimi.view.cell.CellDataBinding
import com.xgimi.view.cell.CellEvent
import com.xgimi.view.cell.Layout
import com.xgimi.view.cell.component.TextComponent
import com.xgimi.view.cell.layout.Gravity
import com.xgimi.view.cell.layout.LinearLayout
import rx.Observable
import rx.functions.Action0
import rx.functions.Action1

/**
 *    author : joker.peng
 *    e-mail : joker.peng@xgimi.com
 *    date   : 2020/8/6 19:50
 *    desc   :
 */
class AllPage(context: Activity) : BasePage(context), IDeviceChangeListener {

    /**
     * 所有显示的设备
     */
    private var deviceDataList = mutableListOf<DeviceInfo>()

    /**
     * DLNA 工具
     */
    private lateinit var mAllShareProxy: AllShareProxy

    private lateinit var mBroadcastFactory: DMSDeviceBrocastFactory

    /**
     * 点击后缓存点击设备，用于刷新设备大小
     */
    private var mClickLocalDevice: DeviceInfo? = null

    companion object {
        const val TAG_ICON = "TAG_ICON"
        const val TAG_STROKE = "TAG_STROKE"
        const val TAG_NAME = "TAG_NAME"
        const val TAG_DES = "TAG_DES"
        const val TAG_FOCUS = "TAG_FOCUS"
        const val TAG_SHADOW = "TAG_SHADOW"
        const val DELAY = 1000L
    }

    private val root: Cell by lazy {
        Cell(LinearLayout(LinearLayout.VERTICAL, GROUP_SIZE, 72, 48)).setPadding(96, 96, 96, 0)
    }

    fun getRootCell(): Cell {
        return root
    }

    //本地应用Adapter
    private lateinit var deviceItemAdapter: CellDataBinding.UpdateAdapter<DeviceInfo>

    private val longPressListener = object : CellEvent.OnLongPressListener {
        override fun onLongPress(cell: Cell, event: Int): Boolean {
            if (event == KeyEvent.KEYCODE_DPAD_CENTER) {
                return true
            }
            return false
        }
    }

    private val onClickListener: CellEvent.OnClickListener =
        CellEvent.OnClickListener { p0 ->
            mClickLocalDevice = null
            val deviceInfo = p0?.holder as DeviceInfo
            val bundle = Bundle()
            bundle.putString(ROOT_PATH_STR, deviceInfo.rootPath)
            bundle.putString(DEVICE_NAME, deviceInfo.deviceName)
            val deviceType = deviceInfo.deviceType
            if (deviceType == DeviceCategory.Usb.ordinal || deviceType == DeviceCategory.LOCALUSB.ordinal) {
                val intent = Intent()
                intent.setClass(context, UsbDetailActivity::class.java)
                intent.putExtra(PATH, deviceInfo.rootPath)
                intent.putExtra("NAME", deviceInfo.deviceName)
                context.startActivity(intent)
                mClickLocalDevice = deviceInfo
            } else if (deviceType == DeviceCategory.Samba.ordinal) {
                openSambaActivity(deviceInfo)
            } else if (deviceType == DeviceCategory.Dlna.ordinal) {
                val intent = Intent()
                intent.setClass(context, DlnaDetailActivity::class.java)
                intent.putExtra(DEVICE_TYPE, deviceInfo.deviceType)
                intent.putExtra(DISPLAY_NAME, deviceInfo.deviceName)
                val deviceList = mAllShareProxy.dmsDeviceList
                if (deviceList != null && deviceList.size > 0) {
                    for (dlna in deviceList) {
                        if (dlna.friendlyName == deviceInfo.deviceName) {
                            mAllShareProxy.dmsSelectedDevice = dlna
                            LogUtils.e("dlna:name" + dlna.friendlyName)
                            LogUtils.e("dlna:type" + dlna.deviceType)
                            LogUtils.e("dlna:other" + dlna.descriptionFilePath)
                            startActivity(intent)
                            break
                        }
                    }
                }
            } else if (deviceType == DeviceCategory.AddDevice.ordinal) {
                if (!NetworkUtils.isConnected()) {
                    ToastUtils.showShort(R.string.please_link_network)
                } else {
//                    mChooseDeviceDialog = ChooseDeviceDialog(getActivity(), mHandler)
//                    mChooseDeviceDialog.show()
                    val intent = Intent()
                    intent.setClass(context, AddSambaDeviceActivity::class.java)
                    startActivity(intent)
                }
            }
        }

    init {
        getDeviceList()
    }

    private fun getDeviceList() {
        addSubscribe(Observable.create(Observable.OnSubscribe<List<DeviceInfo?>?> { subscriber ->
            try { //搜索挂载设备
                initUsbDevice()
            } catch (e: Exception) {
                e.printStackTrace()
            }
            try { //初始化samba设备
                initSambaDevice()
            } catch (e: Exception) {
                e.printStackTrace()
            }
            try { //初始化DLNA
                initDlnaDevice()
            } catch (e: Exception) {
                e.printStackTrace()
            }
            //添加设备
            initAddDevice()
            subscriber.onCompleted()
        }).subscribe(
            Action1<List<DeviceInfo?>?> { },
            Action1<Throwable> { throwable -> throwable.printStackTrace() }, Action0 {
                LogUtils.e("Device Page", "loadData to setDatas")
                initDeviceListCellView(deviceDataList)
            })
        )
    }

    private fun initDeviceListCellView(deviceList: List<DeviceInfo>) {
        if (deviceList.isEmpty()) {
            //没有数据直接不显示可更新
            return
        }
        deviceList.forEachIndexed { _, deviceInfo ->
            root.addCell(
                CellCreateHelper.getItemCell(
                    deviceInfo,
                    onClickListener,
                    longPressListener
                )
            )
        }
    }

    /**
     * 初始化添加设备按钮
     */
    private fun initAddDevice() {
        val deviceInfo = DeviceInfo()
        deviceInfo.deviceType = DeviceCategory.AddDevice.ordinal
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
            if (deviceInfo.deviceType == DeviceCategory.Dlna.ordinal) {
                iterator.remove()
                dataIsChange = true
            }
        }
        val list: List<Device>? = mAllShareProxy.dmsDeviceList
        if (list != null && list.size > 0) {
            for (dlna in list) {
                val device = DeviceInfo()
                val dlnaDeviceName: String = dlna.getFriendlyName()
                Log.e("findDlnaDevice", ">>>>>>>>>>>>>>>>>>>>>>name:$dlnaDeviceName")
                device.deviceName = dlnaDeviceName
                device.deviceType = DeviceCategory.Dlna.ordinal
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
        mMenu.addMenu(
            XgimiMenuItem(
                OperationEvent.ChangeLayoutType,
                if (OperationConfigure.getLayoutType() == LayoutType.GRID_LAYOUT.ordinal) getString(
                    R.string.list_mode
                ) else getString(R.string.grid_mode)
            )
        )
        mMenu.addMenu(
            XgimiMenuItem(
                OperationEvent.SearchDlna,
                getString(R.string.search_dlna_service)
            )
        )
        return mMenu
    }

    /**
     * 打开 samba 界面
     *
     * @param info
     */
    private fun openSambaActivity(info: DeviceInfo) {
        val intent = Intent()
        intent.setClass(context, SambaDetailActivity::class.java)
        intent.putExtra("PATH", info.rootPath)
        intent.putExtra("NAME", info.deviceName)
        startActivity(intent)
    }
}