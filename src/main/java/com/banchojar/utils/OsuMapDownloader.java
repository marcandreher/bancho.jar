package com.banchojar.utils;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class OsuMapDownloader {

    private static final OkHttpClient client = new OkHttpClient();
    public static void downloadMap(long mapId) {
        String url = "https://osu.direct/api/osu/" + mapId;
        String savePath = ".data/maps/" + mapId + ".osu";
    
        try {
            // Ensure directory exists
            Files.createDirectories(Paths.get(".data/maps"));
    
            // Skip download if file already exists
            if (Files.exists(Paths.get(savePath))) {
                System.out.println("Map " + mapId + " already downloaded.");
                return;
            }
    
            Request request = new Request.Builder()
                    .url(url)
                    .build();
    
            Response response = client.newCall(request).execute();
    
            if (!response.isSuccessful()) {
                System.err.println("Failed to download: " + response.code());
                return;
            }
    
            try (InputStream in = response.body().byteStream();
                 FileOutputStream out = new FileOutputStream(savePath)) {
                byte[] buffer = new byte[8192];
                int bytesRead;
                while ((bytesRead = in.read(buffer)) != -1) {
                    out.write(buffer, 0, bytesRead);
                }
            } finally {
                response.body().close();
            }
    
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
}