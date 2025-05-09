package com.banchojar.handlers.osu;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jetbrains.annotations.NotNull;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;

import com.banchojar.Server;
import com.banchojar.db.models.UserRecord;

import io.javalin.http.Context;
import io.javalin.http.Handler;

public class IngameRegistrationHandler implements Handler {

    @Override
    public void handle(@NotNull Context ctx) throws Exception {
        String username = ctx.formParam("user[username]");
        String email = ctx.formParam("user[user_email]");
        String password = ctx.formParam("user[password]");
        String checkParam = ctx.formParam("check");
        int check = checkParam != null ? Integer.parseInt(checkParam) : 1;

        if (username == null || email == null || password == null) {
            ctx.status(400).result("Missing required params");
            return;
        }

        Map<String, List<String>> errors = new HashMap<>();

        // Validate username
        if (!username.matches("^[\\w \\[\\]-]{2,15}$")) {
            errors.computeIfAbsent("username", k -> new ArrayList<>())
                    .add("Must be 2-15 characters in length.");
        }
        if (username.contains("_") && username.contains(" ")) {
            errors.computeIfAbsent("username", k -> new ArrayList<>())
                    .add("May contain '_' or ' ', but not both.");
        }

        DSLContext db = Server.dsl;
        if (errors.get("username") == null &&
                db.fetchExists(DSL.selectOne().from(DSL.table("users")).where(DSL.field("username").eq(username)))) {
            errors.computeIfAbsent("username", k -> new ArrayList<>())
                    .add("Username already taken by another player.");
        }

        // Validate email
        if (!email.matches("^[^@\\s]{1,200}@[a-zA-Z0-9.-]{1,30}\\.[a-zA-Z]{2,24}$")) {
            errors.computeIfAbsent("user_email", k -> new ArrayList<>())
                    .add("Invalid email syntax.");
        } else if (db.fetchExists(DSL.selectOne().from(DSL.table("users")).where(DSL.field("email").eq(email)))) {
            errors.computeIfAbsent("user_email", k -> new ArrayList<>())
                    .add("Email already taken by another player.");
        }

        // Validate password
        if (password.length() < 8 || password.length() > 32) {
            errors.computeIfAbsent("password", k -> new ArrayList<>())
                    .add("Must be 8-32 characters in length.");
        }
        if (password.chars().distinct().count() <= 3) {
            errors.computeIfAbsent("password", k -> new ArrayList<>())
                    .add("Must have more than 3 unique characters.");
        }
        if (!errors.isEmpty()) {
            ctx.status(400).json(Map.of("form_error", Map.of("user", errors)));
            return;
        }

        if (check == 0) {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] md5Bytes = md.digest(password.getBytes(StandardCharsets.UTF_8));

            StringBuilder hexString = new StringBuilder();
            for (byte b : md5Bytes) {
                hexString.append(String.format("%02x", b)); 
            }
            String md5Hex = hexString.toString();

            // TODO: Handle Geolocation

            Server.dsl.insertInto(DSL.table("users"))
                    .columns(DSL.field("username"), DSL.field("password_hash"), DSL.field("email"),
                            DSL.field("country"))
                    .values(username, md5Hex, email, "US")
                    .execute();

            UserRecord userRecord = Server.dsl.selectFrom(DSL.table("users"))
                    .where(DSL.field("username").eq(username))
                    .fetchOneInto(UserRecord.class);

            for (int mode = 0; mode <= 3; mode++) {
                Server.dsl.insertInto(DSL.table("users_stats"))
                        .columns(DSL.field("user_id"), DSL.field("mode"), DSL.field("ranked_score"),
                                DSL.field("accuracy"), DSL.field("play_count"), DSL.field("total_score"),
                                DSL.field("global_rank"), DSL.field("pp"))
                        .values(userRecord.id(), mode, 0, 0.0, 0, 0, 0, 0)
                        .execute();
            }

            ctx.status(200).result("ok");
        }
    }
    
}
