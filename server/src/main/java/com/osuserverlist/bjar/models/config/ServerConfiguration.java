package com.osuserverlist.bjar.models.config;

import java.io.File;
import java.util.List;

import com.moandjiezana.toml.Toml;
import com.moandjiezana.toml.TomlWriter;

import lombok.Data;

@Data
public class ServerConfiguration {

    private String serverName = "bancho.jar";
    private List<String> seasonalBackgrounds = List.of("");

    private MenuIcon menuIcon = new MenuIcon();
    private WelcomeMessage welcomeMessage = new WelcomeMessage();

    @Data
    public static class WelcomeMessage {
        private boolean botEnabled = true;
        private String botMessage = "Welcome to bancho.jar!";
        private boolean notificationEnabled = true;
        private String notificationMessage = "Welcome to bancho.jar!";
    }

    @Data
    public static class MenuIcon {
        private String outlink = "";
        private String imageUrl = "";
    }

    public static ServerConfiguration load() {
        File configFile = new File(".config/server.toml");
        if (configFile.exists()) {
            ServerConfiguration loadedConfig = new Toml().read(configFile)
                    .to(ServerConfiguration.class);
            return loadedConfig;
        } else {
            ServerConfiguration defaultConfig = new ServerConfiguration();
            try {
                new TomlWriter().write(defaultConfig, configFile);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return defaultConfig;
        }
    }
}