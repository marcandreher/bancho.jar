package com.osuserverlist.bjar.server;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;

import com.osuserverlist.bjar.App;
import com.osuserverlist.bjar.Server;
import com.osuserverlist.bjar.models.essentials.Channel;
import com.osuserverlist.bjar.models.essentials.Match;
import com.osuserverlist.bjar.models.essentials.ModeStats;
import com.osuserverlist.bjar.models.essentials.Player;
import com.osuserverlist.bjar.models.osu.Privileges;
import com.osuserverlist.bjar.modules.datastore.MySQL;
import com.osuserverlist.bjar.packets.server.LoginServerPackets.SilenceInfoPacket;
import com.osuserverlist.bjar.packets.server.UserServerPackets.UserQuitPacket;

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
    
    public Player getByUsername(String username) {
        return getByFilter(p -> p.getUsername().equalsIgnoreCase(username));
    }

    public Collection<Player> getAll() {
        return onlinePlayers.values();
    }

    public void disconnect(Player player) {
        Server server = App.server;
        for (Channel channel : server.channelManager.getAll()) {
            if (channel.getPlayers().contains(player)) {
                server.channelManager.leaveChannel(channel.getName(), player);
            }
        }

        Match match = player.getMatch();
        if(match != null) {
            server.matchManager.leaveMatch(match, player);
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

    public void restrict(Player player) {
        player.setServerPrivileges(player.getServerPrivileges() & ~Privileges.UNRESTRICTED.getValue());
        disconnect(player);
    }

    public void unrestrict(Player player) {
        player.setServerPrivileges(player.getServerPrivileges() | Privileges.UNRESTRICTED.getValue());
        disconnect(player);
    }

    public void silence(Player player, int silenceEnd) {
        int silenceSecondsRemaining = (int) (silenceEnd - (System.currentTimeMillis() / 1000L));
        player.setSilenceEnd(silenceEnd);
        player.sendPacket(new SilenceInfoPacket(silenceSecondsRemaining));
    }

    public void unsilence(Player player) {
        player.setSilenceEnd(0);
        player.sendPacket(new SilenceInfoPacket(0));
    }

    public Player getBotPlayer(MySQL mysql, int id) throws SQLException {
        ResultSet botRs = mysql.query("SELECT * FROM `users` WHERE `id` = ?", id).executeQuery();

        if (!botRs.next()) {
            return null;
        }

        // TODO: Make this more dynamic enabling mutliple bots

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
