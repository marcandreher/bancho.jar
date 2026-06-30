package com.osuserverlist.bjar.modules.commands;

import java.lang.reflect.InvocationTargetException;
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
            scan.getClassesWithAnyAnnotation(BanchoCommand.class.getName()).forEach(classInfo -> {
                Class<?> handlerClass = classInfo.loadClass();
                BanchoCommand commandAnnotation = handlerClass.getAnnotation(BanchoCommand.class);
                if (commandAnnotation != null) {
                    String commandName = commandAnnotation.name();
                    CommandCategory category = commandAnnotation.category();
                    boolean isHidden = commandAnnotation.isHidden();
                    String description = commandAnnotation.description();
                    int requiredPrivileges = commandAnnotation.requiredPrivileges().getValue();
                    CommandInfo commandInfo;
                    try {
                        commandInfo = new CommandInfo(commandName, category, isHidden, description, requiredPrivileges, (BanchoCommandHandler) handlerClass.getDeclaredConstructor().newInstance());
                    } catch (InstantiationException | IllegalAccessException | IllegalArgumentException
                            | InvocationTargetException | NoSuchMethodException e) {
                        logger.error("Error occurred while instantiating command handler for command: {}", commandName, e);
                        return;
                    }
                    commandMap.put(commandName, commandInfo);
                }
            });
        }
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
