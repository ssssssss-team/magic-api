package org.ssssssss.magicapi.logging;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.PatternLayout;
import ch.qos.logback.classic.spi.ILoggingEvent;
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
		PatternLayout layout = new PatternLayout();
		layout.setContext(context);
		layout.setPattern(PATTERN);
		layout.start();
		MagicLogbackAppender appender = new MagicLogbackAppender(layout);
		appender.setContext(context);
		appender.setName(LOGGER_NAME);
		appender.start();
		logger.addAppender(appender);
	}

	static class MagicLogbackAppender extends UnsynchronizedAppenderBase<ILoggingEvent> {

		private PatternLayout layout;

		public MagicLogbackAppender(PatternLayout layout) {
			this.layout = layout;
		}

		@Override
		protected void append(ILoggingEvent event) {
			MagicLoggerContext.println(layout.doLayout(event));
		}
	}
}
