package com.osuserverlist.modules.logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;

public class LoggerFactory {
    private final static Map<String, Logger> loggers = new HashMap<>();
    private static Runnable onNewLogger;

    public static void setOnNewLogger(Runnable callback) {
        onNewLogger = callback;
    }

    public static Logger getLogger(String name) {
        if (loggers.containsKey(name)) {
            return loggers.get(name);
        }

        Logger logger = org.slf4j.LoggerFactory.getLogger(name);
        loggers.put(name, logger);
        if (onNewLogger != null) {
            onNewLogger.run();
        }
        return logger;
    }

    public static Logger getLogger(Class<?> clazz) {
        return getLogger(clazz.getName());
    }

    public static List<Logger> getAllLoggers() {
        return new ArrayList<>(loggers.values());
    }
}
