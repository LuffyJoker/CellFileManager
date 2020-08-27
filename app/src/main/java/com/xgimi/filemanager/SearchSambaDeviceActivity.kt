package com.xgimi.filemanager

import android.os.Bundle
import com.xgimi.filemanager.page.SearchSambaDevicePage

class SearchSambaDeviceActivity : BaseActivity() {


    lateinit var page: SearchSambaDevicePage

    override fun initialized(arg0: Bundle?) {

        cellView.root = SearchSambaDevicePage(this).apply {
            page = this
        }.root

        page.loadData()

//        cellView?.viewTreeObserver?.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
//            override fun onGlobalLayout() {
//                cellView?.viewTreeObserver?.removeOnGlobalLayoutListener(this)
//                cellView.root.requestFocus()
//            }
//        })
    }

    override fun onDestroy() {
        super.onDestroy()
        page.stop()
    }
}