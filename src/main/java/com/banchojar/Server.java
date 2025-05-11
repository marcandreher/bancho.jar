package com.banchojar;

import java.util.HashMap;

import org.jooq.DSLContext;

import com.banchojar.App.Config;
import com.banchojar.App.MetricsConfig;
import com.banchojar.db.provider.Provider;
import com.banchojar.geo.GeoLocProvider;
import com.banchojar.packets.server.BanchoChannel;

import io.prometheus.client.exporter.HTTPServer;
import redis.clients.jedis.JedisPooled;

public class Server {

    public static final HashMap<String, Player> players = new HashMap<>();
    public static final HashMap<String, BanchoChannel> channels = new HashMap<>();
    public static Config config = new Config();
    public static MetricsConfig metricsConfig = new MetricsConfig();

    public static Provider provider;
    public static GeoLocProvider geoProvider;

    public static DSLContext dsl;
    public static JedisPooled redis;

    public static HTTPServer prometheusServer;
    
    public static enum PlayerState {
        CONNECTING,
        ONLINE,
        IDLE
    }
    
    public static enum LoginState {
        CONNECTING, 
        PRESENCE,
        LOGGED_IN,
    }
}

