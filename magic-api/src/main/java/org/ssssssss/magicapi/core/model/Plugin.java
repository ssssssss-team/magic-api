package org.ssssssss.magicapi.core.model;

public class Plugin {

	/**
	 * 插件名
	 */
	private String name;

	/**
	 * js文件名
	 */
	private String javascriptFilename;

	public Plugin(String name, String javascriptFilename) {
		this.name = name;
		this.javascriptFilename = javascriptFilename;
	}

	public Plugin(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getJavascriptFilename() {
		return javascriptFilename;
	}

	public void setJavascriptFilename(String javascriptFilename) {
		this.javascriptFilename = javascriptFilename;
	}
}
