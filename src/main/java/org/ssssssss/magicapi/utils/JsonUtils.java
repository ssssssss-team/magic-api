package org.ssssssss.magicapi.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;

public class JsonUtils {

	private static ObjectMapper mapper = new ObjectMapper();

	static{
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
	}

	public static String toJsonString(Object target) {
		try {
			return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(target);
		} catch (JsonProcessingException e) {
			return null;
		}
	}

	public static <T> T readValue(String json, Class<T> clazz){
		try {
			return mapper.readValue(json, clazz);
		} catch (IOException e) {
			return null;
		}
	}

	public static <T> T readValue(byte[] bytes, Class<T> clazz){
		try {
			return mapper.readValue(bytes, clazz);
		} catch (IOException e) {
			return null;
		}
	}
}
