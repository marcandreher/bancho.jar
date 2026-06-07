package com.osuserverlist.bjar.packets.client;

import java.util.HashMap;
import java.util.Map;

import com.osuserverlist.bjar.packets.client.handlers.PingPacket;
import com.osuserverlist.bjar.packets.client.handlers.chat.ChannelJoinPacket;
import com.osuserverlist.bjar.packets.client.handlers.chat.ChannelLeavePacket;
import com.osuserverlist.bjar.packets.client.handlers.chat.SendPrivateMessagePacket;
import com.osuserverlist.bjar.packets.client.handlers.chat.SendPublicMessagePacket;
import com.osuserverlist.bjar.packets.client.handlers.friend.AddFriendPacket;
import com.osuserverlist.bjar.packets.client.handlers.friend.RemoveFriendPacket;
import com.osuserverlist.bjar.packets.client.handlers.user.LogoutPacket;
import com.osuserverlist.bjar.packets.client.handlers.user.PresenceRequestPacket;
import com.osuserverlist.bjar.packets.client.handlers.user.StatsRequestPacket;
import com.osuserverlist.bjar.packets.client.handlers.user.StatusUpdatePacket;
import com.osuserverlist.bjar.packets.client.handlers.user.UserSelfRequestStatusPacket;

public class ClientPacketRegistry {
    public static final Map<ClientPackets, BanchoPacketHandler> packetHandlers = new HashMap<>();

    static {
        packetHandlers.put(ClientPackets.PING, new PingPacket());
        packetHandlers.put(ClientPackets.USER_PRESENCE_REQUEST, new PresenceRequestPacket());
        packetHandlers.put(ClientPackets.CHANGE_ACTION, new StatusUpdatePacket());
        packetHandlers.put(ClientPackets.USER_STATS_REQUEST, new StatsRequestPacket());
        packetHandlers.put(ClientPackets.REQUEST_STATUS_UPDATE, new UserSelfRequestStatusPacket());
        packetHandlers.put(ClientPackets.SEND_PUBLIC_MESSAGE, new SendPublicMessagePacket());
        packetHandlers.put(ClientPackets.SEND_PRIVATE_MESSAGE, new SendPrivateMessagePacket());
        packetHandlers.put(ClientPackets.CHANNEL_JOIN, new ChannelJoinPacket());
        packetHandlers.put(ClientPackets.CHANNEL_PART, new ChannelLeavePacket());
        packetHandlers.put(ClientPackets.LOGOUT, new LogoutPacket());

        packetHandlers.put(ClientPackets.FRIEND_ADD, new AddFriendPacket());
        packetHandlers.put(ClientPackets.FRIEND_REMOVE, new RemoveFriendPacket());
    }
}
