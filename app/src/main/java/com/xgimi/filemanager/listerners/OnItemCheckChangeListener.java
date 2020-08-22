package com.xgimi.filemanager.listerners;

/**
 * author : joker.peng
 * e-mail : joker.peng@xgimi.com
 * date   : 2020/8/5 14:10
 * desc   : item 选中状态改变监听
 */
public interface OnItemCheckChangeListener {
    void onItemCheckChange(boolean checked, Object data);

    boolean isChecked(Object object);

    boolean isOperationFile(Object object);
}
