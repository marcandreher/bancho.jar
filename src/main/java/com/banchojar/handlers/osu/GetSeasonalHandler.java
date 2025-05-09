package com.banchojar.handlers.osu;

import java.util.List;

import org.jetbrains.annotations.NotNull;

import com.google.gson.Gson;

import io.javalin.http.Context;
import io.javalin.http.Handler;

public class GetSeasonalHandler implements Handler {

    @Override
    public void handle(@NotNull Context ctx) throws Exception {
        List<String> seasonalBgs = List.of("https://i.imgur.com/GtDu0J9.jpeg", "https://i.imgur.com/2iRGu14.jpeg",
                "https://i.imgur.com/bO2KATB.jpeg");
        ctx.contentType("application/json");
        if (seasonalBgs == null || seasonalBgs.isEmpty()) {
            ctx.result("[]"); // Return empty JSON array if no seasonal_bgs
        } else {
            Gson gson = new Gson();
            String json = gson.toJson(seasonalBgs);
            ctx.result(json);
        }
    }
    
}
