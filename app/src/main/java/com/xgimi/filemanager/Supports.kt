package com.xgimi.filemanager;

import com.xgimi.view.cell.Cell;
import com.xgimi.view.cell.FocusManager
import com.xgimi.view.cell.Layout
import com.xgimi.view.cell.action.Animator
import com.xgimi.view.cell.action.FollowAction
import com.xgimi.view.cell.component.TextComponent

class Supports {
    // some supports
}

/**
 * @author liangyi.li
 */
fun FocusManager.setFocusExt(cell: Cell, clearFocus: Boolean = false) {
    cell.render.focusManager.let { fm ->
        if (clearFocus)
            fm.setFocus(null)
        fm.setFocus(fm.findFocusIn(cell))
    }
}

/**
 * 限制TextComponent文字长度
 * （备注：只能在设置完Text之后调用，后面修改text后限制失效）
 * @author liangyi.li
 */
fun TextComponent.setMaxLength(maxSize: Int): TextComponent {
    if (text.length <= maxSize) return this
    text = "${text.substring(0, maxSize + 1)}..."
    return this
}

/**
 * 限制TextComponent文字长度
 * @param maxSize 长度限制
 * @author liangyi.li
 */
fun TextComponent.setText(textStr: String?, maxSize: Int = -1): TextComponent {
    textStr ?: return this
    text = if (maxSize == -1 || textStr.length <= maxSize) {
        textStr
    } else {
        "${textStr.substring(0, maxSize + 1)}..."
    }
    return this
}

/**
 *  special for slider cell move, {@link FollowAction} will copy the target w/h and x/y
 *  the animation will set to cell's holder object
 *  @author dnwang
 */
fun Cell.follow(target: Cell,
                frame: Int = 4,
                withAnim: Boolean = true,
                withLock: Boolean = false,
                withSync: Boolean = false,
                checkVisible: Boolean = true) {
    // 1. clear other type animation
    val holderAction = holder
    if (holderAction is Animator) {
        holderAction.cancel(true)
        holder = null
    }
    // 2. create or continue follow animation
    val action = if (null != holder) holder as FollowAction else null
    val isPlaying = null != action && action.isPlaying
    if (isPlaying) action!!.changeTo(target) // just change target to continue
    if (withAnim && !isEmpty && (!checkVisible || isVisible)) {
        if (!isPlaying) {
            val anim = FollowAction(target, frame, withSync)
            if (withLock) anim.withLock()
            holder = anim.setLifeCycleListener {
                holder = null
            }.start(this)
        }
        // 'changeTo' already with animation
    } else {
        target.updateLocation(true)
        this.offset(target.left - left, target.top - top)
        this.updateLocation()
        this.layoutParams?.apply {
            width = target.width
            height = target.height
        }
        Layout.reLayoutBy(this)
    }
}

/**
 *  cancel all animation in cell's holder object
 *  @author dnwang
 */
fun Cell.cancelHolderAnim(withStop: Boolean = false) {
    holder?.also { action ->
        if (action is FollowAction) {
            action.cancel(withStop)
        } else if (action is Animator) {
            action.cancel(withStop)
        }
        holder = null
    }
}