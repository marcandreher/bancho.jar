package com.banchojar;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.banchojar.javalin.HostRestricted;
import com.github.f4b6a3.uuid.UuidCreator;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import io.javalin.Javalin;
import io.javalin.http.Context;
import io.javalin.http.UploadedFile;

public class OsuHandler {

    private static final OkHttpClient client = new OkHttpClient();

    public static void registerRoutes(Javalin app) {
        app.get("/web/osu-search.php", OsuHandler::handleOsuDirect);
        
        app.get("/ss/{uuid}", OsuHandler::handleScreenshot);

        app.post("/web/osu-screenshot.php", ctx -> {
            // Authenticate player (example stub, you should implement your own auth logic)
            String username = ctx.formParam("u");
            String password = ctx.formParam("p");
            
            // Get version param
            String versionStr = ctx.formParam("v");
            int version = 0;
            try {
                version = Integer.parseInt(versionStr);
            } catch (NumberFormatException e) {
                ctx.status(400).result("Invalid version.");
                return;
            }

            // Handle file upload
            UploadedFile screenshot = ctx.uploadedFile("ss");
            if (screenshot == null) {
                ctx.status(400).result("Screenshot file missing.");
                return;
            }

            byte[] bytes = screenshot.content().readAllBytes();
            if (bytes.length > (4 * 1024 * 1024)) { // 4 MB limit
                ctx.status(400).result("Screenshot file too large.");
                return;
            }

            // Check file headers (simple header matching)
            boolean isPng = bytes.length > 8 &&
                    bytes[0] == (byte) 0x89 &&
                    bytes[1] == (byte) 0x50 &&
                    bytes[2] == (byte) 0x4E &&
                    bytes[3] == (byte) 0x47;

            boolean isJpeg = bytes.length > 4 &&
                    bytes[0] == (byte) 0xFF &&
                    bytes[1] == (byte) 0xD8 &&
                    bytes[bytes.length - 2] == (byte) 0xFF &&
                    bytes[bytes.length - 1] == (byte) 0xD9;

            String extension;
            if (isJpeg) {
                extension = "jpeg";
            } else if (isPng) {
                extension = "png";
            } else {
                ctx.status(400).result("Invalid file type.");
                return;
            }

            // Generate unique filename
            String filename;
            java.nio.file.Path uploadDir = java.nio.file.Paths.get(".data/screenshots");
            java.nio.file.Files.createDirectories(uploadDir);
            UUID fileUuid = UuidCreator.getRandomBased();

            do {
                filename = fileUuid.toString();
                filename += "." + extension;
            } while (java.nio.file.Files.exists(uploadDir.resolve(filename)));

            // Save file
            java.nio.file.Files.write(uploadDir.resolve(filename), bytes);

            // Log upload (placeholder)
            System.out.println(username + " uploaded " + filename);

            // Return result
            ctx.result(filename);
        });

    }
    @HostRestricted(path = "/ss/{uuid}", hosts = "osu.osulocal.sh")
    public static void handleScreenshot(Context ctx) throws IOException {
        String uuid = ctx.pathParam("uuid");
        java.nio.file.Path filePath = java.nio.file.Paths.get(".data/screenshots", uuid);
        System.out.println("File path: " + filePath.toString());
        if (java.nio.file.Files.exists(filePath)) {
            String extension = uuid.substring(uuid.lastIndexOf('.') + 1).toLowerCase();
            if ("png".equals(extension)) {
                ctx.contentType("image/png");
            } else if ("jpeg".equals(extension) || "jpg".equals(extension)) {
                ctx.contentType("image/jpeg");
            } else {
                ctx.contentType("application/octet-stream");
            }
            ctx.result(java.nio.file.Files.readAllBytes(filePath));
        } else {
            ctx.status(404).result("File not found.");
        }
    }

    private static void handleOsuDirect(Context ctx) throws IOException {
        String user = ctx.queryParam("u");
        String pass = ctx.queryParam("h");

        Request request = new Request.Builder()
                .url("https://osu.direct/api/v2/search?osudirect=ttt")
                .build();

        Response response = client.newCall(request).execute();
        ;
        if (!response.isSuccessful()) {
            ctx.status(500).result("Failed to fetch data from osu.direct");
            return;
        }
        String responseBody = response.body().string();

        ctx.result(responseBody);
    }

    public static String sanitize(String s) {
        return s.replace("|", "I");
    }
}
