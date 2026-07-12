package com.osuserverlist.bjar.packets.client.handlers;

import java.sql.SQLException;

import org.slf4j.Logger;

import com.osuserverlist.bjar.Server;
import com.osuserverlist.bjar.models.essentials.Player;
import com.osuserverlist.bjar.modules.database.Database;
import com.osuserverlist.bjar.modules.database.MySQL;
import com.osuserverlist.bjar.modules.logger.LoggerFactory;
import com.osuserverlist.bjar.packets.BanchoPacket;
import com.osuserverlist.bjar.packets.client.engine.BanchoPacketReader;
import com.osuserverlist.bjar.packets.client.engine.ClientPacket;
import com.osuserverlist.bjar.packets.client.engine.ClientPackets;
import com.osuserverlist.bjar.packets.server.handlers.user.UserFriendListPacket;
import com.osuserverlist.bjar.repos.UserRepository;

public class UserUtilPackets {

    private static final Logger logger = LoggerFactory.getLogger(UserUtilPackets.class);
    
    @ClientPacket(ClientPackets.PING)
    public boolean ping(BanchoPacket packet, BanchoPacketReader reader, Player player) {
        player.setLastPing(System.currentTimeMillis());
        return true;
    }

    @ClientPacket(ClientPackets.LOGOUT)
    public boolean logout(BanchoPacket packet, BanchoPacketReader reader, Player player) {
        logger.info("Player {} has logged out.", player.toString());

        Server.getInstance().playerManager.disconnect(player);
        return true;
    }

    @ClientPacket(ClientPackets.FRIEND_ADD)
    public boolean addFriend(BanchoPacket packet, BanchoPacketReader reader, Player player) {
        int userId = reader.readInt();

        try (MySQL mysql = Database.getConnection()) {
            UserRepository userRepository = new UserRepository(mysql);

            userRepository.addFriend(player.getId(), userId);
            player.sendPacket(new UserFriendListPacket(userRepository.getFriendIds(player.getId())));
        } catch (SQLException e) {
            logger.error("Error occurred while adding friend", e);
        }

        return true;
    }

    @ClientPacket(ClientPackets.FRIEND_REMOVE)
    public boolean removeFriend(BanchoPacket packet, BanchoPacketReader reader, Player player) {
        int userId = reader.readInt();

        try (MySQL mysql = Database.getConnection()) {
            UserRepository userRepository = new UserRepository(mysql);

            userRepository.removeFriend(player.getId(), userId);
            player.sendPacket(new UserFriendListPacket(userRepository.getFriendIds(player.getId())));
        } catch (SQLException e) {
            logger.error("Error occurred while removing friend", e);
        }

        return true;
    }

    @ClientPacket(ClientPackets.UNHANDLED_PACKET) 
    public boolean handleUnhandledPacket(BanchoPacket packet, BanchoPacketReader reader, Player player) {
        logger.warn("Unhandled packet: " + reader.getCurrentPacketId() + " (" + packet.type.name() + ")");
        return true;
    }

}
