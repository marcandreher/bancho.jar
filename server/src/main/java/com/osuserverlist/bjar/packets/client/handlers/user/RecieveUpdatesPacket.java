package com.osuserverlist.bjar.packets.client.handlers.user;

import java.io.IOException;

import org.slf4j.Logger;

import com.osuserverlist.bjar.models.essentials.Player;
import com.osuserverlist.bjar.models.osu.PresenceFilter;
import com.osuserverlist.bjar.modules.logger.LoggerFactory;
import com.osuserverlist.bjar.packets.BanchoPacket;
import com.osuserverlist.bjar.packets.client.BanchoPacketHandler;
import com.osuserverlist.bjar.packets.client.BanchoPacketReader;
import com.osuserverlist.bjar.packets.client.ClientPackets;
import com.osuserverlist.bjar.packets.client.engine.ClientPacket;

@ClientPacket(ClientPackets.RECEIVE_UPDATES)
public class RecieveUpdatesPacket implements BanchoPacketHandler {

    private final static Logger logger = LoggerFactory.getLogger(RecieveUpdatesPacket.class);
    
    @Override
    public boolean handle(BanchoPacket packet, BanchoPacketReader reader, Player player) throws IOException {
        int presenceFilter = reader.readInt();
        PresenceFilter filter = PresenceFilter.values()[presenceFilter];

        if(filter == null) {
            logger.warn("Player {} send invalid presence filter value {}", player.getUsername(), presenceFilter);
            return true;
        }

        player.setPresenceFilter(filter.getId());
        return true;
    }

}
