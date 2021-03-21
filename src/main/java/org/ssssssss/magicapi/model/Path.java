package org.ssssssss.magicapi.model;

public class Path extends BaseDefinition{

	public Path() {
	}

	public Path(String name, String value) {
		super(name, value);
	}

	@Override
	public boolean isRequired() {
		return true;
	}
}
