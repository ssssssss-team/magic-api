package org.ssssssss.magicapi.model;

public class Group {

	private String id;

	private String name;

	private String type;

	private String parentId;

	private String path;

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
}
