package com.osuserverlist.bjar.modules.database;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.osuserverlist.bjar.models.config.DatabaseConfiguration;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import lombok.Data;

public class Database {
    public static Logger logger = LoggerFactory.getLogger(Database.class);
    public static List<MySQL> runningConnections = new ArrayList<MySQL>();
    private HikariConfig hikariConfig;
    public static HikariDataSource dataSource;
    public static int currentConnections;
    public static DatabaseConfiguration config;

    private static Database instance;

    /**
     * Constructs a new Database object with default settings.
     */
    public Database() {
        if (instance != null) {
            logger.warn("A Database instance already exists.");
            return;
        }
        DatabaseConfiguration.load();
        this.hikariConfig = new HikariConfig();
        instance = this;
    }

    /**
     * Represents the server timezone for the MySQL connection.
     */
    public enum ServerTimezone {
        UTC("UTC"), GMT("GMT");

        private final String code;

        /**
         * Constructs a new ServerTimezone enum with the specified code.
         *
         * @param code The code representing the server timezone.
         */
        ServerTimezone(String code) {
            this.code = code;
        }

        /**
         * Returns the code representing the server timezone.
         *
         * @return The code representing the server timezone.
         */
        @Override
        public String toString() {
            return code;
        }
    }

    /**
     * Connects to a MySQL database using the specified connection parameters.
     *
     * @param host           The host of the MySQL server.
     * @param user           The username for the database connection.
     * @param password       The password for the database connection.
     * @param database       The name of the database to connect to.
     * @param serverTimezone The server timezone for the MySQL connection.
     */
    public void connectToMySQL(Consumer<DatabaseCredentials> databaseConfig) {
        DatabaseCredentials dbConfig = new DatabaseCredentials();
        databaseConfig.accept(dbConfig);
        config = DatabaseConfiguration.load();
        config.apply(hikariConfig);
        String url = "jdbc:mysql://" + dbConfig.getHost() + ":3306/" + dbConfig.getDatabase() + "?serverTimezone=" + dbConfig.getServerTimezone()
                + "&allowPublicKeyRetrieval=true";
        hikariConfig
                .setJdbcUrl(url);
        hikariConfig.setUsername(dbConfig.getUser());
        hikariConfig.setPassword(dbConfig.getPassword());

        try {
            dataSource = new HikariDataSource(hikariConfig);
        } catch (Exception e) {
            logger.error("Error while setting up the connection pool: ", e);
            System.exit(0);
        }

        try (MySQL connection = getConnection()) {
            connection.exec("SELECT 1");
            logger.info("Connected to Database (" + url + ")");
        }
    }

    public HikariConfig getConfig() {
        return hikariConfig;
    }

    public HikariDataSource getDataSource() {
        return dataSource;
    }

    public void shutdown() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
            logger.info("Database connection pool shut down.");
        }
    }

    /**
     * Get a connection to the MySQL database.
     *
     * @return A connection to the MySQL database.
     */
    public static MySQL getConnection() {
        MySQL connection = null;
        try {
            connection = new MySQL(dataSource.getConnection());
            Database.currentConnections++;
            return connection;
        } catch (SQLException e) {
            logger.error("Error while obtaining a connection from the pool.", e);
            return null;
        }
    }

    @Data
    public static class DatabaseCredentials {
        private String host;
        private String user;
        private String password;
        private String database;
        private ServerTimezone serverTimezone;
    }
}