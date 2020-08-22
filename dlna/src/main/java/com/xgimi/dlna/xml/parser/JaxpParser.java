package com.xgimi.dlna.xml.parser;

import com.xgimi.dlna.xml.Node;
import com.xgimi.dlna.xml.ParserException;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.xml.sax.InputSource;

import java.io.InputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

/**
 * author : joker.peng
 * e-mail : joker.peng@xgimi.com
 * date   : 2020/8/3 17:57
 * desc   :
 */
public class JaxpParser extends Parser {

    public JaxpParser() {
        super();
    }

    ////////////////////////////////////////////////
    //	parse (Node)
    ////////////////////////////////////////////////

    public Node parse(Node parentNode, org.w3c.dom.Node domNode, int rank) {
        int domNodeType = domNode.getNodeType();
        //		if (domNodeType != Node.ELEMENT_NODE)
        //			return;

        String domNodeName = domNode.getNodeName();
        String domNodeValue = domNode.getNodeValue();
        NamedNodeMap attrs = domNode.getAttributes();
        int arrrsLen = (attrs != null) ? attrs.getLength() : 0;

        //		Debug.message("[" + rank + "] ELEM : " + domNodeName + ", " + domNodeValue + ", type = " + domNodeType
        //		+ ", attrs = " + arrrsLen);

        if (domNodeType == org.w3c.dom.Node.TEXT_NODE) {
            // Change to use Node::addValue() instead of the setValue(). (2008/02/07)
            //parentNode.setValue(domNodeValue);
            parentNode.addValue(domNodeValue);
            return parentNode;
        }

        if (domNodeType != org.w3c.dom.Node.ELEMENT_NODE) {
            return parentNode;
        }

        Node node = new Node();
        node.setName(domNodeName);
        node.setValue(domNodeValue);

        if (parentNode != null) {
            parentNode.addNode(node);
        }

        NamedNodeMap attrMap = domNode.getAttributes();
        if (attrMap != null) {
            int attrLen = attrMap.getLength();
            //Debug.message("attrLen = " + attrLen);
            for (int n = 0; n < attrLen; n++) {
                org.w3c.dom.Node attr = attrMap.item(n);
                String attrName = attr.getNodeName();
                String attrValue = attr.getNodeValue();
                node.setAttribute(attrName, attrValue);
            }
        }

        org.w3c.dom.Node child = domNode.getFirstChild();
        if (child == null) {
            node.setValue("");
            return node;
        }
        do {
            parse(node, child, rank + 1);
            child = child.getNextSibling();
        } while (child != null);

        return node;
    }

    public Node parse(Node parentNode, org.w3c.dom.Node domNode) {
        return parse(parentNode, domNode, 0);
    }

    /* (non-Javadoc)
     * @see org.cybergarage.xml.Parser#parse(java.io.InputStream)
     */
    public Node parse(InputStream inStream) throws ParserException {
        Node root = null;

        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            InputSource inSrc = new InputSource(inStream);
            Document doc = builder.parse(inSrc);

            org.w3c.dom.Element docElem = doc.getDocumentElement();

            if (docElem != null) {
                root = parse(root, docElem);
            }
/*
			NodeList rootList = doc.getElementsByTagName("root");
			Debug.message("rootList = " + rootList.getLength());

			if (0 < rootList.getLength())
				root = parse(root, rootList.item(0));
*/
        } catch (Exception e) {
            throw new ParserException(e);
        }

        return root;
    }

}

