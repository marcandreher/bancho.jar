package com.osuserverlist.bjar.modules;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.invoke.CallSite;
import java.lang.invoke.LambdaMetafactory;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.EnumMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.osuserverlist.bjar.models.essentials.Player;
import com.osuserverlist.bjar.packets.BanchoPacket;

import io.github.classgraph.ClassGraph;
import lombok.Value;

public class ClientPacketEngine {

    private static final String HANDLER_PACKAGE = "com.osuserverlist.bjar.packets.client";

    public static final EnumMap<ClientPackets, BanchoPacketHandler> packetHandlers = new EnumMap<>(ClientPackets.class);
    public static final EnumMap<ClientPackets, ClientMetadata> packetMetadata = new EnumMap<>(ClientPackets.class);

    public static interface BanchoPacketHandler {
        public static final Logger logger = LoggerFactory.getLogger(BanchoPacketHandler.class);

        boolean handle(BanchoPacket packet, BanchoPacketReader reader, Player player);
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target({ ElementType.METHOD })
    public static @interface ClientPacket {
        ClientPackets value();
    }

    public static enum ClientPackets {
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
        TOURNAMENT_LEAVE_MATCH_CHANNEL(109),
        UNHANDLED_PACKET(255); // This is a special case for packets that don't have a specific handler

        public final int value;

        ClientPackets(int value) {
            this.value = value;
        }

        private static final ClientPackets[] LOOKUP = new ClientPackets[256];

        static {
            for (ClientPackets p : values())
                if (p.value >= 0)
                    LOOKUP[p.value] = p;
        }

        public static ClientPackets getById(int id) {
            return (id >= 0 && id < LOOKUP.length)
                    ? LOOKUP[id]
                    : UNKNOWN_PACKET;
        }
    }

    public static class ClientPacketRegistry {

        private static final Logger logger = LoggerFactory.getLogger(ClientPacketRegistry.class);

        private static final MethodType HANDLER_METHOD_TYPE = MethodType.methodType(
                boolean.class, BanchoPacket.class, BanchoPacketReader.class, Player.class);

        public static void scanAndRegister() {
            packetHandlers.clear();
            packetMetadata.clear();

            MethodHandles.Lookup lookup = MethodHandles.lookup();

            try (var scan = new ClassGraph()
                    .enableClassInfo()
                    .enableMethodInfo()
                    .enableAnnotationInfo()
                    .acceptPackages(HANDLER_PACKAGE)
                    .scan()) {

                int clientPacketCount = 0;

                for (var classInfo : scan.getAllClasses()) {
                    Class<?> clazz = classInfo.loadClass();

                    Object instance = null;

                    for (Method method : clazz.getDeclaredMethods()) {
                        ClientPacket methodAnnotation = method.getAnnotation(ClientPacket.class);

                        if (methodAnnotation == null) {
                            continue;
                        }

                        try {
                            method.setAccessible(true);

                            MethodHandle handle = lookup.unreflect(method);

                            boolean isStatic = Modifier.isStatic(method.getModifiers());

                            if (!isStatic && instance == null) {
                                instance = clazz.getDeclaredConstructor().newInstance();
                            }

                            BanchoPacketHandler handler = createHandler(lookup, handle, isStatic, instance);

                            register(methodAnnotation.value(), handler);
                            packetMetadata.put(methodAnnotation.value(),
                                    new ClientMetadata(clazz.getSimpleName(), method.getName()));

                            clientPacketCount++;

                        } catch (Throwable t) {
                            logger.error(
                                    "Failed to register packet handler {}#{}",
                                    clazz.getName(),
                                    method.getName(),
                                    t);
                        }
                    }
                }

                logger.debug("Registered <{}> client packet handlers", clientPacketCount);
            }
        }

        private static BanchoPacketHandler createHandler(
                MethodHandles.Lookup lookup, MethodHandle target, boolean isStatic, Object instance) throws Throwable {

            MethodType factoryType = isStatic
                    ? MethodType.methodType(BanchoPacketHandler.class)
                    : MethodType.methodType(BanchoPacketHandler.class, target.type().parameterType(0));

            CallSite site = LambdaMetafactory.metafactory(
                    lookup,
                    "handle", // BanchoPacketHandler's single abstract method
                    factoryType,
                    HANDLER_METHOD_TYPE,
                    target,
                    HANDLER_METHOD_TYPE);

            MethodHandle factory = site.getTarget();

            return isStatic
                    ? (BanchoPacketHandler) factory.invoke()
                    : (BanchoPacketHandler) factory.invoke(instance);
        }

        private static void register(ClientPackets packet, BanchoPacketHandler handler) {
            BanchoPacketHandler previous = packetHandlers.put(packet, handler);

            if (previous != null) {
                logger.warn("Duplicate handler registered for {}, overriding previous handler.", packet);
            }
        }
    }

    @Value
    public static class ClientMetadata {
        private final String handlerClassName;
        private final String handlerMethodName;
    }
}
