package com.xgimi.dlna.upnp.xml;

import com.xgimi.dlna.upnp.event.SubscriberList;
import com.xgimi.dlna.utils.ListenerList;
import com.xgimi.dlna.xml.Node;

/**
 * author : joker.peng
 * e-mail : joker.peng@xgimi.com
 * date   : 2020/8/3 18:02
 * desc   :
 */
public class ServiceData extends NodeData {
    public ServiceData() {
    }

    ////////////////////////////////////////////////
    // controlActionListenerList
    ////////////////////////////////////////////////

    private ListenerList controlActionListenerList = new ListenerList();

    public ListenerList getControlActionListenerList() {
        return controlActionListenerList;
    }

    ////////////////////////////////////////////////
    // scpdNode
    ////////////////////////////////////////////////

    private Node scpdNode = null;

    public Node getSCPDNode() {
        return scpdNode;
    }

    public void setSCPDNode(Node node) {
        scpdNode = node;
    }

    ////////////////////////////////////////////////
    // SubscriberList
    ////////////////////////////////////////////////

    private SubscriberList subscriberList = new SubscriberList();

    public SubscriberList getSubscriberList() {
        return subscriberList;
    }

    ////////////////////////////////////////////////
    // SID
    ////////////////////////////////////////////////

    private String descriptionURL = "";

    public String getDescriptionURL() {
        return descriptionURL;
    }

    public void setDescriptionURL(String descriptionURL) {
        this.descriptionURL = descriptionURL;
    }

    ////////////////////////////////////////////////
    // SID
    ////////////////////////////////////////////////

    private String sid = "";

    public String getSID() {
        return sid;
    }

    public void setSID(String id) {
        sid = id;
    }

    ////////////////////////////////////////////////
    // Timeout
    ////////////////////////////////////////////////

    private long timeout = 0;

    public long getTimeout() {
        return timeout;
    }

    public void setTimeout(long value) {
        timeout = value;
    }

}


