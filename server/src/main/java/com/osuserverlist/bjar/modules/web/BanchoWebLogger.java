package com.osuserverlist.bjar.modules.web;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import com.osuserverlist.bjar.modules.logger.LoggerFactory;

import io.javalin.http.Context;
import io.javalin.http.RequestLogger;

public class BanchoWebLogger implements RequestLogger {

    private static final Logger logger = LoggerFactory.getLogger(BanchoWebLogger.class);

    @Override
    public void handle(@NotNull Context ctx, @NotNull Float executionTimeMs) throws Exception {
        logger.info(String.format("%s %s - %s (%.2f ms)", ctx.method().toString(), ctx.url(), ctx.status().toString(), executionTimeMs));
    }
    
}
