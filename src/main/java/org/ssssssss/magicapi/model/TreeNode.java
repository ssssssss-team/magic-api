package org.ssssssss.magicapi.model;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class TreeNode<T> {

	private T node;

	private List<TreeNode<T>> children = new ArrayList<>();

	public TreeNode() {
	}

	public TreeNode(T node) {
		this.node = node;
	}

	public T getNode() {
		return node;
	}

	public void setNode(T node) {
		this.node = node;
	}

	public List<TreeNode<T>> getChildren() {
		return children;
	}

	public void setChildren(List<TreeNode<T>> children) {
		this.children = children;
	}

	public List<T> findNodes(Function<T, Boolean> mapping) {
		return findNodes(this.children, mapping);
	}

	public T findNode(Function<T, Boolean> mapping) {
		return findNode(this.children, mapping);
	}

	private T findNode(List<TreeNode<T>> childs, Function<T, Boolean> mapping) {
		for (TreeNode<T> item : childs) {
			if (mapping.apply(item.node)) {
				return item.node;
			}
			T found = findNode(childs, mapping);
			if (found != null) {
				return found;
			}
		}
		return null;
	}

	public void moveTo(TreeNode<T> node) {
		node.children.add(this);
	}

	private List<T> findNodes(List<TreeNode<T>> childs, Function<T, Boolean> mapping) {
		List<T> nodes = new ArrayList<>();
		childs.forEach(item -> {
			if (mapping.apply(item.getNode())) {
				nodes.add(item.getNode());
			}
			nodes.addAll(findNodes(item.children, mapping));
		});
		return nodes;
	}

	public List<T> flat() {
		return flat(this.children);
	}

	private List<T> flat(List<TreeNode<T>> childs) {
		List<T> result = new ArrayList<>();
		for (TreeNode<T> item : childs) {
			result.add(item.getNode());
			result.addAll(flat(childs));
		}
		return result;
	}
}
