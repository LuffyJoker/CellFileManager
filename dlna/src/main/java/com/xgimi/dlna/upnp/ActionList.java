package com.xgimi.dlna.upnp;

import java.util.Vector;

/**
 * author : joker.peng
 * e-mail : joker.peng@xgimi.com
 * date   : 2020/8/4 9:39
 * desc   :
 */
public class ActionList extends Vector {
    ////////////////////////////////////////////////
    //	Constants
    ////////////////////////////////////////////////

    public final static String ELEM_NAME = "actionList";

    ////////////////////////////////////////////////
    //	Constructor
    ////////////////////////////////////////////////

    public ActionList() {
    }

    ////////////////////////////////////////////////
    //	Methods
    ////////////////////////////////////////////////

    public Action getAction(int n) {
        return (Action) get(n);
    }
}


