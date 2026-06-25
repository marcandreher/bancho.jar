package com.osuserverlist.bjar.handlers.osu;

import java.sql.ResultSet;

import org.jetbrains.annotations.NotNull;

import com.osuserverlist.bjar.Server;
import com.osuserverlist.bjar.models.essentials.Player;
import com.osuserverlist.bjar.modules.database.Database;
import com.osuserverlist.bjar.modules.database.MySQL;
import com.osuserverlist.bjar.modules.web.engine.Host;
import com.osuserverlist.bjar.modules.web.engine.HttpMethod;
import com.osuserverlist.bjar.modules.web.engine.Path;

import io.javalin.http.Context;
import io.javalin.http.Handler;

@Host("osu.")
@Path("/web/osu-search-set.php")
@HttpMethod("GET")
public class OsuSearchSetHandler implements Handler {

    @Override
    public void handle(@NotNull Context ctx) throws Exception {
        Integer setId = ctx.queryParamAsClass("s", Integer.class).getOrNull();
        Integer bmId = ctx.queryParamAsClass("b", Integer.class).getOrNull();
        String checksum = ctx.queryParamAsClass("c", String.class).getOrNull();

        String username = ctx.queryParamAsClass("u", String.class).required().get();
        String passwordHash = ctx.queryParamAsClass("h", String.class).required().get();

        Server server = Server.getInstance();
        Player player = server.playerManager.getByApiIdent(String.format("%s|%s", username, passwordHash));

        if (player == null) {
            ctx.status(401).result("Invalid credentials.");
            return;
        }

        String column;
        Object value;

        if (setId != null) {
            column = "set_id";
            value = setId;
        } else if (bmId != null) {
            column = "id";
            value = bmId;
        } else if (checksum != null) {
            column = "md5";
            value = checksum;
        } else {
            ctx.result("");
            return;
        }

        try (MySQL mysql = Database.getConnection()) {
            String sql = "SELECT DISTINCT set_id, artist, title, status, creator, last_update, diff FROM maps WHERE " + column
                    + " = ?";
            ResultSet mapResult = mysql.query(sql, value).executeQuery();
            if (!mapResult.next()) {
                ctx.result("");
                return;
            }

            int foundSetId = mapResult.getInt("set_id");
            String artist = mapResult.getString("artist");
            String title = mapResult.getString("title");
            String creator = mapResult.getString("creator");
            int status = mapResult.getInt("status");
            String lastUpdate = mapResult.getString("last_update");
            float rating = mapResult.getFloat("diff");

            String response = String.format(
                    java.util.Locale.US,
                    "%d.osz|%s|%s|%s|%d|%.1f|%s|%d|0|0|0|0|0",
                    foundSetId,
                    fix(artist),
                    fix(title),
                    fix(creator),
                    status,
                    rating,
                    fix(lastUpdate),
                    foundSetId);

            ctx.contentType("text/plain");
            ctx.result(response);
        }
    }

    private static String fix(String s) {
        return s.replace("|", "I");
    }

}
