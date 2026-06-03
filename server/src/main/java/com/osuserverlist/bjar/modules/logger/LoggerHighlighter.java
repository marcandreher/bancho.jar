package com.osuserverlist.bjar.modules.logger;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.pattern.color.ANSIConstants;
import ch.qos.logback.core.pattern.color.ForegroundCompositeConverterBase;

public class LoggerHighlighter extends ForegroundCompositeConverterBase<ILoggingEvent> {

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