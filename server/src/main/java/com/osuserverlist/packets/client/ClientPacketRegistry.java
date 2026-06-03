package com.osuserverlist.packets.client;

import java.util.HashMap;
import java.util.Map;

import com.osuserverlist.packets.client.handlers.PingHandler;
import com.osuserverlist.packets.client.handlers.chat.ChannelJoinPacket;
import com.osuserverlist.packets.client.handlers.chat.ChannelLeavePacket;
import com.osuserverlist.packets.client.handlers.chat.SendPrivateMessageHandler;
import com.osuserverlist.packets.client.handlers.chat.SendPublicMessageHandler;
import com.osuserverlist.packets.client.handlers.user.LogoutHandler;
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
        packetHandlers.put(ClientPackets.SEND_PUBLIC_MESSAGE, new SendPublicMessageHandler());
        packetHandlers.put(ClientPackets.SEND_PRIVATE_MESSAGE, new SendPrivateMessageHandler());
        packetHandlers.put(ClientPackets.CHANNEL_JOIN, new ChannelJoinPacket());
        packetHandlers.put(ClientPackets.CHANNEL_PART, new ChannelLeavePacket());
        packetHandlers.put(ClientPackets.LOGOUT, new LogoutHandler());
    }
}
