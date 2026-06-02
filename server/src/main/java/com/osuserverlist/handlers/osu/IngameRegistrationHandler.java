package com.osuserverlist.handlers.osu;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import com.osuserverlist.handlers.engine.Host;
import com.osuserverlist.handlers.engine.HttpMethod;
import com.osuserverlist.handlers.engine.Path;
import com.osuserverlist.modules.logger.LoggerFactory;

import de.marcandreher.fusionkit.core.database.Database;
import de.marcandreher.fusionkit.core.database.MySQL;
import io.javalin.http.Context;
import io.javalin.http.Handler;

@Host("osu.")
@Path("/users")
@HttpMethod("POST")
public class IngameRegistrationHandler implements Handler {

    private static final Logger logger = LoggerFactory.getLogger(IngameRegistrationHandler.class);

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
        if (!errors.isEmpty()) {
            ctx.status(400).json(Map.of("form_error", Map.of("user", errors)));
            return;
        }

        if (check != 0) {
            return;
        }
        
        MessageDigest md = MessageDigest.getInstance("MD5");
        byte[] md5Bytes = md.digest(password.getBytes(StandardCharsets.UTF_8));

        StringBuilder hexString = new StringBuilder();
        for (byte b : md5Bytes) {
            hexString.append(String.format("%02x", b));
        }
        String md5Hex = hexString.toString();

        // TODO: Handle Geolocation

        // TODO: Handle creation time

        // TODO BCrypt

        try (MySQL mysql = Database.getConnection()) {
            ResultSet checkNameEmailRs = mysql.query("SELECT `name`, `email` FROM `users` WHERE `name` = ? OR `email` = ?", username, email).executeQuery();
            if (checkNameEmailRs.next()) {
                String existingName = checkNameEmailRs.getString("name");
                String existingEmail = checkNameEmailRs.getString("email");

                if (existingName.equalsIgnoreCase(username)) {
                    errors.computeIfAbsent("username", k -> new ArrayList<>())
                            .add("Username is already taken.");
                }
                if (existingEmail.equalsIgnoreCase(email)) {
                    errors.computeIfAbsent("email", k -> new ArrayList<>())
                            .add("Email is already registered.");   
                }
                if (!errors.isEmpty()) {
                    ctx.status(400).json(Map.of("form_error", Map.of("user", errors)));
                    return;
                }
            }


            mysql.exec("INSERT INTO `users`(`name`, `safe_name`, `email`, `pw_bcrypt`) VALUES (?, ?, ?, ?)", username, username.toLowerCase().replaceAll(" ", "_"), email, md5Hex);
        }

        logger.info("Registered new user: " + username);

        ctx.status(200).result("ok");

    }

}
