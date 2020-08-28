package com.xgimi.filemanager.page

import android.app.Activity
import android.text.TextUtils
import com.xgimi.filemanager.R
import com.xgimi.filemanager.bean.BaseData
import com.xgimi.filemanager.constants.Constants
import com.xgimi.filemanager.helper.CellCreateHelper
import com.xgimi.view.cell.Cell
import com.xgimi.view.cell.CellEvent
import com.xgimi.view.cell.Threads
import com.xgimi.view.cell.layout.Gravity
import com.xgimi.view.cell.layout.LinearLayout
import com.xgimi.view.cell.utils.SimpleFocusAdapter
import java.util.*

/**
 *    author : joker.peng
 *    e-mail : joker.peng@xgimi.com
 *    date   : 2020/8/19 11:57
 *    desc   :
 */
abstract class FileDetailPage(context: Activity) : BasePage(context) {

    val root: Cell by lazy {
        Cell(LinearLayout(LinearLayout.VERTICAL, true))
    }

    var fileLists = ArrayList<BaseData>() //文件列表

    protected var isFirst = true
    var isLoadData = false

    open fun initialized() {
        initHeaderView()
    }

    // 初始化页面公共元素
    private fun initHeaderView() {
        // 右上角提示
        root.addCell(
            CellCreateHelper.textCell(
                getString(R.string.file_header_notice_pre),
                R.style.font_body_medium_1
            ),
            LinearLayout.Params(656, 28, Gravity.RIGHT_TOP).setMargin(0, 30, 96, 0)
        )

        // 路径
        root.addCell(
            CellCreateHelper.getPathCell(getTitle(), getPath(), R.style.font_crosshead_bold_3),
            LinearLayout.Params(
                LinearLayout.Params.WRAP,
                LinearLayout.Params.WRAP,
                Gravity.LEFT
            ).setMarginLeft(96).setMarginTop(50)
        )

    }

    abstract fun getPath(): String

    abstract fun getTitle(): String

    fun getPosition(path: String): Int {
        if (TextUtils.isEmpty(path)) {
            return 0
        }
        for (i in fileLists.indices) {
            if (path == fileLists[i].path) {
                return i
            }
        }
        return 0
    }
}