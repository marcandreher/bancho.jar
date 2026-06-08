package com.osuserverlist.bjar.handlers.osu;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bouncycastle.crypto.generators.OpenBSDBCrypt;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import com.osuserverlist.bjar.models.database.UserEntity;
import com.osuserverlist.bjar.models.osu.Privileges;
import com.osuserverlist.bjar.modules.database.Database;
import com.osuserverlist.bjar.modules.database.MySQL;
import com.osuserverlist.bjar.modules.logger.LoggerFactory;
import com.osuserverlist.bjar.modules.web.engine.Host;
import com.osuserverlist.bjar.modules.web.engine.HttpMethod;
import com.osuserverlist.bjar.modules.web.engine.Path;
import com.osuserverlist.bjar.repos.UserRepository;

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
        Integer check = ctx.formParamAsClass("check", Integer.class).getOrNull();
        int checkValue = check != null ? check : 0;

        if (username == null || email == null || password == null) {
            ctx.status(400).result("Missing required params");
            return;
        }

        Map<String, List<String>> errors = new HashMap<>();

        // Username validation
        if (!username.matches("^[\\w \\[\\]-]{2,15}$")) {
            errors.computeIfAbsent("username", k -> new ArrayList<>())
                    .add("Must be 2-15 characters in length.");
        }

        if (username.contains("_") && username.contains(" ")) {
            errors.computeIfAbsent("username", k -> new ArrayList<>())
                    .add("May contain '_' or ' ', but not both.");
        }

        // Password validation (matches bancho.py)
        if (password.length() < 8 || password.length() > 32) {
            errors.computeIfAbsent("password", k -> new ArrayList<>())
                    .add("Must be 8-32 characters in length.");
        }

        if (password.chars().distinct().count() <= 3) {
            errors.computeIfAbsent("password", k -> new ArrayList<>())
                    .add("Must have more than 3 unique characters.");
        }

        // Validation failed
        if (!errors.isEmpty()) {
            ctx.status(400).json(Map.of(
                    "form_error",
                    Map.of("user", errors)));
            return;
        }

        // Only register when check == 0
        if (checkValue == 0) {
            MessageDigest md;
            try {
                md = MessageDigest.getInstance("MD5");
            } catch (NoSuchAlgorithmException e) {
                logger.error("MD5 algorithm not found", e);
                ctx.status(500).result("Internal Server Error");
                return;
            }
            byte[] md5Bytes = md.digest(password.getBytes(StandardCharsets.UTF_8));

            StringBuilder hexString = new StringBuilder();
            for (byte b : md5Bytes) {
                hexString.append(String.format("%02x", b));
            }

            String md5Hex = hexString.toString();

            byte[] salt = new byte[16];
            new SecureRandom().nextBytes(salt);

            String bcryptHash = OpenBSDBCrypt.generate(
                    md5Hex.toCharArray(),
                    salt,
                    12);

            handleRegistration(username, email, bcryptHash, errors);

            if (!errors.isEmpty()) {
                ctx.status(400).json(Map.of(
                        "form_error",
                        Map.of("user", errors)));
                return;
            }
        }

        ctx.status(200).result("ok");
    }

    private void handleRegistration(String username, String email, String bcryptHash,
            Map<String, List<String>> errors) {
        try (MySQL mysql = Database.getConnection()) {
            UserRepository userRepository = new UserRepository(mysql);
            UserEntity existingUser = userRepository.getUserByNameOrMail(username, email);
            if (existingUser != null) {
                if (existingUser.getName().equalsIgnoreCase(username)) {
                    errors.computeIfAbsent("username", k -> new ArrayList<>())
                            .add("Username is already taken.");
                }
                if (existingUser.getEmail().equalsIgnoreCase(email)) {
                    errors.computeIfAbsent("email", k -> new ArrayList<>())
                            .add("Email is already registered.");
                }

                return;
            }

            userRepository.insertUser(username, username.toLowerCase().replaceAll(" ", "_"), email, bcryptHash);

            Integer userId = mysql.lastInsertId();

            if(userId == 3) {
                // First user gets all privileges
                int privs = Privileges.allPrivsToInt();
                userRepository.updateUserPrivileges(userId, privs);
            }

            if (userId == null) {
                logger.error("Failed to retrieve last insert ID for user: " + username);
                errors.computeIfAbsent("database", k -> new ArrayList<>())
                        .add("An error occurred while creating the account. Please try again.");
                return;
            }

            for (int i = 0; i <= 8; i++) {
                if (i == 7)
                    continue;

                userRepository.insertStats(userId, i);
            }

            logger.info("Registered new user: <{}>({})", username, userId);
        } catch (SQLException e) {
            logger.error("Database error during registration for user: " + username, e);
            errors.computeIfAbsent("database", k -> new ArrayList<>())
                    .add("An error occurred while creating the account. Please try again.");
        }

    }

}
