package com.banchojar.handlers.osu;

import java.util.Optional;
import java.util.Set;

import org.jetbrains.annotations.NotNull;
import org.jooq.impl.DSL;

import com.banchojar.Player;
import com.banchojar.Server;
import com.banchojar.handlers.BanchoJarRoles;

import io.javalin.http.Context;
import io.javalin.http.Handler;
import io.javalin.http.UnauthorizedResponse;
import io.javalin.security.RouteRole;

public class OsuAuth implements Handler {

    @Override
    public void handle(@NotNull Context ctx) throws Exception {
        Set<RouteRole> permittedRoles = ctx.routeRoles();
        if (permittedRoles == null || permittedRoles.isEmpty()) return;

        if (permittedRoles.contains(BanchoJarRoles.AGENT) && !"osu!".equals(ctx.userAgent())) {
            throw new UnauthorizedResponse("Only osu! client is allowed to access this endpoint.");
        }

        if(permittedRoles.size() == 1) {
            return;
        }

        String username = null;
        String password = null;

        if (permittedRoles.contains(BanchoJarRoles.FORM)) {
            username = ctx.formParam(extractParamName(permittedRoles, true));
            password = ctx.formParam(extractParamName(permittedRoles, false));
        } else if (permittedRoles.contains(BanchoJarRoles.QUERY)) {
            username = ctx.queryParam(extractParamName(permittedRoles, true));
            password = ctx.queryParam(extractParamName(permittedRoles, false));
        }

        Optional<Player> authedUser = authenticate(username, password);
        if (authedUser.isPresent()) {
            ctx.sessionAttribute("player", authedUser.get());
        } else {
            throw new UnauthorizedResponse("You are not logged in.");
        }
    }

    private String extractParamName(Set<RouteRole> roles, boolean first) {
        return roles.stream()
                .map(Role -> Role.toString().toLowerCase())
                .filter(s -> s.startsWith("osuparam_"))
                .skip(first ? 0 : 1)
                .findFirst()
                .map(s -> s.substring("osuparam_".length()))
                .orElse(null);
    }

    private Optional<Player> authenticate(String username, String passwordMd5) {
        if (username == null || passwordMd5 == null) return Optional.empty();

        Integer userId = Server.dsl.select(DSL.field("id", Integer.class))
                .from(DSL.table("users"))
                .where(DSL.field("username").eq(username.replace(" ", ""))
                        .and(DSL.field("password_hash").eq(passwordMd5)))
                .fetchOneInto(Integer.class);

        return Optional.ofNullable(userId)
                .flatMap(id -> Server.players.values().stream()
                        .filter(p -> p.getId() == id)
                        .findFirst());
    }
}
