package com.osuserverlist.bjar.modules.assets;

import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class AchievementDownloader implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(AchievementDownloader.class);

    @Override
    public void run() {

        File assetDir = new File("data/assets/medals/client");
        // Get file count of dir
        int fileCount = assetDir.listFiles() != null ? assetDir.listFiles().length : 0;
        if(fileCount > 0) {
            logger.debug("Achievement icons already exist, skipping download");
            return;
        }
        OkHttpClient client = new OkHttpClient();
        List<String> achievements = new ArrayList<>();
        for (String res : List.of("", "@2x")) {
            for (String mode : List.of("osu", "taiko", "fruits", "mania")) {
                for (int starRating = 1; starRating < 1 + ("osu".equals(mode) ? 10 : 8); starRating++) {
                    achievements.add(String.format("%s-skill-pass-%d%s.png", mode, starRating, res));
                    achievements.add(String.format("%s-skill-fc-%d%s.png", mode, starRating, res));
                }
            }

            for (Integer combo : List.of(500, 750, 1000, 2000)) {
                achievements.add(String.format("osu-combo-%d%s.png", combo, res));
            }

            for (String mod : List.of("suddendeath", "hidden", "perfect", "hardrock", "doubletime", "flashlight",
                    "easy", "nofail", "nightcore", "halftime", "spunout")) {
                achievements.add(String.format("all-intro-%s%s.png", mod, res));
            }
        }

        logger.info("Downloading {} achievement icons", achievements.size());
        long startTime = System.currentTimeMillis();

        for (String ach : achievements) {
            Request request = new Request.Builder()
                    .url("https://assets.ppy.sh/medals/client/" + ach)
                    .build();
            try {
                Response response = client.newCall(request).execute();
                if (!response.isSuccessful()) {
                    logger.error("Error downloading achievement icon: {}", ach);
                    continue;
                }

                byte[] imageBytes = response.body().bytes();
                File outputFile = new File("data/assets/medals/client/" + ach);
                Files.write(outputFile.toPath(), imageBytes);
            } catch (Exception e) {
                logger.error("Error downloading achievement icon: {}", ach, e);
            }
        }
        logger.info("Finished downloading achievement icons in <{}ms>", (System.currentTimeMillis() - startTime));
    }

}
