package com.osuserverlist.bjar.modules.database;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;

import com.osuserverlist.bjar.modules.logger.LoggerFactory;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

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
    public void connectToMySQL(String host, String user, String password, String database,
            ServerTimezone serverTimezone) {
        config = DatabaseConfiguration.load();
        config.apply(hikariConfig);
        String url = "jdbc:mysql://" + host + ":3306/" + database + "?serverTimezone=" + serverTimezone
                + "&allowPublicKeyRetrieval=true";
        hikariConfig
                .setJdbcUrl(url);
        hikariConfig.setUsername(user);
        hikariConfig.setPassword(password);

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
}