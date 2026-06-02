package com.osuserverlist;

import java.io.File;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;


import com.moandjiezana.toml.Toml;
import com.moandjiezana.toml.TomlWriter;
import com.osuserverlist.modules.logger.LoggerConfiguration;
import com.osuserverlist.modules.logger.LoggerFactory;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import de.marcandreher.fusionkit.core.FusionKit;
import de.marcandreher.fusionkit.core.config.WebAppConfig;
import de.marcandreher.fusionkit.core.database.Database;
import de.marcandreher.fusionkit.core.database.Database.ServerTimezone;
import de.marcandreher.fusionkit.core.javalin.ProductionLevel;
import io.github.cdimascio.dotenv.Dotenv;
import org.slf4j.Logger;

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
    }
}
