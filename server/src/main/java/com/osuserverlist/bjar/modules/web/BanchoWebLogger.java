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
        // Real Pretty Logging
        String method = ctx.method().toString();
        String path = ctx.url();
        String status = ctx.status().toString();
        String logMessage = String.format("%s %s - %s (%.2f ms)", method, path, status, executionTimeMs);
        logger.info(logMessage);
    }
    
}
