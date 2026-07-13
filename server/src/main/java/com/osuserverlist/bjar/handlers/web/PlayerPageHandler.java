package com.osuserverlist.bjar.handlers.web;

import java.io.IOException;

import org.jetbrains.annotations.NotNull;

import com.osuserverlist.bjar.Server;
import com.osuserverlist.bjar.modules.web.engine.Host;
import com.osuserverlist.bjar.modules.web.engine.HttpMethod;
import com.osuserverlist.bjar.modules.web.engine.Path;

import io.javalin.http.Context;
import io.javalin.http.Handler;

@Host({ "c.", "c4." })
@Path("/players")
@HttpMethod("GET")
public class PlayerPageHandler implements Handler {

    private final String indexTemplate;

    public PlayerPageHandler() throws IOException {
        this.indexTemplate = new String(
            getClass().getResourceAsStream("/web/players.html").readAllBytes()
        );
    }

    @Override
    public void handle(@NotNull Context ctx) {
        Server server = Server.getInstance();
        StringBuilder playerHtml = new StringBuilder();
        
        server.playerManager.getAll().forEach(player -> {
            playerHtml.append(player.getUsername()).append(" (").append(player.getId()).append(")").append("<br>");
        });

        String html = indexTemplate.replace("%players%", playerHtml.toString());
        ctx.html(html);
    }
}