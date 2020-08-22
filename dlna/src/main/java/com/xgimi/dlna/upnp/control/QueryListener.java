package com.xgimi.dlna.upnp.control;

import com.xgimi.dlna.upnp.StateVariable;

/**
 * author : joker.peng
 * e-mail : joker.peng@xgimi.com
 * date   : 2020/8/4 9:34
 * desc   :
 */
public interface QueryListener {
    boolean queryControlReceived(StateVariable stateVar);
}
