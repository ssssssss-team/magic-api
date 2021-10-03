package org.ssssssss.magicapi.logging;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.ThrowableProxy;
import ch.qos.logback.core.UnsynchronizedAppenderBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 对接Logback
 *
 * @author mxd
 */
public class LogbackLoggerContext implements MagicLoggerContext {

	@Override
	public void generateAppender() {
		LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
		ch.qos.logback.classic.Logger logger = context.getLogger(Logger.ROOT_LOGGER_NAME);
		MagicLogbackAppender appender = new MagicLogbackAppender();
		appender.setContext(context);
		appender.setName(LOGGER_NAME);
		appender.start();
		logger.addAppender(appender);
	}

	static class MagicLogbackAppender extends UnsynchronizedAppenderBase<ILoggingEvent> {

		@Override
		protected void append(ILoggingEvent event) {
			LogInfo logInfo = new LogInfo();
			logInfo.setLevel(event.getLevel().levelStr.toLowerCase());
			logInfo.setMessage(event.getFormattedMessage());
			ThrowableProxy throwableProxy = (ThrowableProxy) event.getThrowableProxy();
			if (throwableProxy != null) {
				logInfo.setThrowable(throwableProxy.getThrowable());
			}
			MagicLoggerContext.println(logInfo);
		}
	}
}
