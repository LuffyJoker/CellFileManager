package com.xgimi.filemanager.listerners;

import android.view.View;

import com.xgimi.filemanager.menus.XgimiMenuItem;

/**
 * author : joker.peng
 * e-mail : joker.peng@xgimi.com
 * date   : 2020/7/31 10:58
 * desc   : 菜单项焦点改变监听
 */
public interface OnMenuFocusChangeListener {
    void onMenuFocusChange(View view, XgimiMenuItem xgimiMenuItem, int position, boolean hasFocus);
}
