package com.xgimi.dlna.upnp.xml;

import com.xgimi.dlna.xml.Node;

/**
 * author : joker.peng
 * e-mail : joker.peng@xgimi.com
 * date   : 2020/8/3 18:03
 * desc   :
 */
public class NodeData {
    public NodeData() {
        setNode(null);
    }

    ////////////////////////////////////////////////
    // Node
    ////////////////////////////////////////////////

    private Node node;

    public void setNode(Node node) {
        this.node = node;
    }

    public Node getNode() {
        return node;
    }
}

