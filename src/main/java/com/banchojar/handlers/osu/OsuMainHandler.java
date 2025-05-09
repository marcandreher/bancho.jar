package com.banchojar.handlers.osu;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.banchojar.handlers.BanchoJarRoles;

import io.javalin.Javalin;

public class OsuMainHandler {

    static Logger logger = LoggerFactory.getLogger("packets");

    public static void registerRoutes(Javalin app) {
        app.get("/ss/{uuid}", new GetScreenshotHandler());
        app.get("/d/{bmId}", ctx -> {
            String bmId = ctx.pathParam("bmId");
            ctx.redirect("https://catboy.best/d/" + bmId);
        });

        app.get("/web/osu-getseasonal.php", new GetSeasonalHandler(), BanchoJarRoles.AGENT);
        app.get("/web/osu-search.php", new OsuDirectHandler(), BanchoJarRoles.AGENT, BanchoJarRoles.QUERY, BanchoJarRoles.OSUPARAM_U, BanchoJarRoles.OSUPARAM_H);
        app.post("/web/osu-screenshot.php", new ScreenshotHandler(), BanchoJarRoles.AGENT, BanchoJarRoles.FORM, BanchoJarRoles.OSUPARAM_P, BanchoJarRoles.OSUPARAM_U);
        app.post("/web/osu-submit-modular-selector.php", new OsuSubmitModularHandler(), BanchoJarRoles.AGENT);
        app.get("/web/osu-getreplay.php", new GetReplayHandler(), BanchoJarRoles.AGENT);
        app.get("/web/osu-getfriends.php", new GetFriendsHandler(), BanchoJarRoles.AGENT, BanchoJarRoles.QUERY, BanchoJarRoles.OSUPARAM_U, BanchoJarRoles.OSUPARAM_H);
        app.post("/users", new IngameRegistrationHandler(), BanchoJarRoles.AGENT);
        app.get("/web/osu-osz2-getscores.php", new Osz2GetScoresHandler(), BanchoJarRoles.AGENT, BanchoJarRoles.QUERY, BanchoJarRoles.OSUPARAM_US, BanchoJarRoles.OSUPARAM_HA);
    }
}
