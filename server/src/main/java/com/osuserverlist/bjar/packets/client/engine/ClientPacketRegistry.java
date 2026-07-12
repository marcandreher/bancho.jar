package com.osuserverlist.bjar.packets.client.engine;

import java.lang.invoke.CallSite;
import java.lang.invoke.LambdaMetafactory;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;

import com.osuserverlist.bjar.modules.logger.LoggerFactory;
import com.osuserverlist.bjar.packets.BanchoPacket;
import com.osuserverlist.bjar.models.essentials.Player;

import io.github.classgraph.ClassGraph;
import lombok.AllArgsConstructor;
import lombok.Data;

public class ClientPacketRegistry {

    private static final String HANDLER_PACKAGE = "com.osuserverlist.bjar.packets.client.handlers";
    private static final Logger logger = LoggerFactory.getLogger(ClientPacketRegistry.class);

    // The functional method on BanchoPacketHandler: boolean handle(BanchoPacket, BanchoPacketReader, Player)
    private static final MethodType HANDLER_METHOD_TYPE = MethodType.methodType(
            boolean.class, BanchoPacket.class, BanchoPacketReader.class, Player.class);

    public static final Map<ClientPackets, BanchoPacketHandler> packetHandlers = new HashMap<>();
    public static final Map<ClientPackets, ClientMetadata> packetMetadata = new HashMap<>();

    public static void scanAndRegister() {
        packetHandlers.clear();

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

                // ------------------------------------------------------------------
                // Class-based handlers
                // ------------------------------------------------------------------
                ClientPacket classAnnotation = clazz.getAnnotation(ClientPacket.class);
                if (classAnnotation != null) {
                    try {
                        if (!BanchoPacketHandler.class.isAssignableFrom(clazz)) {
                            logger.error(
                                    "{} is annotated with @ClientPacket but does not implement BanchoPacketHandler",
                                    clazz.getName());
                        } else {
                            BanchoPacketHandler handler =
                                    (BanchoPacketHandler) clazz.getDeclaredConstructor().newInstance();

                            register(classAnnotation.value(), handler);
                            packetMetadata.put(classAnnotation.value(), new ClientMetadata(clazz.getSimpleName(), null));
                            clientPacketCount++;
                        }
                    } catch (Exception e) {
                        logger.error("Failed to instantiate packet handler {}", clazz.getName(), e);
                    }
                }

                // ------------------------------------------------------------------
                // Method-based handlers
                // ------------------------------------------------------------------
                for (Method method : clazz.getDeclaredMethods()) {
                    ClientPacket methodAnnotation = method.getAnnotation(ClientPacket.class);

                    if (methodAnnotation == null) {
                        continue;
                    }

                    try {
                        validateMethod(method);

                        method.setAccessible(true);

                        MethodHandle handle = lookup.unreflect(method);

                        boolean isStatic = Modifier.isStatic(method.getModifiers());

                        if (!isStatic && instance == null) {
                            instance = clazz.getDeclaredConstructor().newInstance();
                        }

                        BanchoPacketHandler handler = createHandler(lookup, handle, isStatic, instance);

                        register(methodAnnotation.value(), handler);
                        packetMetadata.put(methodAnnotation.value(), new ClientMetadata(clazz.getSimpleName(), method.getName()));

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
                "handle",                                     // BanchoPacketHandler's single abstract method
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

    private static void validateMethod(Method method) {
        if (method.getReturnType() != boolean.class) {
            throw new IllegalArgumentException("Packet handler methods must return boolean.");
        }

        Class<?>[] params = method.getParameterTypes();

        if (params.length != 3
                || params[0] != com.osuserverlist.bjar.packets.BanchoPacket.class
                || params[1] != com.osuserverlist.bjar.packets.client.engine.BanchoPacketReader.class
                || params[2] != com.osuserverlist.bjar.models.essentials.Player.class) {

            throw new IllegalArgumentException(
                    "Packet handler methods must have signature:\n"
                            + "boolean method(BanchoPacket, BanchoPacketReader, Player)");
        }
    }

    @AllArgsConstructor
    @Data
    public static class ClientMetadata {
        private final String handlerClassName;
        private final String handlerMethodName;
    }
}