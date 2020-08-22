package com.xgimi.dlna.upnp.xml;

import com.xgimi.dlna.upnp.control.QueryListener;
import com.xgimi.dlna.upnp.control.QueryResponse;

/**
 * author : joker.peng
 * e-mail : joker.peng@xgimi.com
 * date   : 2020/8/4 9:34
 * desc   :
 */
public class StateVariableData extends NodeData {
    public StateVariableData() {
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

    ////////////////////////////////////////////////
    // QueryListener
    ////////////////////////////////////////////////

    private QueryListener queryListener = null;

    public QueryListener getQueryListener() {
        return queryListener;
    }

    public void setQueryListener(QueryListener queryListener) {
        this.queryListener = queryListener;
    }

    ////////////////////////////////////////////////
    // QueryResponse
    ////////////////////////////////////////////////

    private QueryResponse queryRes = null;

    public QueryResponse getQueryResponse() {
        return queryRes;
    }

    public void setQueryResponse(QueryResponse res) {
        queryRes = res;
    }

}


