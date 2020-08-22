package com.xgimi.dlna.xml;

/**
 * author : joker.peng
 * e-mail : joker.peng@xgimi.com
 * date   : 2020/8/3 17:48
 * desc   :
 */
public class Attribute {
    private String name = new String();
    private String value = new String();

    public Attribute() {
    }

    public Attribute(String name, String value) {
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


