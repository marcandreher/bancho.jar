package com.osuserverlist.bjar.packets.server;

import com.osuserverlist.bjar.Server;
import com.osuserverlist.bjar.models.config.ServerConfiguration.MenuIcon;
import com.osuserverlist.bjar.models.essentials.Player;
import com.osuserverlist.bjar.modules.BanchoPacketWriter;
import com.osuserverlist.bjar.modules.ServerPacketEngine.PacketHandler;
import com.osuserverlist.bjar.modules.ServerPacketEngine.ServerPacket;
import com.osuserverlist.bjar.modules.ServerPacketEngine.ServerPacketHandler;
import com.osuserverlist.bjar.modules.ServerPacketEngine.ServerPackets;

import lombok.Value;

public class LoginServerPackets {

    @Value
    public static class LoginReplyPacket implements ServerPacket {
        private int id;
    }

    @Value
    public static class MenuIconPacket implements ServerPacket { }

    @Value
    public static class ProtocolVersionPacket implements ServerPacket { }

    @Value
    public static class PrivilegesPacket implements ServerPacket { 
        private int privs;
    }

    @PacketHandler(LoginReplyPacket.class)
    public static final class LoginReplyHandler implements ServerPacketHandler<LoginReplyPacket> {
        @Override
        public void write(LoginReplyPacket packet, BanchoPacketWriter writer, Player player) {
            writer.startPacket(ServerPackets.LOGIN_REPLY);
            writer.writeInt(packet.getId());
            writer.endPacket();
        }
    }

    @PacketHandler(MenuIconPacket.class)
    public static final class MenuIconHandler implements ServerPacketHandler<MenuIconPacket> {
        @Override
        public void write(MenuIconPacket packet, BanchoPacketWriter writer, Player player) {
            MenuIcon menuIcon = Server.getInstance().config.getMenuIcon();

            writer.startPacket(ServerPackets.MAIN_MENU_ICON);
            writer.writeString(menuIcon.getImageUrl() + "|" + menuIcon.getOutlink());
            writer.endPacket();
        }
    }

    @PacketHandler(ProtocolVersionPacket.class)
    public static final class ProtocolVersionHandler implements ServerPacketHandler<ProtocolVersionPacket> {
        @Override
        public void write(ProtocolVersionPacket packet, BanchoPacketWriter writer, Player player) {
            writer.startPacket(ServerPackets.PROTOCOL_VERSION);
            writer.writeInt(19);
            writer.endPacket();
        }
    }

    @PacketHandler(PrivilegesPacket.class)
    public static final class PrivilegesHandler implements ServerPacketHandler<PrivilegesPacket> {
        @Override
        public void write(PrivilegesPacket packet, BanchoPacketWriter writer, Player player) {
            writer.startPacket(ServerPackets.PRIVILEGES);
            writer.writeInt(packet.getPrivs());
            writer.endPacket();
        }
    }


}
