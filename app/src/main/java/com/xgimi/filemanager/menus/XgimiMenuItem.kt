package com.xgimi.filemanager.menus

import com.xgimi.filemanager.filehelper.OperationEvent

/**
 *    author : joker.peng
 *    e-mail : joker.peng@xgimi.com
 *    date   : 2020/8/10 18:32
 *    desc   : 菜单对象
 */
class XgimiMenuItem {
    var parentXgimiMenuItem: XgimiMenuItem? = null
    var name: String? = null
    var operationType = OperationEvent.OPERATION_NULL
    var selectPosition = 0
    var isEmpty = false
    var subMenus = mutableListOf<XgimiMenuItem>()

    constructor()

    constructor(operationType: Int, name: String?) {
        this.operationType = operationType
        this.name = name
    }

    constructor(isEmpty: Boolean) {
        this.isEmpty = isEmpty
    }

    fun addMenu(xgimiMenuItem: XgimiMenuItem) {
        subMenus.add(xgimiMenuItem)
    }

    fun addAllMenus(xgimiMenuItems: List<XgimiMenuItem>) {
        subMenus.addAll(xgimiMenuItems)
    }

    private fun getMaxSubCount(): Int {
        return getMaxSubCount(this)
    }

    private fun getMaxSubCount(xgimiMenuItem: XgimiMenuItem): Int {
        var count = 0
        for (i in xgimiMenuItem.subMenus.indices) {
            count =
                if (xgimiMenuItem.subMenus[i].subMenus.size > 0) getMaxSubCount(xgimiMenuItem.subMenus[i]) else {
                    val subCount = xgimiMenuItem.subMenus[i].subMenus.size
                    if (subCount > count) subCount else count
                }
        }
        return if (count > xgimiMenuItem.subMenus.size) count else xgimiMenuItem.subMenus.size
    }

    fun complementMenu() {
        complementMenu(getMaxSubCount(), this)
    }

    private fun complementMenu(count: Int, xgimiMenuItem: XgimiMenuItem) {
        if (xgimiMenuItem.subMenus.size == 0) return
        for (i in xgimiMenuItem.subMenus.indices) {
            if (xgimiMenuItem.subMenus[i].subMenus.size > 0) complementMenu(
                count,
                xgimiMenuItem.subMenus[i]
            )
        }
        val index: Int = (count - xgimiMenuItem.subMenus.size) / 2
        for (i in 0 until index) xgimiMenuItem.subMenus.add(i, XgimiMenuItem(true))
        val size: Int = xgimiMenuItem.subMenus.size
        for (i in size until count) xgimiMenuItem.subMenus.add(i, XgimiMenuItem(true))
    }

    fun clear() {
        parentXgimiMenuItem = null
        selectPosition = 0
        subMenus.clear()
    }
}