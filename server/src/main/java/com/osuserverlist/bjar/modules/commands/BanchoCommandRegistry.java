package com.osuserverlist.bjar.modules.commands;

import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

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
            AtomicInteger count = new AtomicInteger();
            scan.getClassesWithAnyAnnotation(BanchoCommand.class.getName()).forEach(classInfo -> {
                Class<?> handlerClass = classInfo.loadClass();
                BanchoCommand commandAnnotation = handlerClass.getAnnotation(BanchoCommand.class);
                if (commandAnnotation != null) {
                    String commandName = commandAnnotation.name();
                    String description = commandAnnotation.description();
                    int requiredPrivileges = commandAnnotation.requiredPrivileges().getValue();
                    CommandInfo commandInfo;
                    try {
                        commandInfo = new CommandInfo(commandName, description, requiredPrivileges, (BanchoCommandHandler) handlerClass.getDeclaredConstructor().newInstance());
                    } catch (InstantiationException | IllegalAccessException | IllegalArgumentException
                            | InvocationTargetException | NoSuchMethodException e) {
                        logger.error("Error occurred while instantiating command handler for command: {}", commandName, e);
                        return;
                    }
                    commandMap.put(commandName, commandInfo);
                    count.incrementAndGet();
                }
            });
            logger.debug("Registered <{}> bancho commands from package: ({})", count.get(), packageName);
        }
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
        public String description;
        public int requiredPrivileges;
        public BanchoCommandHandler handler;
    }

}
