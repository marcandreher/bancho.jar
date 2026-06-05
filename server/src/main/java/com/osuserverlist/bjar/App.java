package com.osuserverlist.bjar;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;

import org.slf4j.Logger;

import com.osuserverlist.bjar.modules.database.Database;
import com.osuserverlist.bjar.modules.database.Database.ServerTimezone;
import com.osuserverlist.bjar.modules.database.MySQL;
import com.osuserverlist.bjar.modules.logger.LoggerFactory;
import com.osuserverlist.bjar.modules.osu.OsuAPIHandler;
import com.osuserverlist.bjar.modules.redis.Redis;
import com.osuserverlist.bjar.modules.web.BanchoWebLogger;
import com.osuserverlist.bjar.modules.web.ServerWebApp;
import com.osuserverlist.bjar.server.ChannelManager;
import com.osuserverlist.bjar.server.PlayerManager;
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

    public static final Logger logger = LoggerFactory.getLogger(App.class);

    public static void main(String[] args) {
        System.out.println(HEADER);
        Dotenv dotenv = Dotenv.configure().systemProperties().ignoreIfMissing().load();

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
        } catch (IOException e) {
            logger.error("Failed to load beatmap cache", e);
        }

        Server server = Server.start();

        server.osuAPIHandler = new OsuAPIHandler(dotenv.get("OSU_API_KEY"));

        try (MySQL mysql = Database.getConnection()) {

            PlayerManager.connectBot(mysql, 1);
            ChannelManager.populate(mysql);

        } catch (SQLException e) {
            logger.error("Failed to load channels and bot from SQL", e);
        }

        Javalin app = Javalin.create(config -> {
            config.requestLogger.http(new BanchoWebLogger());
            ServerWebApp.registerRoutes(config);

            config.routes.exception(Exception.class, (e, ctx) -> {
                logger.error("Unhandled exception while processing {} {}",
                        ctx.method(), ctx.path(), e);

                ctx.status(500).result("Internal Server Error");
            });
        });

        app.start(Integer.parseInt(dotenv.get("PORT")));
    }
}
