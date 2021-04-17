package org.ssssssss.magicapi.logging;

import org.slf4j.ILoggerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 日志管理
 */
public class LoggerManager {

	private static Logger logger = LoggerFactory.getLogger(LoggerManager.class);

	/**
	 * 创建一个新的appender至项目中，用于UI界面
	 */
	public static MagicLoggerContext createMagicAppender() {
		ILoggerFactory loggerFactory = LoggerFactory.getILoggerFactory();
		String loggerFactoryClassName = loggerFactory.getClass().getName();
		MagicLoggerContext magicLoggerContext = null;
		if ("ch.qos.logback.classic.LoggerContext".equalsIgnoreCase(loggerFactoryClassName)) {    //logback
			magicLoggerContext = new LogbackLoggerContext();
		} else if ("org.apache.logging.slf4j.Log4jLoggerFactory".equalsIgnoreCase(loggerFactoryClassName)) {    //log4j2
			magicLoggerContext = new Log4j2LoggerContext();
		} else if ("org.slf4j.impl.Log4jLoggerFactory".equalsIgnoreCase(loggerFactoryClassName)) {    //log4j 1
			magicLoggerContext = new Log4jLoggerContext();
		}
		if (magicLoggerContext == null) {
			logger.error("无法识别LoggerContext:{}", loggerFactoryClassName);
		} else {
			magicLoggerContext.generateAppender();
		}
		return magicLoggerContext;
	}
}
