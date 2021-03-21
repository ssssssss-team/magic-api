package org.ssssssss.magicapi.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class JsonUtils {

	private static final ObjectMapper mapper = new ObjectMapper();

	private static final Logger logger = LoggerFactory.getLogger(JsonUtils.class);

	static {
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
	}

	public static String toJsonString(Object target) {
		try {
			return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(target);
		} catch (JsonProcessingException e) {
			logger.error("json序列化失败", e);
			return null;
		}
	}

	public static <T> T readValue(String json, TypeReference<T> typeReference) {
		try {
			return mapper.readValue(json, typeReference);
		} catch (IOException e) {
			logger.error("读取json失败,json:{}", json, e);
			return null;
		}
	}

	public static <T> T readValue(String json, Class<T> clazz) {
		try {
			return mapper.readValue(json, clazz);
		} catch (IOException e) {
			logger.error("读取json失败,json:{}", json, e);
			return null;
		}
	}

	public static <T> T readValue(byte[] bytes, Class<T> clazz) {
		try {
			return mapper.readValue(bytes, clazz);
		} catch (IOException e) {
			logger.error("读取json失败,json:{}", new String(bytes), e);
			return null;
		}
	}
	public static <T> T readValue(byte[] bytes, JavaType javaType) {
		try {
			return mapper.readValue(bytes, javaType);
		} catch (IOException e) {
			logger.error("读取json失败,json:{}", new String(bytes), e);
			return null;
		}
	}

}
