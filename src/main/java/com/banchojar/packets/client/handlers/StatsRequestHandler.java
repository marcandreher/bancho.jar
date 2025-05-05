package com.banchojar.packets.client.handlers;

import java.io.IOException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.banchojar.Player;
import com.banchojar.Server;
import com.banchojar.packets.BanchoPacket;
import com.banchojar.packets.client.BanchoPacketHandler;
import com.banchojar.packets.client.BanchoPacketReader;
import com.banchojar.packets.server.PacketSender;
import com.banchojar.packets.server.handlers.UserStatsHandler;
import com.google.gson.Gson;
import com.squareup.okhttp.OkHttpClient;

public class StatsRequestHandler implements BanchoPacketHandler {

    private static final OkHttpClient client = new OkHttpClient();
    private static final Gson gson = new Gson();

    public Logger logger = LoggerFactory.getLogger(BanchoPacketHandler.class);
  
    @Override
    public boolean handle(BanchoPacket packet, BanchoPacketReader reader, Player player) throws IOException {
        List<Integer> userIds = reader.readIntList();
      
        int processedCount = 0;
        for (int userId : userIds) {

            if(player.getId() == userId) {
                if(player.getLastServedStatsRequest() + 10000 > System.currentTimeMillis()) {
                    continue; // Skip if the player is requesting stats too frequently
                }
    
                player.setLastServedStatsRequest(System.currentTimeMillis());
            }

            


            Player requestedPlayer = Server.players.values().stream()
                .filter(p -> p.getId() == userId)
                .findFirst()
                .orElse(null);

            if (requestedPlayer != null) {

                
                
                player.addPacketToStack(new UserStatsHandler(requestedPlayer.getId()));
                
                processedCount++;
              
            } else {
                logger.warn("Requested userId " + userId + " not found in server players.");
            }
        }

        return true;
    }
}
