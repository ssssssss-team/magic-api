package org.ssssssss.magicapi.script;

import javax.script.ScriptEngineFactory;

public class ScriptInfo {

	private String engineName;

	private String languageVersion;

	public ScriptInfo(ScriptEngineFactory factory) {
		this.engineName = factory.getEngineName();
		this.languageVersion = factory.getLanguageVersion();
	}

	public String getEngineName() {
		return engineName;
	}

	public String getLanguageVersion() {
		return languageVersion;
	}
}
