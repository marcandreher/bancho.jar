package com.osuserverlist.bjar.server;

import java.sql.SQLException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;

import com.osuserverlist.bjar.App;
import com.osuserverlist.bjar.models.config.ServerConfiguration;
import com.osuserverlist.bjar.models.essentials.Player;
import com.osuserverlist.bjar.modules.database.Database;
import com.osuserverlist.bjar.modules.database.MySQL;
import com.osuserverlist.bjar.modules.logger.LoggerFactory;
import com.osuserverlist.bjar.modules.osu.OsuAPIHandler;
import com.osuserverlist.bjar.server.scheudler.AutoDisconnectTask;
import com.osuserverlist.bjar.server.scheudler.SendChannelInfoTask;

import io.github.cdimascio.dotenv.Dotenv;

public class Server {
    private static final Logger logger = LoggerFactory.getLogger(App.class);
    private static Server instance;

    public static Server getInstance() {
        return instance;
    }

    public Player botPlayer;
    public OsuAPIHandler osuAPIHandler;
    public ServerConfiguration config = ServerConfiguration.load();
    public PlayerManager playerManager = new PlayerManager();
    public ChannelManager channelManager = new ChannelManager();
    public ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(3);

    public static Server start(Dotenv config) {
        instance = new Server();

        instance.osuAPIHandler = new OsuAPIHandler(config.get("OSU_API_KEY"));

        instance.scheduler.scheduleAtFixedRate(new AutoDisconnectTask(), 0, 60, TimeUnit.SECONDS);
        instance.scheduler.scheduleAtFixedRate(new SendChannelInfoTask(), 0, 8, TimeUnit.SECONDS);

        try (MySQL mysql = Database.getConnection()) {

            Player botPlayer = instance.playerManager.getBotPlayer(mysql, 1);
            
            instance.playerManager.add(botPlayer);

            instance.botPlayer = botPlayer;

            instance.channelManager.populate(mysql);

        } catch (SQLException e) {
            logger.error("Failed to load channels and bot from SQL", e);
        }

        return instance;
    }
}
