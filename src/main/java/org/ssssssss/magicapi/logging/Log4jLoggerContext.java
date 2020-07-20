package org.ssssssss.magicapi.logging;

import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.LogManager;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.spi.LoggingEvent;
import org.apache.log4j.spi.RootLogger;
import org.apache.log4j.spi.ThrowableInformation;


public class Log4jLoggerContext implements MagicLoggerContext {
    @Override
    public void generateAppender() {
        RootLogger logger = (RootLogger) LogManager.getRootLogger();
        PatternLayout patternLayout = new PatternLayout("%d %p [%c] - %m%n");
        MagicLog4jAppender magicLog4jAppender = new MagicLog4jAppender();
        magicLog4jAppender.setLayout(patternLayout);
        logger.addAppender(magicLog4jAppender);
    }
     class MagicLog4jAppender  extends AppenderSkeleton{


        @Override
        protected void append(LoggingEvent event) {
            LogInfo logInfo = new LogInfo();
            logInfo.setLevel(event.getLevel().toString().toLowerCase());
            logInfo.setMessage(String.valueOf(event.getMessage()));
            ThrowableInformation throwableInformation = event.getThrowableInformation();
            if (throwableInformation != null) {
                logInfo.setThrowable(throwableInformation.getThrowable());
            }
            println(logInfo);
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
