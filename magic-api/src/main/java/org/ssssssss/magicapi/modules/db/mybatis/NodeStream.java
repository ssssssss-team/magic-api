package org.ssssssss.magicapi.modules.db.mybatis;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.List;

public class NodeStream {

	private final List<Node> nodes;

	private int index = 0;

	private final int len;

	public NodeStream(NodeList nodeList) {
		this.nodes = filterCommentAndBlankNodes(nodeList);
		this.len = this.nodes.size();
	}

	private static List<Node> filterCommentAndBlankNodes(NodeList nodeList) {
		List<Node> nodes = new ArrayList<>();
		for (int i = 0, len = nodeList.getLength(); i < len; i++) {
			Node node = nodeList.item(i);
			short nodeType = node.getNodeType();
			if (nodeType != Node.COMMENT_NODE && (nodeType != Node.TEXT_NODE || node.getNodeValue().trim().length() > 0)) {
				nodes.add(node);
			}
		}
		return nodes;
	}

	public boolean match(String nodeName) {
		return hasMore() && nodeName.equalsIgnoreCase(this.nodes.get(this.index).getNodeName());
	}


	public boolean match(short nodeType) {
		return hasMore() && nodeType == this.nodes.get(this.index).getNodeType();
	}

	public Node consume() {
		return this.nodes.get(this.index++);
	}

	public boolean hasMore() {
		return this.index < this.len;
	}
}
