package com.osuserverlist.handlers;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import com.osuserverlist.handlers.engine.Host;
import com.osuserverlist.handlers.engine.HttpMethod;
import com.osuserverlist.handlers.engine.Path;
import com.osuserverlist.modules.logger.LoggerFactory;

import io.javalin.http.Context;
import io.javalin.http.Handler;

@Host({ "c.", "c4." })
@Path("/")
@HttpMethod("POST")
public class ChoHandler implements Handler {

    private static final Logger logger = LoggerFactory.getLogger(ChoHandler.class);
    private final LoginHandler loginHandler = new LoginHandler();

    @Override
    public void handle(@NotNull Context ctx) throws Exception {
        String osuToken = ctx.header("osu-token");

        if (osuToken == null) {
            // If token is missing, handle login
            loginHandler.handleLogin(ctx);
        } else {
            // Handle other packets
        }
    }

}
