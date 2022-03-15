package org.ssssssss.magicapi.core.model;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * 分组对象
 *
 * @author mxd
 */
public class Group extends Attributes<Object> {

	private String id;

	private String name;

	private String type;

	private String parentId;

	private String path;

	private Long createTime;

	private Long updateTime;

	private String createBy;

	private String updateBy;

	/**
	 * 路径变量
	 */
	private List<Path> paths = Collections.emptyList();

	/**
	 * 分组选项
	 */
	private List<BaseDefinition> options = Collections.emptyList();

	public Group(String id, String name) {
		this.id = id;
		this.name = name;
	}

	public Group() {
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getParentId() {
		return parentId;
	}

	public void setParentId(String parentId) {
		this.parentId = parentId;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public List<Path> getPaths() {
		return paths;
	}

	public void setPaths(List<Path> paths) {
		this.paths = paths;
	}

	public List<BaseDefinition> getOptions() {
		return options;
	}

	public void setOptions(List<BaseDefinition> options) {
		this.options = options;
	}

	public Group copy(){
		Group group = new Group();
		group.setId(this.id);
		group.setName(this.name);
		group.setType(this.type);
		group.setPaths(this.paths);
		group.setPath(this.path);
		group.setProperties(this.properties);
		group.setParentId(this.parentId);
		group.setOptions(this.options);
		return group;
	}

	public Long getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Long createTime) {
		this.createTime = createTime;
	}

	public Long getUpdateTime() {
		return updateTime;
	}

	public void setUpdateTime(Long updateTime) {
		this.updateTime = updateTime;
	}

	public String getCreateBy() {
		return createBy;
	}

	public void setCreateBy(String createBy) {
		this.createBy = createBy;
	}

	public String getUpdateBy() {
		return updateBy;
	}

	public void setUpdateBy(String updateBy) {
		this.updateBy = updateBy;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		Group group = (Group) o;
		return Objects.equals(id, group.id) && Objects.equals(name, group.name) && Objects.equals(type, group.type) && Objects.equals(parentId, group.parentId) && Objects.equals(path, group.path) && Objects.equals(createTime, group.createTime) && Objects.equals(updateTime, group.updateTime) && Objects.equals(createBy, group.createBy) && Objects.equals(updateBy, group.updateBy) && Objects.equals(paths, group.paths) && Objects.equals(options, group.options);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, name, type, parentId, path, createTime, updateTime, createBy, updateBy, paths, options);
	}
}
