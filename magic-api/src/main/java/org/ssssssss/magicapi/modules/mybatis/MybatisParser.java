package org.ssssssss.magicapi.modules.mybatis;

import org.ssssssss.magicapi.exception.MagicAPIException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;

public class MybatisParser {

	public static SqlNode parse(String xml) {
		try {
			xml = "<mybatis>" + xml + "</mybatis>";
			DocumentBuilder documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			Document document = documentBuilder.parse(new ByteArrayInputStream(xml.getBytes()));
			SqlNode sqlNode = new TextSqlNode("");
			parseNodeList(sqlNode, document.getDocumentElement().getChildNodes());
			return sqlNode;
		} catch (Exception e) {
			throw new MagicAPIException("SQL解析错误", e);
		}
	}

	private static void parseNodeList(SqlNode sqlNode, NodeList nodeList) {
		for (int i = 0, len = nodeList.getLength(); i < len; i++) {
			Node node = nodeList.item(i);
			if (node.getNodeType() == Node.TEXT_NODE) {
				sqlNode.addChildNode(new TextSqlNode(node.getNodeValue().trim()));
			} else if (node.getNodeType() != Node.COMMENT_NODE) {
				String nodeName = node.getNodeName();
				SqlNode childNode;
				if ("foreach".equalsIgnoreCase(nodeName)) {
					childNode = parseForeachSqlNode(node);
				} else if ("if".equalsIgnoreCase(nodeName)) {
					childNode = new IfSqlNode(getNodeAttributeValue(node, "test"));
				} else if ("trim".equalsIgnoreCase(nodeName)) {
					childNode = parseTrimSqlNode(node);
				} else if ("set".equalsIgnoreCase(nodeName)) {
					childNode = parseSetSqlNode();
				} else if ("where".equalsIgnoreCase(nodeName)) {
					childNode = parseWhereSqlNode();
				} else {
					throw new UnsupportedOperationException("Unsupported tags :" + nodeName);
				}
				sqlNode.addChildNode(childNode);
				if (node.hasChildNodes()) {
					parseNodeList(childNode, node.getChildNodes());
				}
			}
		}
	}

	/**
	 * 解析foreach节点
	 */
	private static ForeachSqlNode parseForeachSqlNode(Node node) {
		ForeachSqlNode foreachSqlNode = new ForeachSqlNode();
		foreachSqlNode.setCollection(getNodeAttributeValue(node, "collection"));
		foreachSqlNode.setSeparator(getNodeAttributeValue(node, "separator"));
		foreachSqlNode.setClose(getNodeAttributeValue(node, "close"));
		foreachSqlNode.setOpen(getNodeAttributeValue(node, "open"));
		foreachSqlNode.setItem(getNodeAttributeValue(node, "item"));
		return foreachSqlNode;
	}

	/**
	 * 解析trim节点
	 */
	private static TrimSqlNode parseTrimSqlNode(Node node) {
		TrimSqlNode trimSqlNode = new TrimSqlNode();
		trimSqlNode.setPrefix(getNodeAttributeValue(node, "prefix"));
		trimSqlNode.setPrefixOverrides(getNodeAttributeValue(node, "prefixOverrides"));
		trimSqlNode.setSuffix(getNodeAttributeValue(node, "suffix"));
		trimSqlNode.setSuffixOverrides(getNodeAttributeValue(node, "suffixOverrides"));
		return trimSqlNode;
	}

	/**
	 * 解析set节点
	 */
	private static SetSqlNode parseSetSqlNode() {
		SetSqlNode setSqlNode = new SetSqlNode();
		return setSqlNode;
	}

	/**
	 * 解析where节点
	 */
	private static WhereSqlNode parseWhereSqlNode() {
		WhereSqlNode whereSqlNode = new WhereSqlNode();
		return whereSqlNode;
	}

	private static String getNodeAttributeValue(Node node, String attributeKey) {
		Node item = node.getAttributes().getNamedItem(attributeKey);
		return item != null ? item.getNodeValue() : null;
	}
}
