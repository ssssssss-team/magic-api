package org.ssssssss.utils;

import org.w3c.dom.Node;

import javax.xml.namespace.QName;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;

public class DomUtils {

    private static final XPath XPATH = XPathFactory.newInstance().newXPath();

    /**
     * 获取节点属性
     *
     * @param node         节点
     * @param attributeKey 属性名
     * @return 节点属性值，未设置时返回null
     */
    public static String getNodeAttributeValue(Node node, String attributeKey) {
        Node item = node.getAttributes().getNamedItem(attributeKey);
        return item != null ? item.getNodeValue() : null;
    }

    /**
     * xpath提取
     */
    public static Object evaluate(String xpath, Object item, QName qName) {
        try {
            return XPATH.compile(xpath).evaluate(item, qName);
        } catch (Exception e) {
            return null;
        }
    }
}
