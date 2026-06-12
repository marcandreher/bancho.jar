package com.osuserverlist.bjar.modules.logger;

import com.osuserverlist.bjar.models.engine.ProductionLevel;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class LoggerConfiguration {
    
    private final ProductionLevel level;

    public void apply() {
        // Get logback root logger
        Logger rootLogger = (Logger) LoggerFactory.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME);
        
        if(level == ProductionLevel.DEVELOPMENT) {
            rootLogger.setLevel(Level.DEBUG);
        }
    }
}
