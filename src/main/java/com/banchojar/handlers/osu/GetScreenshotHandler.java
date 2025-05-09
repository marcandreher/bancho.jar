package com.banchojar.handlers.osu;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;


import io.javalin.http.Context;
import io.javalin.http.Handler;

public class GetScreenshotHandler implements Handler {
    
    @Override
    public void handle(Context ctx) throws Exception {
        
        if (ctx.attribute("deny") != null) {
            ctx.status(404);
            return;
        }

        String uuid = ctx.pathParam("uuid");
        Path filePath = Paths.get(".data/screenshots", uuid);

        if (Files.exists(filePath)) {
            String extension = uuid.substring(uuid.lastIndexOf('.') + 1).toLowerCase();
            
            if ("png".equals(extension)) {
                ctx.contentType("image/png");
            } else if ("jpeg".equals(extension) || "jpg".equals(extension)) {
                ctx.contentType("image/jpeg");
            } else {
                ctx.contentType("application/octet-stream");
            }
            
            ctx.result(Files.readAllBytes(filePath));
        } else {
            ctx.status(404).result("File not found.");
        }
    }
}
