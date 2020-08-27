package com.xgimi.filemanager.page

import android.app.Activity
import android.content.Intent
import android.os.Message
import android.view.KeyEvent
import com.blankj.utilcode.util.LogUtils
import com.blankj.utilcode.util.NetworkUtils
import com.blankj.utilcode.util.StringUtils
import com.blankj.utilcode.util.ToastUtils
import com.xgimi.filemanager.ConnectDeviceActivity
import com.xgimi.filemanager.R
import com.xgimi.filemanager.bean.DeviceInfo
import com.xgimi.filemanager.constants.Constants
import com.xgimi.filemanager.helper.CellCreateHelper
import com.xgimi.filemanager.samba.ShareClientController
import com.xgimi.gimiskin.sdk.SkinEngine.getColor
import com.xgimi.samba.SmbDevice
import com.xgimi.samba.search.SmbSearcher
import com.xgimi.view.cell.Cell
import com.xgimi.view.cell.CellEvent
import com.xgimi.view.cell.component.ColorComponent
import com.xgimi.view.cell.layout.FrameLayout
import com.xgimi.view.cell.layout.Gravity
import com.xgimi.view.cell.layout.LinearLayout
import com.xgimi.view.cell.utils.SimpleFocusAdapter

/**
 *    author : joker.peng
 *    e-mail : joker.peng@xgimi.com
 *    date   : 2020/8/22 11:44
 *    desc   :
 */
class SearchSambaDevicePage(context: Activity) : BasePage(context),
    SmbSearcher.OnSmbSearchListener {

    private val ADD_DEVICE_CODE = 1001
    val EXTRA_IP = "ip"

    val root: Cell = Cell(LinearLayout(LinearLayout.VERTICAL, true))
    var container: Cell =
        Cell(LinearLayout(LinearLayout.VERTICAL, Constants.GROUP_SIZE, 72, 8)).apply {
            addComponent(ColorComponent().setColor(getColor(R.color.color_bg_pure_0)))
        }.setPadding(96, 0, 96, 0)
            .setMask(true)
            .setFocusAdapter(SimpleFocusAdapter().serial(true, false, true, false))
            .setTag("sambaDeviceContainer")

    var noNet: Cell
    var progress: Cell

    private val smbSearcher = SmbSearcher(context, this)
    private var isSearching = false
    private val mSearchResult = mutableListOf<DeviceInfo>()
    private val localIp: String = NetworkUtils.getIPAddress(true)

    private val onClickListener: CellEvent.OnClickListener = CellEvent.OnClickListener { p0 ->
        var des = p0.holder as String
        if (des == context.resources.getString(R.string.refresh)) { // 刷新
            if (!isSearching) {
                mSearchResult.clear()
                // 清空列表
                if (!container.isEmpty) {
                    container.removeAllCells()
                }
                isSearching = true
                searchSambaDevice()
            } else {
                ToastUtils.showShort(R.string.scanning_device)
            }

        } else { // 手动连接
            if (!NetworkUtils.isConnected()) {
                ToastUtils.showShort(R.string.please_link_network)
            } else {
                // 手动连接
                showAddDeviceView(null)
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


    init {

        noNet = CellCreateHelper.textCell(
            context.resources.getString(R.string.network_notlink),
            R.style.font_crosshead_medium_2
        ).setTag("noNet")

        progress = CellCreateHelper.getProgressCell().setTag("progress")

        root.addCell(
            CellCreateHelper.getTitleCell(
                context.resources.getString(R.string.add_network_device),
                onClickListener
            ),
            LinearLayout.Params(
                LinearLayout.Params.FULL,
                LinearLayout.Params.WRAP,
                Gravity.TOP
            ).setMargin(96, 96, 96, 0)
        )

        root.addCell(
            container,
            LinearLayout.Params(
                LinearLayout.Params.FULL,
                LinearLayout.Params.FULL
            ).setMarginTop(190)
        )
    }

    fun loadData() {
        isSearching = true
        searchSambaDevice()
    }

    private fun searchSambaDevice() {

        if (root.findCellByTag("noNet") != null) {
            root.removeCell(noNet)
        }
        if (root.findCellByTag("progress") != null) {
            root.removeCell(progress)
        }

        if (!NetworkUtils.isConnected()) {
            // 显示无网络
            root.addCell(noNet)
        } else {
            smbSearcher.startSearchSmbDevice()
            // 显示设备扫描中 progress R.string.searching_device
            root.addCell(
                progress,
                FrameLayout.Params(FrameLayout.Params.WRAP, FrameLayout.Params.WRAP, Gravity.CENTER)
            )
        }
    }

    override fun onSearch(status: Int, device: SmbDevice?) {
        mHandler.post(Runnable {
            when {
                SmbSearcher.OnSmbSearchListener.UPDATE_DEVICE_ADD === status -> {
                    val deviceInfo = DeviceInfo(device!!)
                    val ip = deviceInfo.ip
                    LogUtils.e("ping onDeviceAdd=$ip")
                    if (!StringUtils.isEmpty(localIp) && localIp == ip) {
                        return@Runnable
                    }
                    mSearchResult.add(deviceInfo)
                    // insertData , 更新列表
                    container.addCell(
                        CellCreateHelper.getItemCell(
                            deviceInfo,
                            CellEvent.OnClickListener {
                                if (it.holder is DeviceInfo) {
                                    val ip = (it.holder as DeviceInfo).ip
                                    LogUtils.i("JSmb", "try to mount")
//                                    mountSamba(ip, "", "", true)
                                    ShareClientController.signInSamba(ip,"","")

                                }
                            },
                            CellEvent.OnLongPressListener { p0, p1 ->


                                false
                            },
                            false
                        )
                    )
                }
                SmbSearcher.OnSmbSearchListener.UPDATE_DEVICE_DONE === status -> {
                    isSearching = false
                    // 隐藏加载 progress
                    if (root.findCellByTag("progress") != null) {
                        root.removeCell(progress)
                    }
                    ToastUtils.showShort(R.string.samba_scan_over)
                }
                else -> {
                    isSearching = false
                    // 隐藏加载 progress
                }
            }
        })
    }

    /**
     * 显示添加设备界面
     *
     * @param ip
     */
    private fun showAddDeviceView(ip: String?) {
        val intent = Intent(context, ConnectDeviceActivity::class.java)
        intent.putExtra(EXTRA_IP, ip)
        context.startActivityForResult(
            intent,
            ADD_DEVICE_CODE
        )
    }

    fun stop() {
        smbSearcher.stop()
    }
}