package org.ssssssss.magicapi.provider.impl;

import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.ssssssss.magicapi.model.Group;
import org.ssssssss.magicapi.model.TreeNode;
import org.ssssssss.magicapi.provider.GroupServiceProvider;
import org.ssssssss.magicapi.utils.PathUtils;

import java.util.*;
import java.util.stream.Collectors;

public class DefaultGroupServiceProvider extends BeanPropertyRowMapper<Group> implements GroupServiceProvider {

	private JdbcTemplate template;

	private Map<String, Group> cacheTree = new HashMap<>();

	public DefaultGroupServiceProvider(JdbcTemplate template) {
		super(Group.class);
		this.template = template;
	}

	@Override
	public boolean insert(Group group) {
		group.setId(UUID.randomUUID().toString().replace("-", ""));
		String insertGroup = "insert into magic_group(id,group_name,group_type,group_path,parent_id) values(?,?,?,?,?)";
		return template.update(insertGroup, group.getId(), group.getName(), group.getType(), group.getPath(), group.getParentId()) > 0;
	}

	@Override
	public boolean update(Group group) {
		String updateGroup = "update magic_group set group_name = ?,group_path=?,parent_id = ? where id = ?";
		return template.update(updateGroup, group.getName(), group.getPath(), group.getParentId(), group.getId()) > 0;
	}

	@Override
	public boolean delete(String groupId) {
		String deleteByGroupId = "delete from magic_api_info where api_group_id = ?";
		return template.update(deleteByGroupId, groupId) > 0;
	}

	@Override
	public boolean contains(String groupId) {
		return "0".equals(groupId) || cacheTree.containsKey(groupId);
	}

	@Override
	public TreeNode<Group> apiGroupList() {
		List<Group> groups = template.query("select * from magic_group where group_type = '1' ", this);
		TreeNode<Group> root = new TreeNode<>();
		root.setNode(new Group("0", "root"));
		convertToTree(groups, root);
		Map<String, Group> groupMap = new HashMap<>();
		groups.forEach(group -> groupMap.put(group.getId(), group));
		cacheTree = groupMap;
		return root;
	}

	@Override
	public List<Group> groupList() {
		return template.query("select * from magic_group",this);
	}

	@Override
	public String getFullPath(String groupId) {
		StringBuilder path = new StringBuilder();
		Group group;
		while ((group = cacheTree.get(groupId)) != null) {
			path.insert(0, '/' + Objects.toString(group.getPath(), ""));
			groupId = group.getParentId();
		}
		// 需要找到根节点，否则说明中间被删除了
		if (!"0".equals(groupId)) {
			return null;
		}
		return PathUtils.replaceSlash(path.toString());
	}

	@Override
	public String getFullName(String groupId) {
		if (groupId == null || "0".equals(groupId)) {
			return "";
		}
		StringBuilder name = new StringBuilder();
		Group group;
		while ((group = cacheTree.get(groupId)) != null) {
			name.insert(0, '/' + group.getName());
			groupId = group.getParentId();
		}
		// 需要找到根节点，否则说明中间被删除了
		if (!"0".equals(groupId)) {
			return null;
		}
		return name.substring(1);
	}

	private void convertToTree(List<Group> groups, TreeNode<Group> current) {
		List<TreeNode<Group>> treeNodes = groups.stream()
				.filter(it -> current.getNode().getId().equals(it.getParentId()))
				.map(TreeNode::new)
				.collect(Collectors.toList());
		current.setChildren(treeNodes);
		treeNodes.forEach(it -> convertToTree(groups, it));
	}

	@Override
	protected String lowerCaseName(String name) {
		return super.lowerCaseName(name).replace("group_", "");
	}
}
