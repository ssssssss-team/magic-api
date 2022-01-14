package org.ssssssss.magicapi.core.model;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

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


	public TreeNode<T> findTreeNode(Predicate<T> predicate) {
		return findTreeNode(this.children, predicate);
	}

	private TreeNode<T> findTreeNode(List<TreeNode<T>> childs, Predicate<T> predicate) {
		for (TreeNode<T> child : childs) {
			if (predicate.test(child.getNode())) {
				return child;
			}
			TreeNode<T> node = findTreeNode(child.children, predicate);
			if (node != null) {
				return node;
			}
		}
		return null;
	}

	public void moveTo(TreeNode<T> node) {
		node.children.add(this);
	}

	public List<T> flat() {
		return flat(this);
	}

	private List<T> flat(TreeNode<T> node) {
		List<T> result = new ArrayList<>();
		result.add(node.getNode());
		for (TreeNode<T> item : node.getChildren()) {
			result.addAll(flat(item));
		}
		return result;
	}

	public void addChild(TreeNode<T> node) {
		this.children.add(node);
	}
}
