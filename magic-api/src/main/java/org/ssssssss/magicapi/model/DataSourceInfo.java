package org.ssssssss.magicapi.model;

import java.util.HashMap;

public class DataSourceInfo extends HashMap<String, String> {

	public String getId() {
		return get("id");
	}
}
