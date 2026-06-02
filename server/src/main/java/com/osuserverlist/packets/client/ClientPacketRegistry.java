package com.osuserverlist.packets.client;

import java.util.HashMap;
import java.util.Map;

import com.osuserverlist.packets.client.handlers.PingHandler;
import com.osuserverlist.packets.client.handlers.user.PresenceRequestHandler;
import com.osuserverlist.packets.client.handlers.user.StatsRequestHandler;
import com.osuserverlist.packets.client.handlers.user.StatusUpdateHandler;
import com.osuserverlist.packets.client.handlers.user.UserSelfRequestStatusHandler;

public class ClientPacketRegistry {
    public static final Map<ClientPackets, BanchoPacketHandler> packetHandlers = new HashMap<>();

    static {
        packetHandlers.put(ClientPackets.PING, new PingHandler());
        packetHandlers.put(ClientPackets.USER_PRESENCE_REQUEST, new PresenceRequestHandler());
        packetHandlers.put(ClientPackets.CHANGE_ACTION, new StatusUpdateHandler());
        packetHandlers.put(ClientPackets.USER_STATS_REQUEST, new StatsRequestHandler());
        packetHandlers.put(ClientPackets.REQUEST_STATUS_UPDATE, new UserSelfRequestStatusHandler());
    }
}
