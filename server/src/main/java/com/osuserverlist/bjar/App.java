package com.osuserverlist.bjar;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

import com.osuserverlist.bjar.models.database.DbChannel;
import com.osuserverlist.bjar.models.essentials.BanchoChannel;
import com.osuserverlist.bjar.models.essentials.ModeStats;
import com.osuserverlist.bjar.models.essentials.Player;
import com.osuserverlist.bjar.modules.database.Database;
import com.osuserverlist.bjar.modules.database.Database.ServerTimezone;
import com.osuserverlist.bjar.modules.database.MySQL;
import com.osuserverlist.bjar.modules.geo.Country;
import com.osuserverlist.bjar.modules.logger.LoggerConfiguration;
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
█▄▄▄▄▄▄▄█▄█ █▄▄█▄█  █▄▄█▄▄▄▄▄▄▄█▄▄█ █▄▄█▄▄▄▄▄▄▄█▄▄▄██▄▄▄▄▄▄▄█▄█ █▄▄█▄▄▄█  █▄█
""";

    public static void main(String[] args) {
        System.out.println(HEADER);
        Dotenv dotenv = Dotenv.configure().systemProperties().ignoreIfMissing().load();

        Database db = new Database();
        db.connectToMySQL(dotenv.get("DB_HOST"),
                dotenv.get("DB_USER"),
                dotenv.get("DB_PASS"),
                dotenv.get("DB_NAME"),
                ServerTimezone.valueOf(dotenv.get("DB_TIMEZONE")));

        Javalin app = Javalin.create(config -> {
            config.requestLogger.http(new BanchoWebLogger());
        });

        ServerWebApp.registerRoutes(app);

        app.start(Integer.parseInt(dotenv.get("PORT")));
        
        // Load bot
        // TODO: Move code

        try (MySQL mysql = Database.getConnection()) {
            ResultSet botRs = mysql.query("SELECT * FROM `users` WHERE `id` = 1").executeQuery();

            if (!botRs.next()) {
                return;
            }

            Player botPlayer = new Player(1, true, UUID.randomUUID().toString());
            botPlayer.setUsername(botRs.getString("name"));
            botPlayer.setCountry((short) Country.getIndexByCode(botRs.getString("country")));
            botPlayer.setTimezone(2);
            botPlayer.setActionText("Bancho.jar yeah");

            for(int i = 0; i <= 8; i++) {
                ModeStats modeStats = new ModeStats();
                botPlayer.getModeStats()[i] = modeStats;
            }
            Server.getInstance().playerManager.add(botPlayer);

            ResultSet channelRs = mysql.query("SELECT * FROM `channels`").executeQuery();
            while (channelRs.next()) {
                DbChannel defaultChannel = new DbChannel(channelRs);
                
                BanchoChannel channel = new BanchoChannel(String.valueOf(defaultChannel.getId()), defaultChannel.getName(), defaultChannel.getTopic(), defaultChannel.isAutoJoin());
                Server.getInstance().channelManager.add(channel);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
