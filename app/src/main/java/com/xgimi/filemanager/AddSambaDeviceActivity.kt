package com.xgimi.filemanager

import android.os.Bundle
import android.view.ViewTreeObserver
import com.xgimi.filemanager.page.AddSambaDevicePage
import com.xgimi.filemanager.page.AllPage

class AddSambaDeviceActivity : BaseActivity() {


    lateinit var page: AddSambaDevicePage

    override fun createCell() {
        cellView.root = AddSambaDevicePage(this).apply {
            page = this
        }.root

//        cellView?.viewTreeObserver?.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
//            override fun onGlobalLayout() {
//                cellView?.viewTreeObserver?.removeOnGlobalLayoutListener(this)
//                cellView.root.requestFocus()
//            }
//        })
    }

    override fun initData() {
        page.loadData()
    }

    override fun initView(savedInstanceState: Bundle?) {

    }
}