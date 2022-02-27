package org.ssssssss.magicapi.core.logging;

import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.LogManager;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.spi.LoggingEvent;
import org.apache.log4j.spi.RootLogger;


/**
 * 对接Log4j
 *
 * @author mxd
 */
public class Log4jLoggerContext implements MagicLoggerContext {
	@Override
	public void generateAppender() {
		RootLogger logger = (RootLogger) LogManager.getRootLogger();
		PatternLayout patternLayout = new PatternLayout("%d %p [%c] - %m%n");
		MagicLog4jAppender magicLog4jAppender = new MagicLog4jAppender();
		magicLog4jAppender.setLayout(patternLayout);
		logger.addAppender(magicLog4jAppender);
	}

	static class MagicLog4jAppender extends AppenderSkeleton {


		@Override
		protected void append(LoggingEvent event) {
			String message = Formatter.create()
					.timestamp(event.getTimeStamp())
					.space()
					.level(event.getLevel().toString())
					.value(" --- [")
					.thread(event.getThreadName())
					.value("] ")
					.loggerName(event.getLoggerName())
					.value(": ")
					.value(event.getRenderedMessage())
					.newline()
					.throwable(event.getThrowableInformation() == null ? null : event.getThrowableInformation().getThrowable())
					.toString();
			MagicLoggerContext.println(message);
		}

		@Override
		public void close() {

		}

		@Override
		public boolean requiresLayout() {
			return false;
		}
	}
}
