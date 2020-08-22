package com.xgimi.filemanager.interfaces

import android.content.Context
import com.xgimi.view.cell.Cell

/**
 *    author : joker.peng
 *    e-mail : joker.peng@xgimi.com
 *    date   : 2020/8/6 19:56
 *    desc   :
 */
interface ICellView {
    fun getRoot(ctx: Context): Cell
    fun onPrepared()
}