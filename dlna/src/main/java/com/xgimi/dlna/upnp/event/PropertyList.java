package com.xgimi.dlna.upnp.event;

import java.util.Vector;

/**
 * author : joker.peng
 * e-mail : joker.peng@xgimi.com
 * date   : 2020/8/4 9:47
 * desc   :
 */
public class PropertyList extends Vector {
    ////////////////////////////////////////////////
    //	Constants
    ////////////////////////////////////////////////

    public final static String ELEM_NAME = "PropertyList";

    ////////////////////////////////////////////////
    //	Constructor
    ////////////////////////////////////////////////

    public PropertyList() {
    }

    ////////////////////////////////////////////////
    //	Methods
    ////////////////////////////////////////////////

    public Property getProperty(int n) {
        return (Property) get(n);
    }
}


