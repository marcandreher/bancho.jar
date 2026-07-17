package com.osuserverlist.bjar.handlers.web;

import java.io.IOException;

import org.jetbrains.annotations.NotNull;

import com.osuserverlist.bjar.App;
import com.osuserverlist.bjar.modules.main.Application;
import com.osuserverlist.bjar.modules.main.WebEngine.Host;
import com.osuserverlist.bjar.modules.main.WebEngine.HttpMethod;
import com.osuserverlist.bjar.modules.main.WebEngine.Path;

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
        StringBuilder playerHtml = new StringBuilder();
        
        App.server.playerManager.getAll().forEach(player -> {
            playerHtml.append(player.getUsername()).append(" (").append(player.getId()).append(")").append("<br>");
        });

        String html = indexTemplate.replace("%players%", playerHtml.toString()).replace("%header%", Application.HEADER);
        ctx.html(html);
    }
}