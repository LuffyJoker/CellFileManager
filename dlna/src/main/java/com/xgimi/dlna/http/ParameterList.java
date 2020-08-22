package com.xgimi.dlna.http;

import java.util.Vector;

/**
 * author : joker.peng
 * e-mail : joker.peng@xgimi.com
 * date   : 2020/8/3 17:44
 * desc   :
 */
public class ParameterList extends Vector {
    public ParameterList() {
    }

    public Parameter at(int n) {
        return (Parameter) get(n);
    }

    public Parameter getParameter(int n) {
        return (Parameter) get(n);
    }

    public Parameter getParameter(String name) {
        if (name == null) {
            return null;
        }

        int nLists = size();
        for (int n = 0; n < nLists; n++) {
            Parameter param = at(n);
            if (name.compareTo(param.getName()) == 0) {
                return param;
            }
        }
        return null;
    }

    public String getValue(String name) {
        Parameter param = getParameter(name);
        if (param == null) {
            return "";
        }
        return param.getValue();
    }
}


