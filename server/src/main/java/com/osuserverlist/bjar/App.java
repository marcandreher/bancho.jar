package com.osuserverlist.bjar;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.slf4j.Logger;

import com.osuserverlist.bjar.models.engine.ProductionLevel;
import com.osuserverlist.bjar.modules.assets.AchievementDownloader;
import com.osuserverlist.bjar.modules.assets.DefaultAssetsDownloader;
import com.osuserverlist.bjar.modules.database.Database;
import com.osuserverlist.bjar.modules.database.Database.ServerTimezone;
import com.osuserverlist.bjar.modules.logger.LoggerConfiguration;
import com.osuserverlist.bjar.modules.logger.LoggerFactory;
import com.osuserverlist.bjar.modules.recalc.RecalcRunnable;
import com.osuserverlist.bjar.modules.redis.Redis;

import io.github.cdimascio.dotenv.Dotenv;

/**
 * Bancho.jar - An open-source osu! server implementation in Java.
 * Entrypoint
 */
public class App {

    public static final String HEADER = """
             ██          ▄           ██              ▀▀       ▄
             ████▄ ▄▀▀█▄ ████▄ ▄███▀ ████▄ ▄███▄     ██ ▄▀▀█▄ ████▄
             ██ ██ ▄█▀██ ██ ██ ██    ██ ██ ██ ██     ██ ▄█▀██ ██
            ▄████▀▄▀█▄██▄██ ▀█▄▀███▄▄██ ██▄▀███▀ ██  ██ ▀█▄██ █▀
                                                     ██
                                                   ▀▀▀      """;

    private static final Logger logger = LoggerFactory.getLogger(App.class);

    public void main(String[] args) {
        System.out.println(HEADER);

        Dotenv dotenv = Dotenv.configure().systemProperties().ignoreIfMissing().load();

        ProductionLevel level = ProductionLevel.fromCode(dotenv.get("LEVEL", "PROD"));
        LoggerConfiguration loggerConfig = new LoggerConfiguration(level);
        loggerConfig.apply();

        Database db = new Database();

        db.connectToMySQL(config -> {
            config.setHost(dotenv.get("DB_HOST"));
            config.setUser(dotenv.get("DB_USER"));
            config.setPassword(dotenv.get("DB_PASS"));
            config.setDatabase(dotenv.get("DB_NAME"));
            config.setServerTimezone(ServerTimezone.valueOf(dotenv.get("DB_TIMEZONE")));
        });

        Redis redis = new Redis(config -> {
            config.setHost(dotenv.get("REDIS_HOST"));
            config.setPort(Integer.parseInt(dotenv.get("REDIS_PORT")));
        });

        redis.connect();

        if (args.length > 0 && args[0].equalsIgnoreCase("--recalc")) {
            boolean force = false;
            if (args.length > 1 && args[1].equalsIgnoreCase("--force")) {
                force = true;
            }

            RecalcRunnable recalcRunnable = new RecalcRunnable(force);
            recalcRunnable.run();
            return;
        }

        try {

            Files.createDirectories(Path.of("data/maps"));
            Files.createDirectories(Path.of("data/replays"));
            Files.createDirectories(Path.of("data/ss"));
            Files.createDirectories(Path.of("data/assets/avatars"));
            Files.createDirectories(Path.of("data/assets/medals/client"));

            AchievementDownloader downloader = new AchievementDownloader();
            DefaultAssetsDownloader defaultAssetsDownloader = new DefaultAssetsDownloader();
            defaultAssetsDownloader.run();
            downloader.run();

        } catch (IOException e) {
            logger.error("Failed to initialize data structure", e);
        }

        // Main bjar entrypoint
        Server.start(dotenv, level);
    }
}
