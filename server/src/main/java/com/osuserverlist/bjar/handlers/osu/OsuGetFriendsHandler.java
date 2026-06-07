package com.osuserverlist.bjar.handlers.osu;

import org.jetbrains.annotations.NotNull;

import com.osuserverlist.bjar.models.essentials.Player;
import com.osuserverlist.bjar.modules.database.Database;
import com.osuserverlist.bjar.modules.database.MySQL;
import com.osuserverlist.bjar.modules.web.engine.Host;
import com.osuserverlist.bjar.modules.web.engine.HttpMethod;
import com.osuserverlist.bjar.modules.web.engine.Path;
import com.osuserverlist.bjar.repos.UserRepository;
import com.osuserverlist.bjar.server.Server;

import io.javalin.http.Context;
import io.javalin.http.Handler;

@Host("osu.")
@Path("/web/osu-getfriends.php")
@HttpMethod("GET")
public class OsuGetFriendsHandler implements Handler {

    @Override
    public void handle(@NotNull Context ctx) throws Exception {

        String username = ctx.queryParam("u");
        String passwordHash = ctx.queryParam("h");
        String apiIdent = String.format("%s|%s", username, passwordHash);

        Server server = Server.getInstance();
        Player player = server.playerManager.getByApiIdent(apiIdent);

        if (player == null) {
            ctx.status(401).result("Invalid credentials.");
            return;
        }

        try (MySQL mysql = Database.getConnection()) {
            UserRepository userRepository = new UserRepository(mysql);

            String response = String.join("\n", userRepository.getFriendIds(player.getId()).stream().map(String::valueOf).toArray(String[]::new));
            ctx.result(response);
        }

    }

}
