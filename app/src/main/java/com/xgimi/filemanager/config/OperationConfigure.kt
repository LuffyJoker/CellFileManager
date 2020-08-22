package com.xgimi.filemanager.config

import com.blankj.utilcode.util.SPUtils
import com.xgimi.filemanager.constants.Constants
import com.xgimi.filemanager.filehelper.OperationEvent
import com.xgimi.filemanager.helper.Comparators
import java.util.*

/**
 *    author : joker.peng
 *    e-mail : joker.peng@xgimi.com
 *    date   : 2020/8/7 11:06
 *    desc   :
 */
object OperationConfigure {

    const val OPERATION_CONFIGURE_LAYOUT_TYPE = 1
    const val OPERATION_CONFIGURE_DISPLAY_MODE = 2
    const val OPERATION_CONFIGURE_SORT_ORDER = 3
    /**
     * 是否是操作模式
     */
    var isSelectOperationMode = false
        set(value) {
            field = value
            mSelectObjectList.clear()
        }
    /**
     * 列表模式、图标模式
     */
    private var mLayoutType = 0
    /**
     * 目录模式、观影模式
     */
    private var mDisplayMode = 0
    /**
     * 0：名称排序 1、时间排序
     */
    private var mSortOrder = 0
    private val mOnOperationConfigureChangeListener: MutableList<OnOperationConfigureChangeListener> =
        ArrayList()

    /**
     * 操作选中的items
     */
    private val mSelectObjectList: MutableList<Any> = ArrayList()

    /**
     * 当前操作
     */
    private var mFileOperationEvent: OperationEvent? = null

    init {
        init()
    }

    /**
     * 初始化配置: 列表模式、目录模式等配置
     */
    private fun init() {
        mLayoutType = SPUtils.getInstance().getInt(Constants.LayoutType, 0)
        mDisplayMode = SPUtils.getInstance().getInt(Constants.DisplayMode, 0)
        mSortOrder = SPUtils.getInstance().getInt(Constants.Sortorder, 0)
    }

    /**
     * 改变布局类型(列表模式、图标模式)
     */
    fun changeLayoutType() {
        setLayoutType(if (mLayoutType == LayoutType.GRID_LAYOUT.ordinal) LayoutType.LINEAR_LAYOUT.ordinal else LayoutType.GRID_LAYOUT.ordinal)
    }

    /**
     * 设置布局模式(列表模式、图标模式)
     *
     * @param layoutType
     */
    fun setLayoutType(layoutType: Int) {
        mLayoutType = layoutType
        SPUtils.getInstance().put(Constants.LayoutType, layoutType)
        onOperationConfigureChange(OPERATION_CONFIGURE_LAYOUT_TYPE)
    }

    /**
     * 获取显示模式(列表模式、图标模式)
     *
     * @return
     */
    fun getLayoutType(): Int {
        return mLayoutType
    }

    /**
     * 改变显示模式(目录模式、观影模式)
     */
    fun changeDisplayMode() {
        setDisplayMode(if (mDisplayMode == DisplayMode.FileMode.ordinal) DisplayMode.FolderMode.ordinal else DisplayMode.FileMode.ordinal)
    }

    /**
     * 设置显示模式(目录模式、观影模式)
     *
     * @param displaymode
     */
    fun setDisplayMode(displaymode: Int) {
        mDisplayMode = displaymode
        SPUtils.getInstance().put(Constants.DisplayMode, mDisplayMode)
        onOperationConfigureChange(OPERATION_CONFIGURE_DISPLAY_MODE)
    }

    /**
     * 获取显示模式(目录模式、观影模式)
     *
     * @return
     */
    fun getDisplayMode(): Int {
        return mDisplayMode
    }

    /**
     * 改变排序模式
     */
    fun changeSortOrder() {
        setSortOrder(if (mSortOrder == Comparators.SortMode.NAME.ordinal) Comparators.SortMode.LAST_MODIFIED.ordinal else Comparators.SortMode.NAME.ordinal)
    }

    /**
     * 设置排序模式
     *
     * @param sortOrder
     */
    fun setSortOrder(sortOrder: Int) {
        mSortOrder = sortOrder
        SPUtils.getInstance().put(Constants.Sortorder, mSortOrder)
        onOperationConfigureChange(OPERATION_CONFIGURE_SORT_ORDER)
    }

    /**
     * 获取排序方式
     *
     * @return
     */
    fun getSortOrder(): Int {
        return mSortOrder
    }

    /**
     * 设置当前操作类型
     *
     * @param event
     */
    fun setFileOperationEvent(event: OperationEvent?) {
        mFileOperationEvent = event
    }

    /**
     * 获取当前操作类型
     *
     * @return
     */
    fun getCurrentOperationEvent(): OperationEvent? {
        return mFileOperationEvent
    }

    /**
     * 操作配置发生变化事件
     */
    interface OnOperationConfigureChangeListener {
        fun onOperationConfigureChange(configureType: Int)
    }

    /**
     * 注册操作配置发生变化事件
     *
     * @param listener
     */
    @Synchronized
    fun addOnOperationConfigureChangeListener(listener: OnOperationConfigureChangeListener) {
        if (!mOnOperationConfigureChangeListener.contains(listener)) {
            mOnOperationConfigureChangeListener.add(listener)
        }
    }

    /**
     * 取消操作配置发生变化事件
     *
     * @param listener
     */
    @Synchronized
    fun removeOnOperationConfigureChangeListener(listener: OnOperationConfigureChangeListener?) {
        if (mOnOperationConfigureChangeListener.contains(listener)) {
            mOnOperationConfigureChangeListener.remove(listener)
        }
    }

    /**
     * 分发配置发生变化事件
     *
     * @param type
     */
    private fun onOperationConfigureChange(type: Int) {
        for (i in mOnOperationConfigureChangeListener.indices) {
            mOnOperationConfigureChangeListener[i].onOperationConfigureChange(type)
        }
    }
}