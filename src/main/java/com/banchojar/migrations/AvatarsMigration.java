package com.banchojar.migrations;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.jooq.DSLContext;

import com.banchojar.App;
import com.banchojar.utils.VersionInfo;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class AvatarsMigration implements Migration {

    private final OkHttpClient client = new OkHttpClient();

    @Override
    public void migrate(DSLContext dsl) {
        File avatarsDir = new File(".data/avatars");
        if (!avatarsDir.exists()) {
            avatarsDir.mkdirs();
        }

        String userId = "default";
        String avatarUrl = VersionInfo.getDefaultAvatar();

        Request request = new Request.Builder().url(avatarUrl).build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                App.logger.error("Failed to fetch avatar: " + response.code());
                return;
            }

            ResponseBody body = response.body();
            if (body == null) {
                App.logger.error("Response body is null");
                return;
            }

            String contentType = response.header("Content-Type", "image/png");
            String extension = getExtensionFromContentType(contentType);
            if (extension == null) {
                App.logger.error("Unsupported content type: " + contentType);
                return;
            }

            File avatarFile = new File(avatarsDir, userId + "." + extension);
            try (InputStream in = body.byteStream(); FileOutputStream out = new FileOutputStream(avatarFile)) {
                byte[] buffer = new byte[8192];
                int len;
                while ((len = in.read(buffer)) != -1) {
                    out.write(buffer, 0, len);
                }
            }
        } catch (IOException e) {
        App.logger.error(e.getMessage(), e);
        }
    }

    private String getExtensionFromContentType(String contentType) {
        switch (contentType) {
            case "image/png":
                return "png";
            case "image/jpeg":
                return "jpg";
            case "image/gif":
                return "gif";
            default:
                return null;
        }
    }

    @Override
    public void rollback(DSLContext dsl) {}

    @Override
    public boolean isNeeded() {
        File avatarsDir = new File(".data/avatars");
        if (!avatarsDir.exists()) {
            return true;
        }
        return avatarsDir.canWrite();
    }
}
