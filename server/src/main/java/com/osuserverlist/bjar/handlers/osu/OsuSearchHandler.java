package com.osuserverlist.bjar.handlers.osu;

import java.lang.reflect.Type;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import org.jetbrains.annotations.NotNull;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.osuserverlist.bjar.Server;
import com.osuserverlist.bjar.models.direct.DirectBeatmapSet;
import com.osuserverlist.bjar.models.essentials.Player;
import com.osuserverlist.bjar.models.osu.RankedStatus;
import com.osuserverlist.bjar.modules.web.engine.Host;
import com.osuserverlist.bjar.modules.web.engine.HttpMethod;
import com.osuserverlist.bjar.modules.web.engine.Path;

import io.javalin.http.Context;
import io.javalin.http.Handler;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

@Host("osu.")
@Path("/web/osu-search.php")
@HttpMethod("GET")
public class OsuSearchHandler implements Handler {

    private static final OkHttpClient CLIENT = new OkHttpClient();
    private static final Gson GSON = new Gson();

    @Override
    public void handle(@NotNull Context ctx) throws Exception {
        int mode = ctx.queryParamAsClass("m", Integer.class).getOrDefault(-1);
        int page = ctx.queryParamAsClass("p", Integer.class).getOrDefault(0);
        int rankedStatus = ctx.queryParamAsClass("r", Integer.class).getOrDefault(4);
        String query = ctx.queryParam("q");

        Server server = Server.getInstance();

        String username = ctx.queryParam("u");
        String passwordHash = ctx.queryParam("h");

        Player player = server.playerManager.getByApiIdent(String.format("%s|%s", username, passwordHash));

        if (player == null) {
            ctx.status(401).result("Invalid credentials.");
            return;
        }

        HttpUrl.Builder url = HttpUrl.parse(server.osuDirectAPI.getSearchEndpoint())
                .newBuilder()
                .addQueryParameter("amount", "100")
                .addQueryParameter("offset", String.valueOf(page * 100));

        if (query != null
                && !query.equals("Newest")
                && !query.equals("Top+Rated")
                && !query.equals("Most+Played")) {
            url.addQueryParameter("query", query);
        }

        if (mode != -1) {
            url.addQueryParameter("mode", String.valueOf(mode));
        }

        if (rankedStatus != 4) {
            url.addQueryParameter(
                    "status",
                    String.valueOf(RankedStatus.fromOsuDirect(rankedStatus).getId()));
        }

        Request request = new Request.Builder()
                .url(url.build())
                .get()
                .build();

        try (Response response = CLIENT.newCall(request).execute()) {
            if (!response.isSuccessful() || response.body() == null) {
                ctx.result("-1\nFailed to retrieve data from the beatmap mirror.");
                return;
            }

            String body = response.body().string();

            Type type = new TypeToken<List<DirectBeatmapSet>>() {
            }.getType();

            List<DirectBeatmapSet> result = GSON.fromJson(body, type);

            StringBuilder ret = new StringBuilder();

            ret.append(result.size() == 100 ? "101" : result.size());

            for (DirectBeatmapSet set : result) {
                if (set.getChildrenBeatmaps() == null) {
                    continue;
                }

                set.getChildrenBeatmaps().sort(
                        Comparator.comparingDouble(m -> m.getDifficultyRating()));

                String diffs = set.getChildrenBeatmaps().stream()
                        .map(map -> String.format(
                                Locale.US,
                                "[%.2f⭐] %s {cs: %s / od: %s / ar: %s / hp: %s}@%d",
                                map.getDifficultyRating(),
                                fix(map.getDiffName()),
                                map.getCS(),
                                map.getOD(),
                                map.getAR(),
                                map.getHP(),
                                map.getMode()))
                        .collect(Collectors.joining(","));

                int hasVideo;

                Object hasVideoObj = set.getHasVideo();

                if (hasVideoObj instanceof Boolean b) {
                    hasVideo = b ? 1 : 0;
                } else {
                    hasVideo = (int) Double.parseDouble(hasVideoObj.toString());
                }

                ret.append('\n');

                ret.append(String.format(
                        Locale.US,
                        "%d.osz|%s|%s|%s|%d|10.0|%s|%d|0|%d|0|0|0|%s",
                        set.getSetID(),
                        fix(set.getArtist()),
                        fix(set.getTitle()),
                        set.getCreator(),
                        set.getRankedStatus(),
                        set.getLastUpdate(),
                        set.getSetID(),
                        hasVideo,
                        diffs));
            }

            ctx.contentType("text/plain");
            ctx.result(ret.toString());
        }
    }

    private static String fix(String s) {
        return s == null ? "" : s.replace("|", "I");
    }
}