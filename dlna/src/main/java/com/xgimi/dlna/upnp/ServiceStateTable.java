package com.xgimi.dlna.upnp;

import java.util.Vector;

/**
 * author : joker.peng
 * e-mail : joker.peng@xgimi.com
 * date   : 2020/8/4 9:40
 * desc   :
 */
public class ServiceStateTable extends Vector {
    ////////////////////////////////////////////////
    //	Constants
    ////////////////////////////////////////////////

    public final static String ELEM_NAME = "serviceStateTable";

    ////////////////////////////////////////////////
    //	Constructor
    ////////////////////////////////////////////////

    public ServiceStateTable() {
    }

    ////////////////////////////////////////////////
    //	Methods
    ////////////////////////////////////////////////

    public StateVariable getStateVariable(int n) {
        return (StateVariable) get(n);
    }
}


