package com.osuserverlist.bjar.handlers.osu;

import org.jetbrains.annotations.NotNull;

import com.osuserverlist.bjar.modules.web.engine.Host;
import com.osuserverlist.bjar.modules.web.engine.HttpMethod;
import com.osuserverlist.bjar.modules.web.engine.Path;
import com.osuserverlist.bjar.server.Server;

import io.javalin.http.Context;
import io.javalin.http.Handler;

@Host("osu.")
@Path("/d/{id}")
@HttpMethod("GET")
public class OsuDownloadHandler implements Handler {
    @Override
    public void handle(@NotNull Context ctx) throws Exception {
        Integer id = ctx.pathParamAsClass("id", Integer.class).required().get();

        Server server = Server.getInstance();
        String dlEndpoint = server.osuDirectAPI.getDlEndpoint();

        if (dlEndpoint == null) {
            ctx.status(503).result("Download endpoint not configured.");
            return;
        }

        ctx.redirect(String.format("%s/%d", dlEndpoint, id));
    }
}
