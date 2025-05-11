package com.banchojar.handlers.osu;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

import org.jetbrains.annotations.NotNull;

import com.github.f4b6a3.uuid.UuidCreator;

import io.javalin.http.Context;
import io.javalin.http.Handler;
import io.javalin.http.UploadedFile;

public class ScreenshotHandler implements Handler {

    @Override
    public void handle(@NotNull Context ctx) throws Exception {
        UploadedFile screenshot = ctx.uploadedFile("ss");
        if (screenshot == null) {
            ctx.status(400).result("Screenshot file missing.");
            return;
        }

        byte[] bytes = screenshot.content().readAllBytes();
        if (bytes.length > (4 * 1024 * 1024)) { // 4 MB limit
            ctx.status(400).result("Screenshot file too large.");
            return;
        }

        // Check file headers (simple header matching)
        boolean isPng = bytes.length > 8 &&
                bytes[0] == (byte) 0x89 &&
                bytes[1] == (byte) 0x50 &&
                bytes[2] == (byte) 0x4E &&
                bytes[3] == (byte) 0x47;

        boolean isJpeg = bytes.length > 4 &&
                bytes[0] == (byte) 0xFF &&
                bytes[1] == (byte) 0xD8 &&
                bytes[bytes.length - 2] == (byte) 0xFF &&
                bytes[bytes.length - 1] == (byte) 0xD9;

        String extension;
        if (isJpeg) {
            extension = "jpeg";
        } else if (isPng) {
            extension = "png";
        } else {
            ctx.status(400).result("Invalid file type.");
            return;
        }

        // Generate unique filename
        String filename;
        Path uploadDir = Paths.get(".data/screenshots");
        Files.createDirectories(uploadDir);
        UUID fileUuid = UuidCreator.getRandomBased();

        do {
            filename = fileUuid.toString();
            filename += "." + extension;
        } while (Files.exists(uploadDir.resolve(filename)));

        // Save file
        Files.write(uploadDir.resolve(filename), bytes);
        ctx.result(filename);
    }

}
