package com.banchojar.packets.server;

import java.util.ArrayList;
import java.util.List;

import com.banchojar.Player;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class BanchoChannel {
    private String id;
    private String name;
    private String description;
    private boolean autoJoin;
    private final List<Player> players = new ArrayList<>();
    
}
