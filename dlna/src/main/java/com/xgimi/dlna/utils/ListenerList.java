package com.xgimi.dlna.utils;

import java.util.Vector;

/**
 * author : joker.peng
 * e-mail : joker.peng@xgimi.com
 * date   : 2020/8/3 17:40
 * desc   :
 */
public class ListenerList extends Vector {
    public boolean add(Object obj) {
        if (0 <= indexOf(obj)) {
            return false;
        }
        return super.add(obj);
    }
}
