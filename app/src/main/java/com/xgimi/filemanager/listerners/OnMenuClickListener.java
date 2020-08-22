package com.xgimi.filemanager.listerners;

import android.view.View;

import com.xgimi.filemanager.menus.XgimiMenuItem;

/**
 * author : joker.peng
 * e-mail : joker.peng@xgimi.com
 * date   : 2020/7/31 10:57
 * desc   : 菜单项点击监听
 */
public interface OnMenuClickListener {
    void onMenuClick(View view, XgimiMenuItem xgimiMenuItem);
}
