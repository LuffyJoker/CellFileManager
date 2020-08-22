package com.xgimi.dlna.upnp;

import java.util.Iterator;
import java.util.Vector;

/**
 * author : joker.peng
 * e-mail : joker.peng@xgimi.com
 * date   : 2020/8/4 9:36
 * desc   :
 */
public class AllowedValueList extends Vector {
    ////////////////////////////////////////////////
    //	Constants
    ////////////////////////////////////////////////

    public final static String ELEM_NAME = "allowedValueList";


    ////////////////////////////////////////////////
    //	Constructor
    ////////////////////////////////////////////////

    public AllowedValueList() {
    }

    public AllowedValueList(String[] values) {
        for (int i = 0; i < values.length; i++) {
            add(new AllowedValue(values[i]));
        }

    }


    ////////////////////////////////////////////////
    //	Methods
    ////////////////////////////////////////////////

    public AllowedValue getAllowedValue(int n) {
        return (AllowedValue) get(n);
    }

    public boolean isAllowed(String v) {
        for (Iterator i = this.iterator(); i.hasNext(); ) {
            AllowedValue av = (AllowedValue) i.next();
            if (av.getValue().equals(v)) {
                return true;
            }
        }
        return false;
    }
}

