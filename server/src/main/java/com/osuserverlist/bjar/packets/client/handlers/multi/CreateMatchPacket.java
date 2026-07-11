package com.osuserverlist.bjar.packets.client.handlers.multi;

import java.io.IOException;

import org.slf4j.Logger;

import com.osuserverlist.bjar.Server;
import com.osuserverlist.bjar.models.essentials.BanchoChannel;
import com.osuserverlist.bjar.models.essentials.Match;
import com.osuserverlist.bjar.models.essentials.Player;
import com.osuserverlist.bjar.models.osu.match.SlotStatus;
import com.osuserverlist.bjar.modules.logger.LoggerFactory;
import com.osuserverlist.bjar.packets.BanchoPacket;
import com.osuserverlist.bjar.packets.client.BanchoPacketHandler;
import com.osuserverlist.bjar.packets.client.BanchoPacketReader;
import com.osuserverlist.bjar.packets.client.ClientPackets;
import com.osuserverlist.bjar.packets.client.engine.ClientPacket;
import com.osuserverlist.bjar.packets.server.handlers.channel.ChannelInfoPacket;
import com.osuserverlist.bjar.packets.server.handlers.channel.ChannelJoinSuccessPacket;
import com.osuserverlist.bjar.packets.server.handlers.multi.MatchJoinSuccessPacket;

@ClientPacket(ClientPackets.CREATE_MATCH)
public class CreateMatchPacket implements BanchoPacketHandler {

    private final static Logger logger = LoggerFactory.getLogger(CreateMatchPacket.class);

    @Override
    public boolean handle(BanchoPacket packet, BanchoPacketReader reader, Player player) throws IOException {
        Match match = reader.readMatch();
        Server.getInstance().matchManager.add(match);

        String channelName = "#multi_" + match.getMatchId();
        BanchoChannel matchChannel = BanchoChannel.builder()
                .id(channelName)
                .name(channelName)
                .alias("#multiplayer")
                .description("Multiplayer lobby for match " + match.getMatchId() + ".")
                .autoJoin(false)
                .readPriv(0)
                .writePriv(0)
                .visible(false)
                .build();

        Server.getInstance().channelManager.add(matchChannel);
        Server.getInstance().channelManager.forceJoinChannel(channelName, player);
        player.setMatch(match);

        int freeSlot = match.getFreeSlot();
        match.getSlots()[freeSlot].setPlayerId(player.getId());
        match.getSlots()[freeSlot].setStatus(SlotStatus.NOT_READY.byteValue); 
        match.getPlayers().add(player);
        // TODO: Handle restriction and silenced players

        logger.info("Player {} created a match {}", player.toString(), match.toString());
        player.sendPacket(new MatchJoinSuccessPacket(match));
        player.sendPacket(new ChannelJoinSuccessPacket(matchChannel.getAlias()));
        player.sendPacket(new ChannelInfoPacket(matchChannel.getAlias(), matchChannel.getDescription(), matchChannel.getPlayerCount()));
        return true;
    }
    
}
