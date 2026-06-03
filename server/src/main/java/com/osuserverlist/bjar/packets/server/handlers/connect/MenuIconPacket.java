package com.osuserverlist.bjar.packets.server.handlers.connect;

import java.io.IOException;

import com.osuserverlist.bjar.models.config.ServerConfiguration.MenuIcon;
import com.osuserverlist.bjar.models.essentials.Player;
import com.osuserverlist.bjar.packets.BanchoPacket;
import com.osuserverlist.bjar.packets.server.BanchoPacketWriter;
import com.osuserverlist.bjar.packets.server.ServerPacketHandler;
import com.osuserverlist.bjar.packets.server.ServerPackets;
import com.osuserverlist.bjar.server.Server;

public class MenuIconPacket implements ServerPacketHandler {
    
    final ServerPackets type = ServerPackets.MAIN_MENU_ICON;

    @Override
    public boolean handle(BanchoPacket packet, BanchoPacketWriter writer, Player sender) throws IOException {
        MenuIcon menuIcon = Server.getInstance().config.getMenuIcon();

        writer.startPacket(type.getValue());
        writer.writeString(menuIcon.getImageUrl() + "|" + menuIcon.getOutlink());
        writer.endPacket();
        return true;
    }

}
