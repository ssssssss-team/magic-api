package org.ssssssss.magicapi.core.logging;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.IThrowableProxy;
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
			Formatter formatter = Formatter.create()
					.timestamp(event.getTimeStamp())
					.space()
					.level(event.getLevel().toString())
					.value(" --- [")
					.thread(event.getThreadName())
					.value("] ")
					.loggerName(event.getLoggerName())
					.value(": ")
					.value(event.getFormattedMessage())
					.newline();
			IThrowableProxy proxy = event.getThrowableProxy();
			if (proxy instanceof ThrowableProxy) {
				formatter.throwable(((ThrowableProxy) proxy).getThrowable());
			}
			MagicLoggerContext.println(formatter.toString());
		}
	}
}
