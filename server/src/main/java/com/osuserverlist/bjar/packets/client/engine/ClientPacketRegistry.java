package com.osuserverlist.bjar.packets.client.engine;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;

import com.osuserverlist.bjar.modules.logger.LoggerFactory;
import com.osuserverlist.bjar.packets.client.BanchoPacketHandler;
import com.osuserverlist.bjar.packets.client.ClientPackets;

import io.github.classgraph.ClassGraph;

public class ClientPacketRegistry {
    private static final String HANDLER_PACKAGE = "com.osuserverlist.bjar.packets.client.handlers";
    private static final Logger logger = LoggerFactory.getLogger(ClientPacketRegistry.class);

    public static final Map<ClientPackets, BanchoPacketHandler> packetHandlers = new HashMap<>();

    public static void scanAndRegister() {
        try (var scan = new ClassGraph()
                .enableClassInfo()
                .enableAnnotationInfo()
                .acceptPackages(HANDLER_PACKAGE)
                .scan()) {

            int clientPacketCount = 0;

            for (var classInfo : scan.getClassesWithAnnotation(ClientPacket.class.getName())) {
                Class<?> handlerClass = classInfo.loadClass();
                ClientPacket annotation = handlerClass.getAnnotation(ClientPacket.class);
                ClientPackets packetType = annotation.value();

                try {
                    BanchoPacketHandler handler = (BanchoPacketHandler) handlerClass.getDeclaredConstructor().newInstance();
                    packetHandlers.put(packetType, handler);
                    clientPacketCount++;
                } catch (Exception e) {
                    logger.error("Failed to instantiate packet handler for {}", packetType, e);
                }
            }

            logger.debug("Registered {} client packet handlers", clientPacketCount);
        }
    }
}
