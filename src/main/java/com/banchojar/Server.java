package com.banchojar;

import java.util.HashMap;

import lombok.Data;

public class Server {
    public static HashMap<String, Player> players = new HashMap<>();
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