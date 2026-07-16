package com.osuserverlist.bjar;

import java.sql.SQLException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.osuserverlist.bjar.models.ConfigModels.ServerConfiguration;
import com.osuserverlist.bjar.models.engine.ProductionLevel;
import com.osuserverlist.bjar.models.essentials.Player;
import com.osuserverlist.bjar.modules.Commands;
import com.osuserverlist.bjar.modules.WebEngine;
import com.osuserverlist.bjar.modules.WebEngine.BanchoWebLogger;
import com.osuserverlist.bjar.modules.database.Database;
import com.osuserverlist.bjar.modules.database.MySQL;
import com.osuserverlist.bjar.modules.osu.OsuAPIHandler;
import com.osuserverlist.bjar.modules.packets.ClientPacketEngine.ClientPacketRegistry;
import com.osuserverlist.bjar.modules.packets.ServerPacketEngine;
import com.osuserverlist.bjar.packets.server.UtilServerPackets.RestartPacket;
import com.osuserverlist.bjar.server.AchievementManager;
import com.osuserverlist.bjar.server.ChannelManager;
import com.osuserverlist.bjar.server.MatchManager;
import com.osuserverlist.bjar.server.PlayerManager;
import com.osuserverlist.bjar.server.scheudler.AutoDisconnectTask;
import com.osuserverlist.bjar.server.scheudler.BotPresenceTask;
import com.osuserverlist.bjar.server.scheudler.SendChannelInfoTask;

import io.github.cdimascio.dotenv.Dotenv;
import io.javalin.Javalin;
import lombok.Data;

public class Server {
    private static final Logger logger = LoggerFactory.getLogger(App.class);
    private static Server instance;

    public static Server getInstance() {
        return instance;
    }

    public String domain;
    public Player botPlayer;
    public OsuAPIHandler osuAPIHandler;
    public ServerConfiguration config = ServerConfiguration.load();
    public PlayerManager playerManager = new PlayerManager();
    public ChannelManager channelManager = new ChannelManager();
    public MatchManager matchManager = new MatchManager();
    public AchievementManager achievementManager = new AchievementManager();
    public EnviromentConfig enviromentConfig = new EnviromentConfig();
    public ScheduledExecutorService executor = Executors.newScheduledThreadPool(6);

    public static Server start(Dotenv dotenv, ProductionLevel level) {
        instance = new Server();

        ClientPacketRegistry.registerDefaultHandlers();;
        ServerPacketEngine.registerHandlers();

        instance.osuAPIHandler = new OsuAPIHandler(dotenv.get("OSU_API_KEY"));

        instance.enviromentConfig.setSearchEndpoint(dotenv.get("DIRECT_SEARCH"));
        instance.enviromentConfig.setDlEndpoint(dotenv.get("DIRECT_DL"));
        instance.enviromentConfig.setIngameRegistrationEnabled(Boolean.parseBoolean(dotenv.get("INGAME_REGISTRATION_ENABLED")));

        instance.domain = dotenv.get("DOMAIN");

        instance.executor.scheduleAtFixedRate(new AutoDisconnectTask(), 0, 60, TimeUnit.SECONDS);
        instance.executor.scheduleAtFixedRate(new SendChannelInfoTask(), 0, 8, TimeUnit.SECONDS);

        try (MySQL mysql = Database.getConnection()) {

            Player botPlayer = instance.playerManager.getBotPlayer(mysql, 1);
            
            instance.playerManager.add(botPlayer);

            instance.botPlayer = botPlayer;

            instance.executor.scheduleAtFixedRate(new BotPresenceTask(), 0, 60, TimeUnit.SECONDS);

            instance.channelManager.populate(mysql);
            instance.achievementManager.populate(mysql);

        } catch (SQLException e) {
            logger.error("Failed to initialize server", e);
        }

        Commands.registerAnnotatedHandlers("com.osuserverlist.bjar.commands");

        Commands.finalizeCommandRegistration();

        Javalin app = Javalin.create(config -> {
            config.routes.exception(Exception.class, (e, ctx) -> {
                logger.error("Unhandled exception while processing {} {}",
                        ctx.method(), ctx.path(), e);

                ctx.status(500).result("Internal Server Error");
            });

            config.concurrency.useVirtualThreads = true;
            config.requestLogger.http(new BanchoWebLogger());

            // Register annotated handlers for the web server
            WebEngine.registerDefaultHandlers(config);

            if (level == ProductionLevel.DEVELOPMENT) {
                config.bundledPlugins.enableRouteOverview("/routes");
                config.bundledPlugins.enableDevLogging();
            }
        });

        app.start(Integer.parseInt(dotenv.get("PORT")));
        return instance;
    }

    public static void stop() {
        instance.playerManager.getAll().forEach(player -> {
            player.sendPacket(new RestartPacket(10000));
        });

        if (instance != null) {
            instance.executor.shutdown(); // Gracefully shutdown the executor service
            logger.info("Server stopped");
        }
    }

    @Data
    public static class EnviromentConfig {
        private String searchEndpoint;
        private String dlEndpoint;
        private boolean ingameRegistrationEnabled;
    }
}
