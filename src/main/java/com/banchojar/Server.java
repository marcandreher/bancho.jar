package com.banchojar;

import java.util.HashMap;

import com.banchojar.packets.server.BanchoChannel;

public class Server {
    public static HashMap<String, Player> players = new HashMap<>();
    public static HashMap<String, BanchoChannel> channels = new HashMap<>();
}

enum PlayerState {
    CONNECTING,
    ONLINE,
    IDLE
}

enum LoginState {
    CONNECTING, 
    PRESENCE,
    LOGGED_IN,
}