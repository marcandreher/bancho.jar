package com.banchojar.handlers.osu;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.jetbrains.annotations.NotNull;

import com.banchojar.App;

import io.javalin.http.Context;
import io.javalin.http.Handler;

public class GetReplayHandler implements Handler {
    @Override
    public void handle(@NotNull Context ctx) throws Exception {
        App.logger.info(ctx.formParamMap().toString());
        ctx.header("Cache-Control", "no-store");
        String uuid = ctx.queryParam("c");
        Path filePath = Paths.get(".data/replays", uuid + ".osr");
        System.out.println("File path: " + filePath.toString());
        if (Files.exists(filePath)) {
            ctx.contentType("application/octet-stream");
            ctx.result(Files.readAllBytes(filePath));
        } else {
            ctx.status(404).result("File not found.");
        }
    }
}
