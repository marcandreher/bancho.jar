package com.osuserverlist.bjar.packets.client.engine;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.osuserverlist.bjar.packets.client.ClientPackets;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface ClientPacket {
    ClientPackets value();
}
