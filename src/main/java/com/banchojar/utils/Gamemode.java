package com.banchojar.utils;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public enum Gamemode {
    Standard(0, "Osu!"),
    Taiko(1, "Osu!Taiko"),
    Catch(2, "Osu!Ctb"),
    Mania(3, "Osu!Mania");
 
    private final int id;
    private final String displayName;
 
    private Gamemode(int id, String displayName) {
       this.id = id;
       this.displayName = displayName;
    }
 
    public int getId() {
       return this.id;
    }
 
    public String getDisplayName() {
       return this.displayName;
    }
 
    public static Gamemode getGamemode(String gamemode) {
       String gm = gamemode.toLowerCase();
       Map<String, Gamemode> map = new HashMap<String, Gamemode>();
       map.put("osu", Standard);
       map.put("osu!ctb", Catch);
       map.put("osuctb", Catch);
       map.put("osumania", Mania);
       map.put("osutaiko", Taiko);
       map.put("osu!", Standard);
       map.put("osu!catch", Catch);
       map.put("osu!mania", Mania);
       map.put("osu!taiko", Taiko);
       map.put("padrão", Standard);
       map.put("default", Standard);
       map.put("standard", Standard);
       map.put("catch", Catch);
       map.put("mania", Mania);
       map.put("taiko", Taiko);
       return map.containsKey(gm) ? (Gamemode)map.get(gamemode) : null;
    }
 
    public static Gamemode getById(int id) {
       return (Gamemode)Arrays.stream(values()).filter((g) -> {
          return g.id == id;
       }).findFirst().orElse((Gamemode)null);
    }
 }
 