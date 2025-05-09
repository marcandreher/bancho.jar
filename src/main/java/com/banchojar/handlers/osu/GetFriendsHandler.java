package com.banchojar.handlers.osu;

import org.jetbrains.annotations.NotNull;

import io.javalin.http.Context;
import io.javalin.http.Handler;

public class GetFriendsHandler implements Handler {

    @Override
    public void handle(@NotNull Context ctx) throws Exception {
        //Player player = ctx.sessionAttribute("player");
        String response = String.join("\n",
                "123", 
                "456",
                "789");
        ctx.result(response);
    }
    
}
