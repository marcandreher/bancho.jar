package com.osuserverlist;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

import com.osuserverlist.main.Server;
import com.osuserverlist.models.essentials.ModeStats;
import com.osuserverlist.models.essentials.Player;
import com.osuserverlist.modules.geo.Country;
import com.osuserverlist.modules.logger.LoggerConfiguration;

import de.marcandreher.fusionkit.core.FusionKit;
import de.marcandreher.fusionkit.core.config.WebAppConfig;
import de.marcandreher.fusionkit.core.database.Database;
import de.marcandreher.fusionkit.core.database.Database.ServerTimezone;
import de.marcandreher.fusionkit.core.database.MySQL;
import de.marcandreher.fusionkit.core.javalin.ProductionLevel;
import io.github.cdimascio.dotenv.Dotenv;

/**
 * FusionKit based bancho server application.
 */
public class App {

    public static void main(String[] args) {
        FusionKit.setApplication(App.class);
        Dotenv dotenv = Dotenv.configure().systemProperties().ignoreIfMissing().load();

        boolean isProduction = !ProductionLevel.isInDevelopment(ProductionLevel.valueOf(dotenv.get("LEVEL")));

        if (!isProduction) {
            FusionKit.setLogLevel("DEBUG");
        } else {
            FusionKit.setLogLevel("INFO");
        }

        Database db = new Database();
        db.connectToMySQL(dotenv.get("DB_HOST"),
                dotenv.get("DB_USER"),
                dotenv.get("DB_PASS"),
                dotenv.get("DB_NAME"),
                ServerTimezone.valueOf(dotenv.get("DB_TIMEZONE")));

        WebAppConfig webAppConfig = WebAppConfig.builder()
                .name("BJAR")
                .port(Integer.parseInt(dotenv.get("PORT")))
                .domain(dotenv.get("DOMAIN"))
                .debugger(true)
                .productionLevel(ProductionLevel.valueOf(dotenv.get("LEVEL")))
                .build();

        ServerWebApp webApp = new ServerWebApp(webAppConfig);
        FusionKit.registerWebApplication(webApp);

        LoggerConfiguration loggerConfig = new LoggerConfiguration();
        loggerConfig.reload();

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
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
