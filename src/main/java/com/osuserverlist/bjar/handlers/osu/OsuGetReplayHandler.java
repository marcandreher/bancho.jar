package com.osuserverlist.bjar.handlers.osu;

import java.io.File;
import java.nio.file.Files;

import org.jetbrains.annotations.NotNull;

import com.osuserverlist.bjar.App;
import com.osuserverlist.bjar.models.essentials.Player;
import com.osuserverlist.bjar.modules.WebEngine.Host;
import com.osuserverlist.bjar.modules.WebEngine.HttpMethod;
import com.osuserverlist.bjar.modules.WebEngine.Path;

import io.javalin.http.Context;
import io.javalin.http.Handler;

@Host("osu.")
@Path("/web/osu-getreplay.php")
@HttpMethod("GET")
public class OsuGetReplayHandler implements Handler {

    @Override
    public void handle(@NotNull Context ctx) throws Exception {
        String replayId = ctx.queryParam("c");
        String username = ctx.queryParam("u");
        String passwordHash = ctx.queryParam("h");
        String apiIdent = String.format("%s|%s", username, passwordHash);

        Player player = App.server.playerManager.getByApiIdent(apiIdent);

        if (player == null) {
            ctx.status(401).result("Invalid credentials.");
            return;
        }

        // TODO: Increase replay watched count in database

        File replayFile = new File("data/replays/" + replayId + ".osr");
        if (Files.exists(replayFile.toPath())) {
            ctx.contentType("application/octet-stream");
            ctx.result(Files.readAllBytes(replayFile.toPath()));
        } else {
            ctx.status(404).result("File not found.");
        }
    }

}
