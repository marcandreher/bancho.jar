package com.osuserverlist.bjar.modules.assets;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

import org.slf4j.Logger;

import com.osuserverlist.bjar.modules.logger.LoggerFactory;

import okhttp3.OkHttpClient;

public class DefaultAssetsDownloader implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(DefaultAssetsDownloader.class);

    private static final String DEFAULT_AVATAR_URL = "https://i.ibb.co/zHDyg8nj/default.png";
    private static final String DEFAULT_BOT_AVATAR_URL = "https://i.ibb.co/LXw9VFgM/1.png";
    private static final OkHttpClient client = new OkHttpClient();

    @Override
    public void run() {
        File avatarDirectory = new File("data/assets/avatars");
        if (avatarDirectory.listFiles() != null && avatarDirectory.listFiles().length > 0) {
            logger.debug("Default avatars already exist, skipping download");
            return;
        }

        Path defaultAvatarPath = Path.of(avatarDirectory.toPath().toString(), "default.png");
        Path defaultBotAvatarPath = Path.of(avatarDirectory.toPath().toString(), "1.png");

        try {
            byte[] defaultAvatarBytes = client.newCall(new okhttp3.Request.Builder().url(DEFAULT_AVATAR_URL).build()).execute().body().bytes();
            Files.write(defaultAvatarPath, defaultAvatarBytes);

            byte[] defaultBotAvatarBytes = client.newCall(new okhttp3.Request.Builder().url(DEFAULT_BOT_AVATAR_URL).build()).execute().body().bytes();
            Files.write(defaultBotAvatarPath, defaultBotAvatarBytes);
        } catch (Exception e) {
            logger.error("Error downloading default avatars", e);
        }

        logger.info("Default avatars downloaded successfully");
    }

}
