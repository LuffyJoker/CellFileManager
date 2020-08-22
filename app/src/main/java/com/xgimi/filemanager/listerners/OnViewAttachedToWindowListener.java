package com.xgimi.filemanager.listerners;

import androidx.recyclerview.widget.RecyclerView;

/**
 * author : joker.peng
 * e-mail : joker.peng@xgimi.com
 * date   : 2020/7/31 11:00
 * desc   : View 添加到窗口的监听
 */
public interface OnViewAttachedToWindowListener {
    void onViewAttachedToWindow(RecyclerView.ViewHolder viewHolder);
}
