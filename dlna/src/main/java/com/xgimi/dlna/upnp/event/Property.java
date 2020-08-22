package com.xgimi.dlna.upnp.event;

/**
 * author : joker.peng
 * e-mail : joker.peng@xgimi.com
 * date   : 2020/8/4 9:46
 * desc   :
 */
public class Property {
    ////////////////////////////////////////////////
    //	Constructor
    ////////////////////////////////////////////////

    public Property() {
    }

    ////////////////////////////////////////////////
    //	name
    ////////////////////////////////////////////////

    private String name = "";

    public String getName() {
        return name;
    }

    public void setName(String val) {
        if (val == null) {
            val = "";
        }
        name = val;
    }

    ////////////////////////////////////////////////
    //	value
    ////////////////////////////////////////////////

    private String value = "";

    public String getValue() {
        return value;
    }

    public void setValue(String val) {
        if (val == null) {
            val = "";
        }
        value = val;
    }
}

