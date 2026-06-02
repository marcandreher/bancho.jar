package com.osuserverlist.models.engine;

import java.util.UUID;

import io.javalin.http.Context;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@ToString
public class LoginResponse {
    private boolean success = false;

    @Setter
    private String uuid;

    private String ip;

    private String username;
    private String passwordMd5;

    private String buildName;
    private String utcOffset;
    private boolean displayCityLocation;
    private boolean friendOnlyDms;

    private String executeableNameHash;
    private String networkInterfacesHash;
    private String registryKeyHash;
    private String diskDriveHash;

    public LoginResponse(Context ctx) {
        String[] body = ctx.body().split("\n");

        if (body.length < 3) {
            return;
        }

        uuid = UUID.randomUUID().toString();
        username = body[0];
        passwordMd5 = body[1];
        ip = ctx.header("X-Real-IP");

        String[] clientInfo = body[2].split("\\|");

        if (clientInfo.length < 5) {
            return;
        }

        buildName = clientInfo[0];
        utcOffset = clientInfo[1];
        displayCityLocation = Integer.parseInt(clientInfo[2]) == 1;
        friendOnlyDms = Integer.parseInt(clientInfo[4]) == 1;

        String[] clientHashes = clientInfo[3].split(":");
        if (clientHashes.length < 5) {
            return;
        }

        executeableNameHash = clientHashes[0];
        networkInterfacesHash = clientHashes[1];
        registryKeyHash = clientHashes[2];
        diskDriveHash = clientHashes[3];
        success = true;
    }
}