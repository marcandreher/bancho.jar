package com.banchojar.packets.client.handlers.channels;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.banchojar.App;
import com.banchojar.Player;
import com.banchojar.commands.AbstractBanchoCommandHandler;
import com.banchojar.handlers.bancho.BanchoHandler;
import com.banchojar.packets.BanchoPacket;
import com.banchojar.packets.client.BanchoPacketHandler;
import com.banchojar.packets.client.BanchoPacketReader;
import com.banchojar.packets.server.handlers.SendMessageHandler;

public class SendPublicMessageHandler implements BanchoPacketHandler {
    @Override
    public boolean handle(BanchoPacket packet, BanchoPacketReader reader, Player player) throws IOException {
        reader.readString(); // senderName
        String message = reader.readString();
        String target = reader.readString();
        App.logger.info("[BANCHO] Message from <{}>: <{}> to <{}>", player.getUsername(), message, target);

        player.addPacketToStack(new SendMessageHandler("BanchoBot", message, target, 1));

        Pattern pattern = Pattern.compile("beatmapsets/(\\d+)#/(\\d+)");
        Matcher matcher = pattern.matcher(message);

        if (matcher.find()) {
            int secondId = Integer.parseInt(matcher.group(2));
            player.setLastNpBeatmapId(secondId);
        }

        if (message.startsWith("!")) {
            String[] command = message.split(" ");
            String cmd = command[0].toLowerCase();

            for (AbstractBanchoCommandHandler handler : BanchoHandler.commandHandlers) {
                if(handler.commandName().equals(cmd)) {
                    String response = handler.handle(player, command);
                    player.addPacketToStack(new SendMessageHandler("BanchoBot", response, target, 1));
                    return true;
                }
            }
            
        }

        return true;
    }
}
