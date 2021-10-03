package org.ssssssss.magicapi.spring.boot.starter;

import com.alibaba.fastjson.JSON;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Configuration;
import org.ssssssss.magicapi.utils.IoUtils;
import org.ssssssss.script.exception.MagicScriptException;
import org.ssssssss.script.parsing.ast.statement.ClassConverter;

import java.io.IOException;
import java.io.InputStream;
import java.util.function.Function;

/**
 * JSON自动配置
 *
 * @author mxd
 */
@Configuration
@AutoConfigureBefore(MagicAPIAutoConfiguration.class)
public class MagicJsonAutoConfiguration {

	private static void register(Function<String, Object> processString, Function<byte[], Object> processBytes, Function<Object, String> stringify) {
		register(processString, processBytes, IoUtils::bytes, stringify);
	}

	private static void register(Function<String, Object> processString, Function<byte[], Object> processBytes, Function<InputStream, Object> processInputStream, Function<Object, String> stringify) {
		ClassConverter.register("json", (value, params) -> {
			if (value == null) {
				return params != null && params.length > 0 ? params[0] : null;
			} else if (value instanceof CharSequence) {
				return processString.apply(value.toString());
			} else if (value instanceof byte[]) {
				return processBytes.apply((byte[]) value);
			} else if (value instanceof InputStream) {
				return processInputStream.apply((InputStream) value);
			}
			throw new MagicScriptException(String.format("不支持的类型:%s", value.getClass()));
		});
		ClassConverter.register("stringify", (value, params) -> {
			if (value == null) {
				return params != null && params.length > 0 ? params[0] : null;
			}
			return stringify.apply(value);
		});
	}

	@ConditionalOnBean({ObjectMapper.class})
	@Configuration
	static class MagicJacksonAutoConfiguration {


		MagicJacksonAutoConfiguration(ObjectMapper objectMapper) {
			register(str -> {
				try {
					return objectMapper.readValue(str, Object.class);
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			}, bytes -> {
				try {
					return objectMapper.readValue(bytes, Object.class);
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			}, is -> {
				try {
					return objectMapper.readValue(is, Object.class);
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			}, object -> {
				try {
					return objectMapper.writeValueAsString(object);
				} catch (JsonProcessingException e) {
					throw new RuntimeException(e);
				}
			});
		}
	}

	@ConditionalOnMissingBean({ObjectMapper.class})
	@ConditionalOnClass(JSON.class)
	@Configuration
	static class MagicFastJsonAutoConfiguration {

		MagicFastJsonAutoConfiguration() {
			register(JSON::parse, JSON::parse, JSON::toJSONString);
		}
	}
}
