package com.banchojar;

import java.nio.file.Paths;

import javax.sql.DataSource;

import org.eclipse.jetty.server.handler.StatisticsHandler;
import org.jooq.Configuration;
import org.jooq.conf.Settings;
import org.jooq.impl.DSL;
import org.jooq.impl.DefaultConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tomlj.Toml;
import org.tomlj.TomlParseResult;

import com.banchojar.db.provider.Provider;
import com.banchojar.db.provider.Providers;
import com.banchojar.handlers.assets.AssetHandler;
import com.banchojar.handlers.bancho.BanchoHandler;
import com.banchojar.handlers.osu.OsuAuth;
import com.banchojar.handlers.osu.OsuMainHandler;
import com.banchojar.migrations.AvatarsMigration;
import com.banchojar.migrations.SQLTableMigration;
import com.banchojar.utils.Color;
import com.banchojar.utils.SQLLogger;
import com.banchojar.utils.StatisticsHandlerCollector;
import com.banchojar.utils.VersionInfo;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import io.javalin.Javalin;
import io.javalin.http.RequestLogger;
import io.prometheus.client.exporter.HTTPServer;
import lombok.Data;
import redis.clients.jedis.DefaultJedisClientConfig;
import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.JedisPooled;

public class App {
    public static Logger logger = LoggerFactory.getLogger("bancho.jar");

    @Data
    public static class Config {
        private String dbHost;
        private String dbUsername;
        private String dbPassword;
        private String dbName;
        private String dbProvider;
        private int dbPort;

        private String redisHost;
        private int redisPort;
        private String redisPassword;
        private int redisChannel;

        private String osuApiKey;
    }

    @Data
    public static class MetricsConfig {
        private boolean prometheusEnabled;
        private String prometheusHost;
        private int prometheusPort;
    }

