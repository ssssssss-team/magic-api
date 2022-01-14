package org.ssssssss.magicapi.core.model;

public class PathMagicEntity extends MagicEntity {

	/**
	 * 路径
	 */
	protected String path;

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	protected void copyTo(PathMagicEntity entity) {
		super.copyTo(entity);
		entity.setPath(this.path);
	}

	protected void simple(PathMagicEntity entity) {
		super.simple(entity);
		entity.setPath(this.path);
	}
}
