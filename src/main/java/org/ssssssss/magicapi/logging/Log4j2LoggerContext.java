package org.ssssssss.magicapi.logging;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.apache.logging.log4j.core.config.Property;
import org.apache.logging.log4j.core.impl.ThrowableProxy;
import org.apache.logging.log4j.core.layout.PatternLayout;

import java.io.Serializable;
import java.nio.charset.StandardCharsets;

/**
 * 对接Log4j2
 */
public class Log4j2LoggerContext implements MagicLoggerContext{

	@Override
	public void generateAppender() {
		LoggerContext context = (LoggerContext) LogManager.getContext(false);
		Configuration configuration = context.getConfiguration();
		LoggerConfig logger = configuration.getRootLogger();
		PatternLayout layout = PatternLayout.newBuilder()
				.withCharset(StandardCharsets.UTF_8)
				.withConfiguration(configuration)
				.withPattern("%d %t %p %X{TracingMsg} %c - %m%n")
				.build();
		MagicLog4j2Appender appender = new MagicLog4j2Appender("Magic", logger.getFilter(), layout);
		appender.start();
		configuration.addAppender(appender);
		logger.addAppender(appender,logger.getLevel(),logger.getFilter());
		context.updateLoggers(configuration);
	}

	class MagicLog4j2Appender extends AbstractAppender{

		MagicLog4j2Appender(String name, Filter filter, Layout<? extends Serializable> layout) {
			super(name, filter, layout,true, Property.EMPTY_ARRAY);
		}

		@Override
		public void append(LogEvent event) {
			LogInfo logInfo = new LogInfo();
			logInfo.setLevel(event.getLevel().name().toLowerCase());
			logInfo.setMessage(event.getMessage().getFormattedMessage());
			ThrowableProxy throwableProxy = event.getThrownProxy();
			if(throwableProxy != null){
				logInfo.setThrowable(throwableProxy.getThrowable());
			}
			println(logInfo);
		}
	}
}
