package com.osuserverlist.bjar.modules.main;

import org.slf4j.LoggerFactory;

import com.osuserverlist.bjar.models.engine.ProductionLevel;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.pattern.color.ANSIConstants;
import ch.qos.logback.core.pattern.color.ForegroundCompositeConverterBase;
import lombok.AllArgsConstructor;

public class Logging {

    @AllArgsConstructor
    public static class LoggerConfiguration {
        private final ProductionLevel level;

        public void apply() {
            // Get logback root logger
            Logger rootLogger = (Logger) LoggerFactory.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME);

            if (level == ProductionLevel.DEVELOPMENT) {
                rootLogger.setLevel(Level.DEBUG);
            }

            // Ebean Logging
            Logger sqlLogger = (Logger) LoggerFactory.getLogger("io.ebean.SQL");
            Logger txnLogger = (Logger) LoggerFactory.getLogger("io.ebean.TXN");
            if(level == ProductionLevel.PRODUCTION) {
                sqlLogger.setLevel(Level.WARN);
                txnLogger.setLevel(Level.WARN);
            } else {
                sqlLogger.setLevel(Level.DEBUG);
                txnLogger.setLevel(Level.DEBUG);
            }
        }
    }

    public static class LoggerHighlighter extends ForegroundCompositeConverterBase<ILoggingEvent> {
        @Override
        protected String getForegroundColorCode(ILoggingEvent event) {
            return switch (event.getLevel().toInt()) {
                case Level.ERROR_INT -> "1;31"; // bold red
                case Level.WARN_INT -> "38;5;208"; // orange
                case Level.INFO_INT -> "38;5;194"; // bright green
                case Level.DEBUG_INT -> "38;5;245"; // gray
                default -> ANSIConstants.DEFAULT_FG;
            };
        }
    }

}
