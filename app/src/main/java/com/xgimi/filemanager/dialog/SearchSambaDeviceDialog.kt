package com.xgimi.filemanager.dialog

import android.app.Activity
import android.app.Dialog
import android.os.Bundle
import android.os.Handler
import android.view.Gravity
import android.view.WindowManager
import com.xgimi.filemanager.R
import com.xgimi.filemanager.page.SearchSambaDevicePage
import com.xgimi.view.cell.CellView

class SearchSambaDeviceDialog : Dialog {

    var cellView: CellView
    var page: SearchSambaDevicePage

    constructor(context: Activity, handler: Handler) : super(context, R.style.New_Add_DevieDialog) {

        cellView = CellView(context)
        cellView.root = SearchSambaDevicePage(context,handler).apply {
            page = this
        }.root

        page.loadData()

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        cellView.init(context)
        setContentView(cellView)
        refreshDialogWidth(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT)
    }

    private fun refreshDialogWidth(w: Int, h: Int) {
        val window = window
        window!!.setGravity(Gravity.CENTER)
        window.setDimAmount(0.65f)
        val wl = window.attributes
        wl.width = w
        wl.height = h
        onWindowAttributesChanged(wl)
    }

    override fun dismiss() {
        super.dismiss()
        page.stop()
    }

}