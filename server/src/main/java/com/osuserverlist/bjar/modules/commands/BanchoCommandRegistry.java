package com.osuserverlist.bjar.modules.commands;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;

import com.osuserverlist.bjar.modules.logger.LoggerFactory;

import io.github.classgraph.ClassGraph;
import lombok.AllArgsConstructor;

public class BanchoCommandRegistry {

    private static Map<String, CommandInfo> commandMap = new HashMap<>();
    private static final Logger logger = LoggerFactory.getLogger(BanchoCommandRegistry.class);

    public static void registerAnnotatedHandlers(String packageName) {
        try (var scan = new ClassGraph()
                .enableClassInfo()
                .enableAnnotationInfo()
                .acceptPackages(packageName)
                .scan()) {
            scan.getAllClasses().forEach(classInfo -> {
                Class<?> handlerClass = classInfo.loadClass();

                BanchoCommand classAnnotation = handlerClass.getAnnotation(BanchoCommand.class);
                boolean hasMethodCommands = false;

                for (Method method : handlerClass.getDeclaredMethods()) {
                    if (method.isAnnotationPresent(BanchoCommand.class)) {
                        hasMethodCommands = true;
                        break;
                    }
                }

                Object handlerInstance = null;

                if (classAnnotation != null || hasMethodCommands) {
                    try {
                        handlerInstance = handlerClass.getDeclaredConstructor().newInstance();
                    } catch (InstantiationException | IllegalAccessException | IllegalArgumentException
                            | InvocationTargetException | NoSuchMethodException e) {
                        logger.error("Error occurred while instantiating command handler for class: {}", handlerClass.getName(), e);
                        return;
                    }
                }

                if (classAnnotation != null) {
                    registerCommand(classAnnotation, (BanchoCommandHandler) handlerInstance);
                }

                for (Method method : handlerClass.getDeclaredMethods()) {
                    BanchoCommand methodAnnotation = method.getAnnotation(BanchoCommand.class);

                    if (methodAnnotation == null) {
                        continue;
                    }

                    if (!isValidCommandMethod(method)) {
                        logger.error(
                                "Ignoring command method {}#{} because it does not match the expected signature",
                                handlerClass.getName(),
                                method.getName()
                        );
                        continue;
                    }

                    registerCommand(methodAnnotation, createMethodHandler(handlerInstance, method));
                }
            });
        }
    }

    private static void registerCommand(BanchoCommand commandAnnotation, BanchoCommandHandler handler) {
        String commandName = commandAnnotation.name();

        CommandInfo commandInfo = new CommandInfo(
                commandName,
                commandAnnotation.category(),
                commandAnnotation.isHidden(),
                commandAnnotation.description(),
                commandAnnotation.requiredPrivileges().getValue(),
                handler
        );

        commandMap.put(commandName, commandInfo);
    }

    private static BanchoCommandHandler createMethodHandler(Object handlerInstance, Method method) {
        method.setAccessible(true);

        return new BanchoCommandHandler() {
            @Override
            public void handle(
                    com.osuserverlist.bjar.models.essentials.Player sender,
                    BanchoCommandProcessor.PlayerCommandInfo[] commandInfos,
                    String[] args
            ) {
                try {
                    Object target = Modifier.isStatic(method.getModifiers()) ? null : handlerInstance;
                    method.invoke(target, sender, commandInfos, args);
                } catch (IllegalAccessException | InvocationTargetException e) {
                    logger.error(
                            "Error occurred while invoking command method {}#{}",
                            method.getDeclaringClass().getName(),
                            method.getName(),
                            e
                    );
                }
            }
        };
    }

    private static boolean isValidCommandMethod(Method method) {
        return method.getParameterCount() == 3
                && method.getParameterTypes()[0] == com.osuserverlist.bjar.models.essentials.Player.class
                && method.getParameterTypes()[1] == BanchoCommandProcessor.PlayerCommandInfo[].class
                && method.getParameterTypes()[2] == String[].class;
    }

    public static void finalizeCommandRegistration() {
        logger.info("Registered <{}> commands", commandMap.size());
    }

    public static Collection<CommandInfo> getAllCommands() {
        return commandMap.values();
    }

    public static CommandInfo getCommand(String name) {
        return commandMap.get(name);
    }

    @AllArgsConstructor
    public static class CommandInfo {
        public String name;
        public CommandCategory category;
        public boolean isHidden;
        public String description;
        public int requiredPrivileges;
        public BanchoCommandHandler handler;
    }

}
