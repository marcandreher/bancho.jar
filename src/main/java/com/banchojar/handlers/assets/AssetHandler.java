package com.banchojar.handlers.assets;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import io.javalin.Javalin;
import io.javalin.http.Context;

public class AssetHandler {

    public static void registerRoutes(Javalin app) {
        app.get("/{userid}", AssetHandler::handleAvatarRequest);
        app.get("/thumb/{id}", AssetHandler::getPeppyRedirect);
        app.get("/preview/{id}", AssetHandler::getPeppyRedirect);
    }

    private static void handleAvatarRequest(Context ctx) throws Exception {
        String userId = ctx.pathParam("userid");

        Path jpgPath = Paths.get(".data/avatars", userId + ".jpg");

        if (Files.exists(jpgPath)) {
            ctx.contentType("image/jpeg");
            ctx.result(Files.readAllBytes(jpgPath));
            return;
        }

        Path pngPath = Paths.get(".data/avatars", userId + ".png");

        if (Files.exists(pngPath)) {
            ctx.contentType("image/png");
            ctx.result(Files.readAllBytes(pngPath));
        } else {
            Path defaultPath = Paths.get(".data/avatars", "default.png");
            ctx.contentType("image/png");
            ctx.result(Files.readAllBytes(defaultPath));
        }
    }

    public static void getPeppyRedirect(Context ctx) {
        ctx.status(301).redirect("https://b.ppy.sh" + ctx.path());
    }
}
