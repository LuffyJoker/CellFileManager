package com.xgimi.filemanager

import android.os.Bundle
import com.xgimi.filemanager.filehelper.OperationEvent
import com.xgimi.filemanager.listerners.OnMenuClickListener
import com.xgimi.filemanager.menus.XgimiMenuItem
import com.xgimi.filemanager.page.SambaDetailPage

class SambaDetailActivity : BaseActivity() {

    lateinit var page: SambaDetailPage
    private var isFromSearch = false
    private lateinit var rootPath: String
    private lateinit var deviceName: String


    override fun initialized(arg0: Bundle?) {

        rootPath = intent.getStringExtra("PATH")
        deviceName = intent.getStringExtra("NAME")
        isFromSearch = intent.getBooleanExtra("isFromSearch", false)

        cellView.root = SambaDetailPage(this, rootPath, rootPath, deviceName).apply {
            page = this
        }.root

    }

    override fun initMenu(isEdit: Boolean) {
        super.initMenu(isEdit)
        if (page != null) {
            mXgimiMenuItem?.subMenus?.clear()
            if (!isFromSearch) {
                mXgimiMenuItem?.addMenu(
                    XgimiMenuItem(
                        OperationEvent.Refresh,
                        resources.getString(R.string.refresh)
                    )
                )
            }
            if (!isEdit) {
                mXgimiMenuItem?.addAllMenus(page.initMenus().subMenus)
            } else {
                mXgimiMenuItem?.addAllMenus(page.getEditMenu().subMenus)
            }
        }
        setOnMenuClickListener(OnMenuClickListener { _, menu ->
            var isClose: Boolean = if (menu.operationType === OperationEvent.Refresh) {
                // todo 刷新samba页面数据
                true
            } else {
                page.onMenuClicked(menu)
            }
            if (isClose) {
                hideMenu()
            }
        })
    }
}