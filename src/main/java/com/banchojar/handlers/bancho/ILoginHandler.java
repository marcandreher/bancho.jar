package com.banchojar.handlers.bancho;

import com.banchojar.Player;
import com.banchojar.db.models.UserRecord;
import com.banchojar.handlers.bancho.LoginHandler.LoginResponse;
import com.banchojar.packets.server.PacketSender;

public interface ILoginHandler {

    public Player handleLogin(PacketSender sender, LoginResponse loginResponse, UserRecord dbUser);
    public UserRecord getUserRecord(LoginResponse loginResponse);
    
}
