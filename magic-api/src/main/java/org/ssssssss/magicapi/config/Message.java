package org.ssssssss.magicapi.config;

import java.lang.annotation.*;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Message {

	/**
	 * 消息类型
	 */
	MessageType value();
}
