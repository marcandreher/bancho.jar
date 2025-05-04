package com.banchojar;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import io.javalin.Javalin;
import io.javalin.community.ssl.SslPlugin;
import io.javalin.http.Handler;
import io.javalin.jetty.JettyUtil;

import java.util.List;

import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.SecureRequestCustomizer;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.SslConnectionFactory;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Data
@NoArgsConstructor
@AllArgsConstructor
class Message {
    private String sender;
    private String text;
    private String recipient;
    private int senderId;
}

@Data
@NoArgsConstructor
@AllArgsConstructor
class Channel {
    private String name;
    private String topic;
    private int players;
}

@Data
@NoArgsConstructor
@AllArgsConstructor
class ScoreFrame {
    private int time;
    private int id;
    private int num300;
    private int num100;
    private int num50;
    private int numGeki;
    private int numKatu;
    private int numMiss;
    private int totalScore;
    private int maxCombo;
    private int currentCombo;
    private boolean perfect;
    private int currentHp;
    private int tagByte;
    private boolean scoreV2;
    private Float comboPortion;
    private Float bonusPortion;
}

@Data
@NoArgsConstructor
@AllArgsConstructor
class MultiplayerMatch {
    private int id;
    private boolean inProgress;
    private int powerplay;
    private int mods;
    private String name;
    private String passwd;
    private String mapName;
    private int mapId;
    private String mapMd5;
    private List<Integer> slotStatuses;
    private List<Integer> slotTeams;
    private List<Integer> slotIds;
    private int hostId;
    private int mode;
    private int winCondition;
    private int teamType;
    private boolean freemods;
    private List<Integer> slotMods;
    private int seed;
}

public class App {
    public static Logger logger = LoggerFactory.getLogger(App.class);

    public static void main(String[] args) {

        SslPlugin plugin = new SslPlugin(conf -> {
            conf.keystoreFromPath("keystore.jks", "changeit");
        });

        Javalin app = Javalin.create(config -> {
            config.registerPlugin(plugin);
        }).start();

        app.before("*", new Handler() {
            @Override
            public void handle(io.javalin.http.Context ctx) throws Exception {
                logger.info("[REQ] | " + ctx.path() + " | " + ctx.method() + " | " + ctx.status());
            }
        });

        BanchoHandler.registerRoutes(app);
        LoginHandler.registerRoutes(app);

        System.out.println("Bancho server is running on HTTPS port 7000...");
    }
}