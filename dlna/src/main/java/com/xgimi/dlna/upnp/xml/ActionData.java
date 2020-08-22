package com.xgimi.dlna.upnp.xml;

import com.xgimi.dlna.upnp.control.ActionListener;
import com.xgimi.dlna.upnp.control.ControlResponse;

/**
 * author : joker.peng
 * e-mail : joker.peng@xgimi.com
 * date   : 2020/8/4 9:25
 * desc   :
 */
public class ActionData extends NodeData {
    public ActionData() {
    }

    ////////////////////////////////////////////////
    // ActionListener
    ////////////////////////////////////////////////

    private ActionListener actionListener = null;

    public ActionListener getActionListener() {
        return actionListener;
    }

    public void setActionListener(ActionListener actionListener) {
        this.actionListener = actionListener;
    }

    ////////////////////////////////////////////////
    // ControlResponse
    ////////////////////////////////////////////////

    private ControlResponse ctrlRes = null;

    public ControlResponse getControlResponse() {
        return ctrlRes;
    }

    public void setControlResponse(ControlResponse res) {
        ctrlRes = res;
    }

}
