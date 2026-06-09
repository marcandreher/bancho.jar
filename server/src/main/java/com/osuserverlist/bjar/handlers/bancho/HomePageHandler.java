package com.osuserverlist.bjar.handlers.bancho;

import java.util.ArrayDeque;

import org.jetbrains.annotations.NotNull;

import com.osuserverlist.bjar.modules.web.engine.Host;
import com.osuserverlist.bjar.modules.web.engine.HttpMethod;
import com.osuserverlist.bjar.modules.web.engine.Path;
import com.osuserverlist.bjar.packets.client.ClientPacketRegistry;
import com.osuserverlist.bjar.server.Server;

import io.javalin.http.Context;
import io.javalin.http.Handler;

@Host({ "c.", "c4." })
@Path("/")
@HttpMethod("GET")
public class HomePageHandler implements Handler {
    
    @Override
    public void handle(@NotNull Context ctx) throws Exception {
        String indexResource = new String(getClass().getResource("/web/index.html").openStream().readAllBytes());
        
        indexResource = indexResource.replace("%players%", String.valueOf(Server.getInstance().playerManager.getAll().size()));

        ArrayDeque<String> packets = new ArrayDeque<>();

        ClientPacketRegistry.packetHandlers.forEach((id, handler) -> {
            packets.add(String.format("%s(%d) - %s\n", id.name(), id.getValue(), handler.getClass().getSimpleName()));
        });
        indexResource = indexResource.replace("%packets%", String.join("", packets));

        ctx.contentType("text/html").result(indexResource);
    }

}
