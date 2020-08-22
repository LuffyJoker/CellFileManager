package com.xgimi.dlna.xml;

import java.util.Vector;

/**
 * author : joker.peng
 * e-mail : joker.peng@xgimi.com
 * date   : 2020/8/3 17:48
 * desc   :
 */
public class AttributeList extends Vector {
    public AttributeList() {
    }

    public Attribute getAttribute(int n) {
        return (Attribute) get(n);
    }

    public Attribute getAttribute(String name) {
        if (name == null) {
            return null;
        }

        int nLists = size();
        for (int n = 0; n < nLists; n++) {
            Attribute elem = getAttribute(n);
            if (name.compareTo(elem.getName()) == 0) {
                return elem;
            }
        }
        return null;
    }
}
