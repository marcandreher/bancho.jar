package com.osuserverlist.bjar.handlers.bancho;

import java.io.IOException;
import java.util.Comparator;
import java.util.stream.Collectors;

import org.jetbrains.annotations.NotNull;

import com.osuserverlist.bjar.Server;
import com.osuserverlist.bjar.modules.web.engine.Host;
import com.osuserverlist.bjar.modules.web.engine.HttpMethod;
import com.osuserverlist.bjar.modules.web.engine.Path;
import com.osuserverlist.bjar.packets.client.engine.ClientPacketRegistry;

import io.javalin.http.Context;
import io.javalin.http.Handler;

@Host({ "c.", "c4." })
@Path("/")
@HttpMethod("GET")
public class HomePageHandler implements Handler {

    private final String indexTemplate;
    private final String packetList;

    public HomePageHandler() throws IOException {
        this.indexTemplate = new String(
            getClass().getResourceAsStream("/web/index.html").readAllBytes()
        );

        this.packetList = ClientPacketRegistry.packetHandlers.entrySet()
            .stream()
            .sorted(Comparator.comparing(entry -> entry.getKey().name()))
            .map(entry ->
                entry.getKey().name() +
                "(" + entry.getKey().getValue() + ") - " +
                entry.getValue().getClass().getSimpleName()
            )
            .collect(Collectors.joining("\n"));
    }

    @Override
    public void handle(@NotNull Context ctx) {
        String html = indexTemplate
            .replace("%players%", String.valueOf(Server.getInstance().playerManager.getAll().size()))
            .replace("%packets%", packetList);

        ctx.contentType("text/html").result(html);
    }
}