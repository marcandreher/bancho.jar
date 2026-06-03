package com.osuserverlist.bjar.modules.database;

import java.io.File;

import com.zaxxer.hikari.HikariConfig;

import lombok.Data;

@Data
public class DatabaseConfiguration {
    private boolean logSql = false;
    private boolean cachePreparedStatements = true;
    private int preparedStatementCacheSize = 250;
    private int preparedStatementCacheSqlLimit = 2048;
    private boolean autoReconnect = true;
    private boolean useServerPrepStmts = true;
    private boolean useLocalSessionState = true;
    private boolean rewriteBatchedStatements = true;
    private boolean cacheResultSetMetadata = true;
    private boolean cacheStatements = true;

    private boolean useSSL = false;
    private boolean requireSSL = false;
    private int connectionTimeout = 30000; // 30 seconds
    private String characterEncoding = "UTF-8";
    private long idleTimeout = 600000; // 10 minutes
    private long maxLifetime = 1800000; // 30 minutes
    private int maximumPoolSize = 10;
    private int validationTimeout = 5000; // 5 seconds
    private int leakDetectionThreshold = 0; // disabled by default
    private boolean allowPoolSuspension = false;
    private boolean autoCommit = true;

    public static DatabaseConfiguration load() {
        File configFile = new File(".config/database.toml");
        if (configFile.exists()) {
            DatabaseConfiguration loadedConfig = new com.moandjiezana.toml.Toml().read(configFile)
                    .to(DatabaseConfiguration.class);
            return loadedConfig;
        }else {
            DatabaseConfiguration defaultConfig = new DatabaseConfiguration();
            try {
                new com.moandjiezana.toml.TomlWriter().write(defaultConfig, configFile);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return defaultConfig;
        }
    }

    public void apply(HikariConfig config) {
        config.setAutoCommit(autoCommit);
        config.setAllowPoolSuspension(allowPoolSuspension);
        config.setConnectionTimeout(connectionTimeout);
        config.setIdleTimeout(idleTimeout);
        config.setMaxLifetime(maxLifetime);
        config.setMaximumPoolSize(maximumPoolSize);
        config.setValidationTimeout(validationTimeout);
        config.setLeakDetectionThreshold(leakDetectionThreshold);

        config.addDataSourceProperty("cachePrepStmts", cachePreparedStatements);
        config.addDataSourceProperty("prepStmtCacheSize", preparedStatementCacheSize);
        config.addDataSourceProperty("prepStmtCacheSqlLimit", preparedStatementCacheSqlLimit);
        config.addDataSourceProperty("autoReconnect", autoReconnect);
        config.addDataSourceProperty("useServerPrepStmts", useServerPrepStmts);
        config.addDataSourceProperty("useLocalSessionState", useLocalSessionState);
        config.addDataSourceProperty("rewriteBatchedStatements", rewriteBatchedStatements);
        config.addDataSourceProperty("cacheResultSetMetadata", cacheResultSetMetadata);
        config.addDataSourceProperty("cacheStatements", cacheStatements);

        config.addDataSourceProperty("useSSL", useSSL);
        config.addDataSourceProperty("requireSSL", requireSSL);
        config.addDataSourceProperty("characterEncoding", characterEncoding);
    }
}