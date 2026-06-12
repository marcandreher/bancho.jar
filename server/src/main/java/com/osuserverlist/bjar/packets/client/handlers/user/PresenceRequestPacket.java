package com.osuserverlist.bjar.packets.client.handlers.user;

import java.io.IOException;
import java.util.List;

import org.slf4j.Logger;

import com.osuserverlist.bjar.models.essentials.Player;
import com.osuserverlist.bjar.modules.logger.LoggerFactory;
import com.osuserverlist.bjar.packets.BanchoPacket;
import com.osuserverlist.bjar.packets.client.BanchoPacketHandler;
import com.osuserverlist.bjar.packets.client.BanchoPacketReader;
import com.osuserverlist.bjar.packets.client.ClientPackets;
import com.osuserverlist.bjar.packets.client.engine.ClientPacket;
import com.osuserverlist.bjar.packets.server.handlers.user.UserPresencePacket;
import com.osuserverlist.bjar.server.Server;

@ClientPacket(ClientPackets.USER_PRESENCE_REQUEST)
public class PresenceRequestPacket implements BanchoPacketHandler {

    private final static Logger logger = LoggerFactory.getLogger(PresenceRequestPacket.class);

    @Override
    public boolean handle(BanchoPacket packet, BanchoPacketReader reader, Player player) throws IOException {
        List<Integer> userIds = reader.readIntList();

        logger.debug("Player {} requested presence info for user IDs {}", player.getUsername(), userIds);

        for (Integer userId : userIds) {
            
            Player requestedPlayer = Server.getInstance().playerManager.getById(userId);

            if (requestedPlayer != null) {
                player.sendPacket(new UserPresencePacket(requestedPlayer.getId()));
            } else {
                logger.warn("Requested player not found for {}", player.toString());
            }

        }

        return true;
    }

}
