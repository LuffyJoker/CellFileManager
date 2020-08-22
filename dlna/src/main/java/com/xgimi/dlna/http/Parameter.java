package com.xgimi.dlna.http;

/**
 * author : joker.peng
 * e-mail : joker.peng@xgimi.com
 * date   : 2020/8/3 17:45
 * desc   :
 */
public class Parameter {
    private String name = new String();
    private String value = new String();

    public Parameter() {
    }

    public Parameter(String name, String value) {
        setName(name);
        setValue(value);
    }

    ////////////////////////////////////////////////
    //	name
    ////////////////////////////////////////////////

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    ////////////////////////////////////////////////
    //	value
    ////////////////////////////////////////////////

    public void setValue(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}


