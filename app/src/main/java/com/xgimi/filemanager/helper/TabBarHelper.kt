package com.xgimi.filemanager.helper

import com.xgimi.filemanager.R
import com.xgimi.view.cell.*
import com.xgimi.view.cell.action.Animator
import com.xgimi.view.cell.component.ColorComponent
import com.xgimi.view.cell.component.TextComponent
import com.xgimi.view.cell.theme.DynamicResource

/**
 *    author : joker.peng
 *    e-mail : joker.peng@xgimi.com
 *    date   : 2020/8/6 17:26
 *    desc   :
 */
class TabBarHelper(
    private val tabBar: Cell,
    private val tabSlider: Cell? = null
) {

    var onThemeChanged: ((isDarkTheme: Boolean) -> Unit)? = null

    /**
     * 当前焦点 上焦永远是一个颜色
     */
    private val focusColor = Supports.getColor(R.color.color_bg_pure_11)

    /**
     * 下焦 跟随主题 可能是 黑/灰白
     */
    private val unFocusColor = DynamicResource.create(R.color.font_crosshead_bold_2)

    private val sliderFocusColor = DynamicResource.create(R.color.color_brand_1)

    private var forceSliderColor = false
    private var currentSliderColor: Int = sliderFocusColor.color

    private var forceDarkTheme = false

    companion object {
        private const val SWITCH_DELAY = 100L
    }

    private var gotFocusAction: Runnable? = null

    val focusChangeDelegate = CellEvent.OnFocusChangeListener { tab, dir ->
        if (tab.isFocused) {
            if (null != gotFocusAction) Threads.removeInMain(gotFocusAction)
            gotFocusAction = Runnable {
                gotFocusAction = null // clear self
                onTabGotFocus(tab)
                // 如果上焦的是强制暗主题，则需立即设置所有tab
//                if (forceDarkTheme || oldState != forceDarkTheme) {
//                    if (forceDarkTheme) applyAllTabToDarkTheme()
//                    else applyAllTabToCurrentTheme()
//                }
            }
            Threads.postToMain(gotFocusAction, SWITCH_DELAY)
        } else {
            if (FocusManager.DIR_DOWN != dir || !forceDarkTheme) {
                onTabLoseFocus(tab)
            }
        }
    }

    /**
     * Tab 获取焦点
     */
    private fun onTabGotFocus(tab: Cell) {
        tab.findComponent<TextComponent>(0).setColor(focusColor, Animator.DURATION_FAST0)
        // 滑块随主题切换颜色
        setSliderColor(sliderFocusColor.color)
    }

    /**
     * Tab 失去焦点
     */
    private fun onTabLoseFocus(tab: Cell) {
        tab.findComponent<TextComponent>(0).setColor(unFocusColor.color, Animator.DURATION_FAST0)
    }

    /**
     * 设置滑块颜色
     */
    private fun setSliderColor(color: Int) {
        if (color != currentSliderColor) {
            tabSlider?.findComponent<ColorComponent>(0)?.setColor(color, Animator.DURATION_FAST0)
            currentSliderColor = color
        }
    }

    fun fillContent(tab: Cell) {
        if (tab.holder !is String) return
        val data = tab.holder as String
        tab.findComponent<TextComponent>(0).text = data
    }
}