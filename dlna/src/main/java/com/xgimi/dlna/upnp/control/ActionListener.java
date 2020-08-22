package com.xgimi.dlna.upnp.control;

import com.xgimi.dlna.upnp.Action;

/**
 * author : joker.peng
 * e-mail : joker.peng@xgimi.com
 * date   : 2020/8/4 9:26
 * desc   :
 */
public interface ActionListener {
    boolean actionControlReceived(Action action);
}
