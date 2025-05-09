package com.banchojar.handlers.osu;

import java.util.List;
import java.util.Map;

import org.jetbrains.annotations.NotNull;

import com.banchojar.App;

import io.javalin.http.Context;
import io.javalin.http.Handler;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class OsuDirectHandler implements Handler {

    private final OkHttpClient client = new OkHttpClient();

    @Override
    public void handle(@NotNull Context ctx) throws Exception {
        App.logger.info(ctx.queryParamMap().toString());
       StringBuilder url = new StringBuilder("https://osu.direct/api/v2/search?");
        Request.Builder builder = new Request.Builder();
        for (Map.Entry<String, List<String>> entry : ctx.queryParamMap().entrySet()) {
            String key = entry.getKey();
            List<String> values = entry.getValue();
            if (values != null && !values.isEmpty()) {
                for (String value : values) {
                    url.append(key).append("=").append(sanitize(value)).append("&");
                }
            }
        }
        url.append("osudirect=true");
        builder.addHeader("osudirect", "true").url(url.toString()).build();


        Request request = builder.build();
        Response response = client.newCall(request).execute();
        
        if (!response.isSuccessful()) {
            ctx.status(500).result("Failed to fetch data from osu.direct");
            return;
        }
        String responseBody = response.body().string();

        ctx.result(responseBody);
    }

    public String sanitize(String s) {
        return s.replace("|", "I");
    }
    
}