    public static void main(String[] args) throws Exception {
        long startTime = System.currentTimeMillis();
        logger.info("⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⣠⡇⠀⠀⠀⠀⠀⠀⠀      ⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⢰⣶⣶");
        logger.info("⠀⠀⠀⠀⠀⠀⠀⠀⢀⣤⣾⠟⠀⣀⣠⠄⠀⠀⠀⠀      ⠀⠀⢀⣀⣀⣀⠀⠀⠀⠀⠀⢀⣀⣀⣀⡀⠀⢀⣀⣀⠀⠀⢀⣀⣀⠀⠀⢸⣿⣿");
        logger.info("⠀⠀⠀⠀⠀⠀⢠⣶⣿⠟⠁⢠⣾⠋⠁⠀⠀⠀⠀⠀      ⢀⣾⣿⠟⠛⢿⣿⣆⠀⠀⣾⣿⠟⠛⠛⠃⠀⢸⣿⣿⠀⠀⢸⣿⣿⠀⠀⢸⣿⣿");
        logger.info("⠀⠀⠀⠀⠀⠀⠹⣿⡇⠀⠀⠸⣿⡄⠀⠀⠀⠀⠀⠀      ⢸⣿⡏⠀⠀⠀⣿⣿⡆⠀⠻⣿⣷⣶⣤⡀⠀⢸⣿⣿⠀⠀⢸⣿⣿⠀⠀⢸⣿⣿");
        logger.info("⠀⠀⠀⠀⠀⠀⠀⠙⠷⡀⠀⠀⢹⠗⠀⠀⠀⠀⠀⠀      ⢸⣿⣧⠀⠀⢀⣿⣿⠃⠀⠀⠀⠉⢙⣿⣿⠀⠸⣿⣿⡀⠀⢸⣿⣿⠀⠀⢈⣉⣉");
        logger.info("⠀⠀⢀⣤⣴⡖⠒⠀⠀⠀⠀⠀⠀⠀⠀⡀⠀⠒⢶⣄      ⠀⠛⢿⣿⣾⣿⠟⠃⠀⠀⣿⣿⣿⣿⠿⠃⠀⠀⠛⢿⣿⣿⣿⣿⠿⠀⠀⢸⣿⣿");
        logger.info("⠀⠀⠈⠙⢛⣻⠿⠿⠿⠟⠛⠛⠛⠋⠉⠀⠀⠀⣸⡿");
        logger.info("⠀⠀⠀⠀⠛⠿⣷⣶⣶⣶⣶⣾⠿⠗⠂⠀⢀⠴⠛⠁                          bancho.jar");
        logger.info("⠀⠀⠀⠀⠀⢰⣿⣦⣤⣤⣤⣴⣶⣶⠄⠀⠀⠀⠀⠀");
        logger.info("⣀⣤⡤⠄⠀⠀⠈⠉⠉⠉⠉⠉⠀⠀⠀⠀⢀⡀⠀⠀");
        logger.info("⠻⣿⣦⣄⣀⣀⣀⣀⣀⣀⣀⣀⣀⣀⣀⣠⣴⠾⠃⠀⢀");
        logger.info("⠀⠀⠈⠉⠛⠛⠛⠛⠛⠛⠛⠛⠋⠉⠁⠀⣀⣤⡶⠋");
        logger.info("⠀⠀⠀⠀⠐⠒⠀⠠⠤⠤⠤⠶⠶⠚⠛⠛⠉⠀⠀⠀");

        try {
            TomlParseResult configResult = Toml.parse(Paths.get(".config/server.toml"));
            Server.config.dbProvider = configResult.getString("db.provider");
            Server.config.dbHost = configResult.getString("db.host");
            Server.config.dbUsername = configResult.getString("db.username");
            Server.config.dbPassword = configResult.getString("db.password");
            Server.config.dbPort = configResult.getLong("db.port").intValue();
            Server.config.dbName = configResult.getString("db.db");
            Server.config.osuApiKey = configResult.getString("osu.api_key");
            Server.config.redisHost = configResult.getString("redis.host");
            Server.config.redisPort = configResult.getLong("redis.port").intValue();
            Server.config.redisPassword = configResult.getString("redis.password");
            Server.config.redisChannel = configResult.getLong("redis.db").intValue();

            TomlParseResult metricsConfigResult = Toml.parse(Paths.get(".config/metrics.toml"));
            Server.metricsConfig.prometheusEnabled = metricsConfigResult.getBoolean("prometheus.enabled");
            Server.metricsConfig.prometheusHost = metricsConfigResult.getString("prometheus.host");
            Server.metricsConfig.prometheusPort = metricsConfigResult.getLong("prometheus.port").intValue();
        } catch (Exception e) {
            logger.error("Failed to load config file: " + e.getLocalizedMessage());
            return;
        }

        Provider provider = Providers.fromString(Server.config.dbProvider);

        if (provider == null) {
            logger.error("Invalid database provider: " + Server.config.dbProvider);
            return;
        }
        logger.info("Using database provider: <{}>", provider.getClass().getSimpleName());
        logger.info("Starting bancho.jar v{} <{}>", VersionInfo.getVersion(), VersionInfo.getBuildTimestamp());

        Server.provider = provider;

        RequestLogger logHandler = (ctx, ms) -> {
            String timeStr;
            if (ms < 1000) {
                timeStr = String.format("%.1fms", ms);
            } else if (ms < 60_000) {
                timeStr = String.format("%.1fs", ms / 1000.0);
            } else {
                timeStr = String.format("%.1fm", ms / 60000.0);
            }

            logger.info("[" + ctx.method().toString().toUpperCase() + "] | <" + ctx.host() + ctx.path() + ">" +
                    " | <" + ctx.status() + "> " + Color.GREEN + "@" + timeStr + Color.RESET);
        };

        StatisticsHandler banchoStatisticsHandler = new StatisticsHandler();

        Javalin bancho = Javalin.create(config -> {
            config.requestLogger.http(logHandler);
            config.bundledPlugins.enableRouteOverview("/routes");
            config.jetty.modifyServletContextHandler(context -> {
                banchoStatisticsHandler.setHandler(context.getHandler());
                context.setHandler(banchoStatisticsHandler);
            });
        }).start(10000);

        StatisticsHandler osuStatisticsHandler = new StatisticsHandler();

        Javalin osu = Javalin.create(config -> {
            config.router.mount(router -> {
                router.beforeMatched(new OsuAuth());
            });
            config.requestLogger.http(logHandler);
            config.bundledPlugins.enableRouteOverview("/routes");
            config.jetty.modifyServletContextHandler(context -> {
                osuStatisticsHandler.setHandler(context.getHandler());
                context.setHandler(osuStatisticsHandler);
            });
        }).start(10001);

        StatisticsHandler assetsStatisticsHandler = new StatisticsHandler();

        Javalin assets = Javalin.create(config -> {
            config.requestLogger.http(logHandler);
            config.bundledPlugins.enableRouteOverview("/routes");
            config.jetty.modifyServletContextHandler(context -> {
                // wrap the context handler with statisticsHandler before Jetty starts
                assetsStatisticsHandler.setHandler(context.getHandler());
                context.setHandler(assetsStatisticsHandler);
            });
        }).start(10002);

        if (Server.metricsConfig.prometheusEnabled) {
            StatisticsHandlerCollector.initialize("osu_", osuStatisticsHandler);
            StatisticsHandlerCollector.initialize("bancho_", banchoStatisticsHandler);
            StatisticsHandlerCollector.initialize("assets_", assetsStatisticsHandler);
            Server.prometheusServer = new HTTPServer(Server.metricsConfig.prometheusPort);
            logger.info("Prometheus metrics available at http://localhost:{}/metrics",
                    Server.prometheusServer.getPort());

        }

        var clientConfig = DefaultJedisClientConfig.builder()

                .database(Server.config.redisChannel);

        if (Server.config.redisPassword != null && !Server.config.redisPassword.isEmpty()) {
            clientConfig.password(Server.config.redisPassword);
        }

        HostAndPort redisHostAndPort = new HostAndPort(Server.config.redisHost, Server.config.redisPort);
        Server.redis = new JedisPooled(redisHostAndPort, clientConfig.build());

        String pingResponse = Server.redis.ping();
        if (!"PONG".equals(pingResponse)) {
            logger.error("Failed to connect to Redis: " + pingResponse);
            return;
        }
        logger.info("Connected to Redis at {}:{}", Server.config.redisHost, Server.config.redisPort);

        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(provider.getConnectionString(Server.config));
        config.setUsername(Server.config.getDbName());
        config.setPassword(Server.config.getDbPassword());
        config.setDriverClassName(provider.getDriverClassName());
        config.setMaximumPoolSize(10);

        DataSource dataSource = new HikariDataSource(config);
        Configuration configuration = new DefaultConfiguration()
                .set(dataSource)
                .set(provider.getDialect())
                .set(new SQLLogger())
                .set(new Settings().withExecuteLogging(false));
        Server.dsl = DSL.using(configuration);


        SQLTableMigration migration = new SQLTableMigration();
        migration.migrate(Server.dsl);

        AvatarsMigration avatarsMigration = new AvatarsMigration();
        avatarsMigration.migrate(Server.dsl);

        SetupExceptions(bancho);
        SetupExceptions(osu);
        SetupExceptions(assets);

        BanchoHandler.registerRoutes(bancho);
        OsuMainHandler.registerRoutes(osu);
        AssetHandler.registerRoutes(assets);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            long shutdownTime = System.currentTimeMillis();
            bancho.stop();
            osu.stop();
            assets.stop();

            if (dataSource != null) {
                ((HikariDataSource) dataSource).close();
            }
            Server.prometheusServer.close();
            logger.info("Server stopped in {}ms", (System.currentTimeMillis() - shutdownTime));
        }));

        logger.info("Server fully started in " + (System.currentTimeMillis() - startTime) + "ms");
    }

    public static void SetupExceptions(Javalin app) {
        app.exception(Exception.class, (e, ctx) -> {
            logger.error("Exception: ", e);
            ctx.status(500).result("Internal Server Error");
        });
    }
}