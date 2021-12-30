package org.ssssssss.magicapi.model;

public class Option extends BaseDefinition {

	public Option() {
	}

	public Option(String name, String value) {
		super(name, value);
	}

	public Option(String name, String value, String description) {
		super(name, value);
		setDescription(description);
	}


}
