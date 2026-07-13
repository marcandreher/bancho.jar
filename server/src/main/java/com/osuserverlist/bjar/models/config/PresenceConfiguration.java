package com.osuserverlist.bjar.models.config;

import java.io.File;
import java.util.List;

import com.moandjiezana.toml.Toml;
import com.moandjiezana.toml.TomlWriter;
import com.osuserverlist.bjar.models.osu.OsuClientModels.ActionStatus;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
public class PresenceConfiguration {

    private List<PresenceInfo> presenceInfos;
    
    @AllArgsConstructor
    @NoArgsConstructor
    @Data
    public static class PresenceInfo {
        private ActionStatus actionStatus;
        private String details;
    }

    @Data
    public static class MenuIcon {
        private String outlink = "";
        private String imageUrl = "";
    }

    public static PresenceConfiguration load() {
        File configFile = new File(".config/presence.toml");
        if (configFile.exists()) {
            PresenceConfiguration loadedConfig = new Toml().read(configFile)
                    .to(PresenceConfiguration.class);
            return loadedConfig;
        } else {
            PresenceConfiguration defaultConfig = new PresenceConfiguration();
            defaultConfig.setPresenceInfos(List.of(new PresenceInfo(ActionStatus.EDITING, "bancho.jars source code"), new PresenceInfo(ActionStatus.WATCHING, "some gameplay"), new PresenceInfo(ActionStatus.TESTING, "some beatmaps"), new PresenceInfo(ActionStatus.SUBMITTING, "some beatmaps")));
            try {
                new TomlWriter().write(defaultConfig, configFile);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return defaultConfig;
        }
    }
}