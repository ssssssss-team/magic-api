package org.ssssssss.magicapi.core.annotation;

import org.ssssssss.magicapi.core.config.MessageType;

import java.lang.annotation.*;

/**
 * WebSocket 消息
 *
 * @author mxd
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Message {

	/**
	 * @return 消息类型
	 */
	MessageType value();
}
