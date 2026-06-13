package com.osuserverlist.bjar.packets.client.handlers.chat;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;

import com.osuserverlist.bjar.models.essentials.Player;
import com.osuserverlist.bjar.modules.commands.BanchoCommandHandler;
import com.osuserverlist.bjar.modules.commands.BanchoCommandRegistry;
import com.osuserverlist.bjar.modules.commands.BanchoCommandRegistry.CommandInfo;
import com.osuserverlist.bjar.modules.logger.LoggerFactory;
import com.osuserverlist.bjar.packets.BanchoPacket;
import com.osuserverlist.bjar.packets.client.BanchoPacketHandler;
import com.osuserverlist.bjar.packets.client.BanchoPacketReader;
import com.osuserverlist.bjar.packets.client.ClientPackets;
import com.osuserverlist.bjar.packets.client.engine.ClientPacket;
import com.osuserverlist.bjar.packets.server.handlers.chat.SendMessagePacket;
import com.osuserverlist.bjar.server.Server;

@ClientPacket(ClientPackets.SEND_PRIVATE_MESSAGE)
public class SendPrivateMessagePacket implements BanchoPacketHandler {
    private final Logger logger = LoggerFactory.getLogger(SendPublicMessagePacket.class);

    @Override
    public boolean handle(BanchoPacket packet, BanchoPacketReader reader, Player player) throws IOException {
        reader.readString(); // senderName
        String message = reader.readString();
        String target = reader.readString();

        logger.info("Private Message from <{}>: <{}> to <{}>", player.getUsername(), message, target);

        Player targetPlayer = Server.getInstance().playerManager.getByFilter(p -> p.getUsername().equalsIgnoreCase(target));

        if (targetPlayer == null) {
            logger.warn("Target player not found for private message: " + target);
            return true;
        }

        targetPlayer.sendPacket(new SendMessagePacket(player.getUsername(), message, target, player.getId()));

        Pattern pattern = Pattern.compile("beatmapsets/(\\d+)#/(\\d+)");
        Matcher matcher = pattern.matcher(message);

        if(matcher.find()) {
            String beatmapId = matcher.group(2);
            String beatmapSetId = matcher.group(1);
            player.setLastNpBeatmapId(Long.parseLong(beatmapId));
            player.setLastNpBeatmapSetId(Long.parseLong(beatmapSetId));   
        }

        if (message.startsWith("!")) {
            String[] command = message.split(" ");
            String cmd = command[0].toLowerCase();

            CommandInfo commandInfo = BanchoCommandRegistry.getCommand(cmd);
            if(commandInfo == null) {
                return true;
            }

            if(player.getServerPrivileges() < commandInfo.requiredPrivileges) {
                return true;
            }

            String[] args = new String[command.length - 1];
            System.arraycopy(command, 1, args, 0, args.length);
            commandInfo.handler.handle(player, player.getUsername(), args);
        }

        return true;
    }

}
