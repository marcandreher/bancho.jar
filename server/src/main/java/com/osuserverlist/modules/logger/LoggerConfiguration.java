package com.osuserverlist.modules.logger;

import java.io.File;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

import org.slf4j.Logger;

import com.moandjiezana.toml.Toml;
import com.moandjiezana.toml.TomlWriter;

public class LoggerConfiguration {
    private final Logger logger = LoggerFactory.getLogger(LoggerConfiguration.class);
    private final File logsConfig = new File(".config/loggers.toml");

    public LoggerConfiguration() {
        if (!logsConfig.exists()) {
            logsConfig.getParentFile().mkdirs();
        }

        LoggerFactory.setOnNewLogger(this::syncConfig);
        syncConfig();
    }

    private void syncConfig() {
        Map<String, Boolean> loggers = new LinkedHashMap<>();

        if (logsConfig.exists()) {
            Toml toml = new Toml().read(logsConfig);
            Map<String, Object> existing = toml.getTable("loggers") != null
                    ? toml.getTable("loggers").toMap()
                    : Map.of();
            for (Map.Entry<String, Object> entry : existing.entrySet()) {
                String key = normalizeLoggerKey(entry.getKey());
                if (!key.isEmpty()) {
                    loggers.put(key, (Boolean) entry.getValue());
                }
            }
        }

        for (Logger logger : LoggerFactory.getAllLoggers()) {
            String name = logger.getName();
            loggers.putIfAbsent(name, true);
        }

        Map<String, Object> root = new LinkedHashMap<>();
        root.put("loggers", loggers);

        TomlWriter writer = new TomlWriter();
        try {
            writer.write(root, logsConfig);
        } catch (IOException e) {
            logger.error("Failed to write logger configuration", e);
        }
    }

    public void reload() {
        syncConfig();
        Toml toml = new Toml().read(logsConfig);

        Map<String, Object> loggers = toml.getTable("loggers").toMap();

        for (Map.Entry<String, Object> entry : loggers.entrySet()) {
            boolean enabled = (Boolean) entry.getValue();

            String key = normalizeLoggerKey(entry.getKey());
            if (key.isEmpty()) {
                continue;
            }

            ch.qos.logback.classic.Logger logger = (ch.qos.logback.classic.Logger) LoggerFactory
                    .getLogger(key);
            logger.setLevel(enabled ? null : ch.qos.logback.classic.Level.OFF);
        }
    }

    private String normalizeLoggerKey(String key) {
        if (key == null) {
            return "";
        }
        return key.replaceAll("^\"+|\"+$", "");
    }
}
