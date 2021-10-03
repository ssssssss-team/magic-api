package org.ssssssss.magicapi.model;

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

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof Group)) {
			return false;
		}
		Group group = (Group) o;
		return Objects.equals(id, group.id) && Objects.equals(name, group.name) && Objects.equals(type, group.type) && Objects.equals(parentId, group.parentId) && Objects.equals(path, group.path);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, name, type, parentId, path);
	}
}
