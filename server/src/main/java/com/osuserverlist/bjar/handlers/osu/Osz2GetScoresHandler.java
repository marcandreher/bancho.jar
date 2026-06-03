package com.osuserverlist.bjar.handlers.osu;

import org.jetbrains.annotations.NotNull;

import com.osuserverlist.bjar.models.database.DbMap;
import com.osuserverlist.bjar.models.essentials.Player;
import com.osuserverlist.bjar.modules.database.Database;
import com.osuserverlist.bjar.modules.database.MySQL;
import com.osuserverlist.bjar.modules.web.engine.Host;
import com.osuserverlist.bjar.modules.web.engine.HttpMethod;
import com.osuserverlist.bjar.modules.web.engine.Path;
import com.osuserverlist.bjar.server.Server;

import io.javalin.http.Context;
import io.javalin.http.Handler;

@Host("osu.")
@Path("/web/osu-osz2-getscores.php")
@HttpMethod("GET")
public class Osz2GetScoresHandler implements Handler {
    
    @Override
    public void handle(@NotNull Context ctx) throws Exception {
        String modeStr = ctx.queryParam("m");

        if (modeStr == null) {
            ctx.status(400).result("Missing required query parameters.");
            return;
        }

        int mode = Integer.parseInt(modeStr);

        String username = ctx.queryParam("us");
        String passwordHash = ctx.queryParam("ha");

        System.out.println("username|passwordHash: " + username + "|" + passwordHash);

        Player player = Server.getInstance().playerManager.getByApiIdent(String.format("%s|%s", username, passwordHash));
        if(player == null) {
            ctx.status(401).result("Invalid credentials.");
            return;
        }

        DbMap beatmap;
        try (MySQL mysql = Database.getConnection()) {
            beatmap = Server.getInstance().osuAPIHandler.getBeatmapByHash(mysql, ctx.queryParam("c"));
        }

        StringBuilder sb = new StringBuilder();


        

    }

}
