package com.xgimi.dlna.xml;

import java.util.Vector;

/**
 * author : joker.peng
 * e-mail : joker.peng@xgimi.com
 * date   : 2020/8/3 17:49
 * desc   :
 */
public class NodeList extends Vector {
    public NodeList() {
    }

    public Node getNode(int n) {
        return (Node) get(n);
    }

    public Node getNode(String name) {
        if (name == null) {
            return null;
        }

        int nLists = size();
        for (int n = 0; n < nLists; n++) {
            Node node = getNode(n);
            String nodeName = node.getName();
            if (name.compareTo(nodeName) == 0) {
                return node;
            }
        }
        return null;
    }

    public Node getEndsWith(String name) {
        if (name == null) {
            return null;
        }

        int nLists = size();
        for (int n = 0; n < nLists; n++) {
            Node node = getNode(n);
            String nodeName = node.getName();
            if (nodeName == null) {
                continue;
            }
            if (nodeName.endsWith(name) == true) {
                return node;
            }
        }
        return null;
    }
}


