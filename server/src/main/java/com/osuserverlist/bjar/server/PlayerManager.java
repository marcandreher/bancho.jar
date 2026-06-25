package com.osuserverlist.bjar.server;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;

import com.osuserverlist.bjar.models.essentials.BanchoChannel;
import com.osuserverlist.bjar.models.essentials.ModeStats;
import com.osuserverlist.bjar.models.essentials.Player;
import com.osuserverlist.bjar.models.osu.Privileges;
import com.osuserverlist.bjar.modules.database.MySQL;
import com.osuserverlist.bjar.packets.server.handlers.user.UserQuitPacket;

public class PlayerManager {
    private final Map<String, Player> onlinePlayers = new ConcurrentHashMap<>();
    private final Map<String, Player> apiIdentMap = new ConcurrentHashMap<>();

    public void add(Player player) {
        onlinePlayers.put(player.getOsuToken(), player);
        apiIdentMap.put(player.getApiIdent(), player);
    }

    public Player get(String osuToken) {
        return onlinePlayers.get(osuToken);
    }

    public Player getByApiIdent(String apiIdent) {
        return apiIdentMap.get(apiIdent);
    }

    public Player getByFilter(Predicate<Player> filter) {
        return onlinePlayers.values().stream().filter(filter).findFirst().orElse(null);
    }

    public Player getById(int id) {
        return getByFilter(p -> p.getId() == id);
    }

    public Collection<Player> getAll() {
        return onlinePlayers.values();
    }

    public void forceRemove(Player player) {
        onlinePlayers.remove(player.getOsuToken());
        apiIdentMap.remove(player.getApiIdent());
    }

    public void disconnect(Player player) {
        for (BanchoChannel channel : Server.getInstance().channelManager.getAll()) {
            if (channel.getPlayers().contains(player)) {
                Server.getInstance().channelManager.leaveChannel(channel.getName(), player);
            }
        }

        for(Player p : onlinePlayers.values()) {
            if(p.equals(player)) continue;
            p.sendPacket(new UserQuitPacket(player.getId()));
        }

        onlinePlayers.remove(player.getOsuToken());
        apiIdentMap.remove(player.getApiIdent());
    }

    public void addPriv(Player player, Privileges priv) {
        player.setServerPrivileges(player.getServerPrivileges() | priv.getValue());
        disconnect(player);
    }

    public void removePriv(Player player, Privileges priv) {
        player.setServerPrivileges(player.getServerPrivileges() & ~priv.getValue());
        disconnect(player);
    }

    public Player getBotPlayer(MySQL mysql, int id) throws SQLException {
        ResultSet botRs = mysql.query("SELECT * FROM `users` WHERE `id` = ?", id).executeQuery();

        if (!botRs.next()) {
            return null;
        }

        Player botPlayer = new Player(id, true, UUID.randomUUID().toString());
        botPlayer.setUsername(botRs.getString("name"));
        botPlayer.setCountry((short) 245); // satellite provider
 
        for (int i = 0; i <= 8; i++) {
            ModeStats modeStats = new ModeStats();
            botPlayer.getModeStats()[i] = modeStats;
        }

        return botPlayer;
    }
}
