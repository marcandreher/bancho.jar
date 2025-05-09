package com.banchojar.packets.client;

public enum ClientPackets {
    UNKNOWN_PACKET(-1),
    CHANGE_ACTION(0),
    SEND_PUBLIC_MESSAGE(1),
    LOGOUT(2),
    REQUEST_STATUS_UPDATE(3),
    PING(4),
    START_SPECTATING(16),
    STOP_SPECTATING(17),
    SPECTATE_FRAMES(18),
    ERROR_REPORT(20),
    CANT_SPECTATE(21),
    SEND_PRIVATE_MESSAGE(25),
    PART_LOBBY(29),
    JOIN_LOBBY(30),
    CREATE_MATCH(31),
    JOIN_MATCH(32),
    PART_MATCH(33),
    MATCH_CHANGE_SLOT(38),
    MATCH_READY(39),
    MATCH_LOCK(40),
    MATCH_CHANGE_SETTINGS(41),
    MATCH_START(44),
    MATCH_SCORE_UPDATE(47),
    MATCH_COMPLETE(49),
    MATCH_CHANGE_MODS(51),
    MATCH_LOAD_COMPLETE(52),
    MATCH_NO_BEATMAP(54),
    MATCH_NOT_READY(55),
    MATCH_FAILED(56),
    MATCH_HAS_BEATMAP(59),
    MATCH_SKIP_REQUEST(60),
    CHANNEL_JOIN(63),
    BEATMAP_INFO_REQUEST(68),
    MATCH_TRANSFER_HOST(70),
    FRIEND_ADD(73),
    FRIEND_REMOVE(74),
    MATCH_CHANGE_TEAM(77),
    CHANNEL_PART(78),
    RECEIVE_UPDATES(79),
    SET_AWAY_MESSAGE(82),
    IRC_ONLY(84),
    USER_STATS_REQUEST(85),
    MATCH_INVITE(87),
    MATCH_CHANGE_PASSWORD(90),
    TOURNAMENT_MATCH_INFO_REQUEST(93),
    USER_PRESENCE_REQUEST(97),
    USER_PRESENCE_REQUEST_ALL(98),
    TOGGLE_BLOCK_NON_FRIEND_DMS(99),
    TOURNAMENT_JOIN_MATCH_CHANNEL(108),
    TOURNAMENT_LEAVE_MATCH_CHANNEL(109);

    private final int value;

    ClientPackets(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static String getNameById(int id) {
        for (ClientPackets packet : ClientPackets.values()) {
            if (packet.getValue() == id) {
                return packet.name();
            }
        }
        return "UNKNOWN_PACKET";  // Return a default value if not found
    }

    public static ClientPackets getById(int id) {
        for (ClientPackets packet : ClientPackets.values()) {
            if (packet.getValue() == id) {
                return packet;
            }
        }
        return UNKNOWN_PACKET;  // Return a default value if not found
    }
}