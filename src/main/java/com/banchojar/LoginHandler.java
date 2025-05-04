package com.banchojar;

import io.javalin.Javalin;
import io.javalin.http.Context;
import io.javalin.http.HttpStatus;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class LoginHandler {

    private static final Pattern USERNAME_PATTERN = Pattern.compile("^[a-zA-Z0-9_ ]{2,15}$");
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[^@\\s]{1,200}@[a-zA-Z0-9.-]{1,30}\\.[a-zA-Z]{1,24}$");

    public static void registerRoutes(Javalin app) {
        app.post("/users", LoginHandler::registerAccount);
    }

    private static void registerAccount(Context ctx) {
        String username = ctx.formParam("user[username]");
        String email = ctx.formParam("user[user_email]");
        String password = ctx.formParam("user[password]");
        String checkParam = ctx.formParam("check");

        if (username == null || email == null || password == null || checkParam == null) {
            ctx.status(HttpStatus.BAD_REQUEST).result("Missing required params");
            return;
        }

        int check;
        try {
            check = Integer.parseInt(checkParam);
        } catch (NumberFormatException e) {
            ctx.status(HttpStatus.BAD_REQUEST).result("Invalid check parameter");
            return;
        }

        Map<String, String> errors = new HashMap<>();

        // Validate username
        if (!USERNAME_PATTERN.matcher(username).matches()) {
            errors.put("username", "Must be 2-15 characters in length and contain only letters, numbers, spaces, or underscores.");
        }

        // Validate email
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            errors.put("email", "Invalid email syntax.");
        }

        // Validate password
        if (password.length() < 8 || password.length() > 32) {
            errors.put("password", "Must be 8-32 characters in length.");
        }

        if (errors.isEmpty()) {
            if (check == 0) {
                // Simulate account creation
                System.out.println("Account created for username: " + username);
            }
            ctx.status(HttpStatus.OK).result("ok");
        } else {
            ctx.status(HttpStatus.BAD_REQUEST).json(errors);
        }
    }
}