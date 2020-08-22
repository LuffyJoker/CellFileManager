package com.xgimi.dlna.xml.parser;

import com.xgimi.dlna.xml.Node;
import com.xgimi.dlna.xml.ParserException;

import org.xmlpull.v1.XmlPullParserFactory;

import java.io.InputStream;

/**
 * author : joker.peng
 * e-mail : joker.peng@xgimi.com
 * date   : 2020/8/3 17:55
 * desc   :
 */
public class XmlPullParser extends Parser {
    ////////////////////////////////////////////////
    //	Constructor
    ////////////////////////////////////////////////

    public XmlPullParser() {
    }

    ////////////////////////////////////////////////
    //	parse
    ////////////////////////////////////////////////

    public Node parse(org.xmlpull.v1.XmlPullParser xpp, InputStream inStream) throws ParserException {
        Node rootNode = null;
        Node currNode = null;

        try {
            //InputStreamReader inReader = new InputStreamReader(inStream);
            //xpp.setInput(inReader);
            xpp.setInput(inStream, null);
            int eventType = xpp.getEventType();
            while (eventType != org.xmlpull.v1.XmlPullParser.END_DOCUMENT) {
                switch (eventType) {
                    case org.xmlpull.v1.XmlPullParser.START_TAG: {
                        Node node = new Node();
                        String namePrefix = xpp.getPrefix();
                        String name = xpp.getName();
                        StringBuffer nodeName = new StringBuffer();
                        if (namePrefix != null && 0 < namePrefix.length()) {
                            nodeName.append(namePrefix);
                            nodeName.append(":");
                        }
                        if (name != null && 0 < name.length()) {
                            nodeName.append(name);
                        }
                        node.setName(nodeName.toString());
                        int attrsLen = xpp.getAttributeCount();
                        for (int n = 0; n < attrsLen; n++) {
                            String attrName = xpp.getAttributeName(n);
                            String attrValue = xpp.getAttributeValue(n);
                            node.setAttribute(attrName, attrValue);
                        }

                        if (currNode != null) {
                            currNode.addNode(node);
                        }
                        currNode = node;
                        if (rootNode == null) {
                            rootNode = node;
                        }
                    }
                    break;
                    case org.xmlpull.v1.XmlPullParser.TEXT: {
                        String value = xpp.getText();
                        if (value != null && currNode != null) {
                            currNode.setValue(value);
                        }
                    }
                    break;
                    case org.xmlpull.v1.XmlPullParser.END_TAG: {
                        currNode = currNode.getParentNode();
                    }
                    break;
                }
                eventType = xpp.next();
            }
        } catch (Exception e) {
            throw new ParserException(e);
        }

        return rootNode;
    }

    public Node parse(InputStream inStream) throws ParserException {
        Node rootNode = null;

        try {
            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            factory.setNamespaceAware(true);
            org.xmlpull.v1.XmlPullParser xpp = factory.newPullParser();
            rootNode = parse(xpp, inStream);
        } catch (Exception e) {
            throw new ParserException(e);
        }

        return rootNode;
    }

}


