package com.osuserverlist.bjar.packets.client.handlers.friend;

import java.io.IOException;
import java.sql.SQLException;

import org.slf4j.Logger;

import com.osuserverlist.bjar.models.essentials.Player;
import com.osuserverlist.bjar.modules.database.Database;
import com.osuserverlist.bjar.modules.database.MySQL;
import com.osuserverlist.bjar.modules.logger.LoggerFactory;
import com.osuserverlist.bjar.packets.BanchoPacket;
import com.osuserverlist.bjar.packets.client.BanchoPacketHandler;
import com.osuserverlist.bjar.packets.client.BanchoPacketReader;
import com.osuserverlist.bjar.packets.server.handlers.user.UserFriendListPacket;
import com.osuserverlist.bjar.repos.UserRepository;

public class AddFriendPacket implements BanchoPacketHandler {

    private final static Logger logger = LoggerFactory.getLogger(AddFriendPacket.class);

    @Override
    public boolean handle(BanchoPacket packet, BanchoPacketReader reader, Player player) throws IOException {
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

}
