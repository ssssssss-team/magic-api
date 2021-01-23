package org.ssssssss.magicapi.model;

import java.util.List;

public class SynchronizeRequest {

	private String secret;

	private String remote;


	/**
	 * 0 全部
	 * 1 一个组
	 * 2 一个接口或函数
	 */
	private String mode;

	/**
	 * 分组ID
	 */
	private String groupId;

	/**
	 * 接口ID
	 */
	private String apiId;

	/**
	 * 函数ID
	 */
	private String functionId;

	private List<Info> infos;

	public String getMode() {
		return mode;
	}

	public void setMode(String mode) {
		this.mode = mode;
	}

	public String getGroupId() {
		return groupId;
	}

	public void setGroupId(String groupId) {
		this.groupId = groupId;
	}

	public String getApiId() {
		return apiId;
	}

	public void setApiId(String apiId) {
		this.apiId = apiId;
	}

	public String getFunctionId() {
		return functionId;
	}

	public void setFunctionId(String functionId) {
		this.functionId = functionId;
	}

	public String getSecret() {
		return secret;
	}

	public void setSecret(String secret) {
		this.secret = secret;
	}

	public String getRemote() {
		return remote;
	}

	public void setRemote(String remote) {
		this.remote = remote;
	}

	public List<Info> getInfos() {
		return infos;
	}

	public void setInfos(List<Info> infos) {
		this.infos = infos;
	}

	public static class Info {

		private String id;

		private String name;

		private String method;

		private String groupId;

		private String groupPath;

		private String path;

		private Long updateTime;

		public static Info from(ApiInfo apiInfo) {
			Info info = new Info();
			info.setId(apiInfo.getId());
			info.setName(apiInfo.getName());
			info.setMethod(apiInfo.getMethod());
			info.setPath(apiInfo.getPath());
			info.setUpdateTime(apiInfo.getUpdateTime());
			info.setGroupId(apiInfo.getGroupId());
			return info;
		}

		public String getGroupPath() {
			return groupPath;
		}

		public void setGroupPath(String groupPath) {
			this.groupPath = groupPath;
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

		public String getMethod() {
			return method;
		}

		public void setMethod(String method) {
			this.method = method;
		}

		public String getPath() {
			return path;
		}

		public void setPath(String path) {
			this.path = path;
		}

		public Long getUpdateTime() {
			return updateTime;
		}

		public void setUpdateTime(Long updateTime) {
			this.updateTime = updateTime;
		}

		public String getGroupId() {
			return groupId;
		}

		public void setGroupId(String groupId) {
			this.groupId = groupId;
		}
	}

}
