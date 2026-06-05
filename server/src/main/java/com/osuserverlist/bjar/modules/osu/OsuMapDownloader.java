package com.osuserverlist.bjar.modules.osu;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public final class OsuMapDownloader {

    private static final Path MAP_DIRECTORY = Path.of("data", "maps");

    private static final OkHttpClient CLIENT = new OkHttpClient.Builder()
            .connectTimeout(Duration.ofSeconds(10))
            .readTimeout(Duration.ofSeconds(30))
            .build();

    public static byte[] downloadMap(long mapId) throws IOException {
        Path mapFile = MAP_DIRECTORY.resolve(mapId + ".osu");

        if (Files.exists(mapFile)) {
            return Files.readAllBytes(mapFile);
        }

        Request request = new Request.Builder()
                .url("https://osu.direct/api/osu/" + mapId)
                .build();

        try (Response response = CLIENT.newCall(request).execute()) {
            if (!response.isSuccessful() || response.body() == null) {
                return null;
            }

            byte[] data = response.body().bytes();

            Files.write(mapFile, data);

            return data;
        }
    }
}