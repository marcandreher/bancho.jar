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
import com.osuserverlist.bjar.modules.redis.Redis;
import com.osuserverlist.bjar.modules.web.BanchoWebLogger;
import com.osuserverlist.bjar.modules.web.ServerWebApp;
import com.osuserverlist.bjar.server.Server;

import io.github.cdimascio.dotenv.Dotenv;
import io.javalin.Javalin;

/**
 * Bancho.jar - An open-source osu! server implementation in Java.
 * Entrypoint
 */
public class App {

    public static final String HEADER = """
             ▄▄▄▄▄▄▄ ▄▄▄▄▄▄ ▄▄    ▄ ▄▄▄▄▄▄▄ ▄▄   ▄▄ ▄▄▄▄▄▄▄          ▄▄▄ ▄▄▄▄▄▄ ▄▄▄▄▄▄
            █  ▄    █      █  █  █ █       █  █ █  █       █        █   █      █   ▄  █
            █ █▄█   █  ▄   █   █▄█ █       █  █▄█  █   ▄   █        █   █  ▄   █  █ █ █
            █       █ █▄█  █       █     ▄▄█       █  █ █  █     ▄  █   █ █▄█  █   █▄▄█▄
            █  ▄   ██      █  ▄    █    █  █   ▄   █  █▄█  █▄▄▄ █ █▄█   █      █    ▄▄  █
            █ █▄█   █  ▄   █ █ █   █    █▄▄█  █ █  █       █   ██       █  ▄   █   █  █ █
            █▄▄▄▄▄▄▄█▄█ █▄▄█▄█  █▄▄█▄▄▄▄▄▄▄█▄▄█ █▄▄█▄▄▄▄▄▄▄█▄▄▄██▄▄▄▄▄▄▄█▄█ █▄▄█▄▄▄█  █▄█""";

    private static final Logger logger = LoggerFactory.getLogger(App.class);

    public void main() {
        System.out.println(HEADER);

        Dotenv dotenv = Dotenv.configure().systemProperties().ignoreIfMissing().load();

        ProductionLevel level = ProductionLevel.fromCode(dotenv.get("LEVEL", "PROD"));
        LoggerConfiguration loggerConfig = new LoggerConfiguration(level);
        loggerConfig.apply();

        Database db = new Database();
        db.connectToMySQL(dotenv.get("DB_HOST"),
                dotenv.get("DB_USER"),
                dotenv.get("DB_PASS"),
                dotenv.get("DB_NAME"),
                ServerTimezone.valueOf(dotenv.get("DB_TIMEZONE")));

        Redis.connect(dotenv);

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
            logger.error("Failed to initialize data structures", e);
        }

        // Main bjar entrypoint
        Server.start(dotenv);

        Javalin app = Javalin.create(config -> {
            config.routes.exception(Exception.class, (e, ctx) -> {
                logger.error("Unhandled exception while processing {} {}",
                        ctx.method(), ctx.path(), e);

                ctx.status(500).result("Internal Server Error");
            });

            config.requestLogger.http(new BanchoWebLogger());
            ServerWebApp.registerRoutes(config);

            if (level == ProductionLevel.DEVELOPMENT) {
                config.bundledPlugins.enableRouteOverview("/routes");
            }
        });

        app.start(Integer.parseInt(dotenv.get("PORT")));
    }
}
