package com.osuserverlist.bjar.handlers.osu;

import org.jetbrains.annotations.NotNull;

import com.osuserverlist.bjar.App;
import com.osuserverlist.bjar.modules.WebEngine.Host;
import com.osuserverlist.bjar.modules.WebEngine.HttpMethod;
import com.osuserverlist.bjar.modules.WebEngine.Path;

import io.javalin.http.Context;
import io.javalin.http.Handler;

@Host("osu.")
@Path("/d/{id}")
@HttpMethod("GET")
public class OsuDownloadHandler implements Handler {

    @Override
    public void handle(@NotNull Context ctx) throws Exception {
        String mapSetId = ctx.pathParam("id");

        boolean noVideo = mapSetId.endsWith("n");
        if (noVideo) {
            mapSetId = mapSetId.substring(0, mapSetId.length() - 1);
        }

        String dlEndpoint = App.server.enviromentConfig.getDlEndpoint();

        if (dlEndpoint == null || dlEndpoint.isBlank()) {
            ctx.status(503).result("Download endpoint not configured.");
            return;
        }

        String queryStr = mapSetId + "?n=" + (noVideo ? 0 : 1);

        ctx.redirect(dlEndpoint + "/" + queryStr);
    }
}