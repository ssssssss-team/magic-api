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


	public TreeNode<T> findTreeNode(Function<T, Boolean> mapping){
		return findTreeNode(this.children, mapping);
	}

	private TreeNode<T> findTreeNode(List<TreeNode<T>> childs, Function<T, Boolean> mapping) {
		for (TreeNode<T> child : childs) {
			if (mapping.apply(child.getNode())) {
				return child;
			}
			TreeNode<T> node = findTreeNode(child.children, mapping);
			if(node != null){
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
}
