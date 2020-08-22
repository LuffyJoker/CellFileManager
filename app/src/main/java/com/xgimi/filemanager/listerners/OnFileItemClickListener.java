package com.xgimi.filemanager.listerners;

import android.view.View;

import androidx.annotation.NonNull;

import com.chad.library.adapter.base.BaseQuickAdapter;

/**
 * author : joker.peng
 * e-mail : joker.peng@xgimi.com
 * date   : 2020/8/3 13:52
 * desc   :
 */
public interface OnFileItemClickListener {
    void onItemClick(@NonNull BaseQuickAdapter<?, ?> adapter, @NonNull View view, int position);
}
