package com.banchojar.packets.server;

public enum ServerPackets {
    LOGIN_REPLY(5),
    SEND_MESSAGE(7),
    PONG(8),
    HANDLE_IRC_CHANGE_USERNAME(9),
    HANDLE_IRC_QUIT(10),
    USER_STATS(11),
    USER_LOGOUT(12),
    SPECTATOR_JOINED(13),
    SPECTATOR_LEFT(14),
    SPECTATE_FRAMES(15),
    VERSION_UPDATE(19),
    SPECTATOR_CANT_SPECTATE(22),
    GET_ATTENTION(23),
    NOTIFICATION(24),
    UPDATE_MATCH(26),
    NEW_MATCH(27),
    DISPOSE_MATCH(28),
    TOGGLE_BLOCK_NON_FRIEND_DMS(34),
    MATCH_JOIN_SUCCESS(36),
    MATCH_JOIN_FAIL(37),
    FELLOW_SPECTATOR_JOINED(42),
    FELLOW_SPECTATOR_LEFT(43),
    ALL_PLAYERS_LOADED(45),
    MATCH_START(46),
    MATCH_SCORE_UPDATE(48),
    MATCH_TRANSFER_HOST(50),
    MATCH_ALL_PLAYERS_LOADED(53),
    MATCH_PLAYER_FAILED(57),
    MATCH_COMPLETE(58),
    MATCH_SKIP(61),
    UNAUTHORIZED(62),
    CHANNEL_JOIN_SUCCESS(64),
    CHANNEL_INFO(65),
    CHANNEL_KICK(66),
    CHANNEL_AUTO_JOIN(67),
    BEATMAP_INFO_REPLY(69),
    PRIVILEGES(71),
    FRIENDS_LIST(72),
    PROTOCOL_VERSION(75),
    MAIN_MENU_ICON(76),
    MONITOR(80),
    MATCH_PLAYER_SKIPPED(81),
    USER_PRESENCE(83),
    RESTART(86),
    MATCH_INVITE(88),
    CHANNEL_INFO_END(89),
    MATCH_CHANGE_PASSWORD(91),
    SILENCE_END(92),
    USER_SILENCED(94),
    USER_PRESENCE_SINGLE(95),
    USER_PRESENCE_BUNDLE(96),
    USER_DM_BLOCKED(100),
    TARGET_IS_SILENCED(101),
    VERSION_UPDATE_FORCED(102),
    SWITCH_SERVER(103),
    ACCOUNT_RESTRICTED(104),
    RTX(105),
    MATCH_ABORT(106),
    SWITCH_TOURNAMENT_SERVER(107);

    private final short value;

    ServerPackets(int value) {
        this.value = (short) value;
    }

    public short getValue() {
        return value;
    }

    
    public static String getNameById(int id) {
        for (ServerPackets packet : ServerPackets.values()) {
            if (packet.getValue() == id) {
                return packet.name();
            }
        }
        return "UNKNOWN_PACKET";  // Return a default value if not found
    }
}