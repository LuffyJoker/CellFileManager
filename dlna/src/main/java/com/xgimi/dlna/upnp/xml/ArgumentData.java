package com.xgimi.dlna.upnp.xml;

/**
 * author : joker.peng
 * e-mail : joker.peng@xgimi.com
 * date   : 2020/8/4 9:38
 * desc   :
 */
public class ArgumentData extends NodeData {
    public ArgumentData() {
    }

    ////////////////////////////////////////////////
    // value
    ////////////////////////////////////////////////

    private String value = "";

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

}


