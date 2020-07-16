package org.ssssssss.magicapi.logging;

import org.slf4j.ILoggerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoggerManager {

	private static Logger logger = LoggerFactory.getLogger(LoggerManager.class);

	public static MagicLoggerContext createMagicAppender() {
		ILoggerFactory loggerFactory = LoggerFactory.getILoggerFactory();
		String loggerFactoryClassName = loggerFactory.getClass().getName();
		MagicLoggerContext magicLoggerContext = null;
		if ("ch.qos.logback.classic.LoggerContext".equalsIgnoreCase(loggerFactoryClassName)) {	//logback
			magicLoggerContext = new LogbackLoggerContext();
		}else if("org.apache.logging.slf4j.Log4jLoggerFactory".equalsIgnoreCase(loggerFactoryClassName)){	//log4j2
			magicLoggerContext = new Log4j2LoggerContext();
		}
		if (magicLoggerContext == null) {
			logger.error("无法识别LoggerContext:{}", loggerFactoryClassName);
		}else{
			magicLoggerContext.generateAppender();
		}
		return magicLoggerContext;
	}
}
