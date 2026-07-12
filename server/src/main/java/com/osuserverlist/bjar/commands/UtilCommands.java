package com.osuserverlist.bjar.commands;

import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.lang.management.ThreadMXBean;
import java.time.Duration;

import com.osuserverlist.bjar.models.essentials.Player;
import com.osuserverlist.bjar.models.osu.Privileges;
import com.osuserverlist.bjar.modules.commands.BanchoCommand;
import com.osuserverlist.bjar.modules.commands.BanchoCommandHandler;
import com.osuserverlist.bjar.modules.commands.BanchoCommandProcessor.PlayerCommandInfo;
import com.osuserverlist.bjar.modules.commands.CommandCategory;
import com.osuserverlist.bjar.modules.logger.BuildInfo;
import com.osuserverlist.bjar.packets.server.handlers.util.NotificationPacket;

public class UtilCommands extends BanchoCommandHandler {

    private static final long MB = 1024L * 1024L;
    
    @BanchoCommand(
        name = "!alert", 
        category = CommandCategory.MISC, 
        description = "Alert all players with a message", 
        requiredPrivileges = Privileges.ADMINISTRATOR
    )
    public void alert(Player sender, PlayerCommandInfo[] commandInfos, String[] args) {
        if(args.length == 0) {
            sendBotMessage(commandInfos, "Usage: !alert <message>");
            return;
        }

        String message = String.join(" ", args);

        server.playerManager.getAll().forEach(player -> {
            player.sendPacket(new NotificationPacket(message));
        });

        logger.info("Alert sent by {}: {}", sender.toString(), message);

        sendBotMessage(commandInfos, "Alert sent to all players: " + message);
    }

    @BanchoCommand(
        name = "!server", 
        category = CommandCategory.MISC, 
        description = "Shows server information", 
        requiredPrivileges = Privileges.ADMINISTRATOR
    )
    public void serverInfo(Player sender, PlayerCommandInfo[] commandInfos, String[] args) {
        sendBotMessage(commandInfos, "Running bancho.jar <v" + BuildInfo.VERSION + "> built on (" + BuildInfo.BUILD_TIME + ")");

        // Get App RAM Info and usage
        Runtime runtime = Runtime.getRuntime();
        long maxMemory = runtime.maxMemory();
        long totalMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();
        long usedMemory = totalMemory - freeMemory;

        // Runtime / uptime info
        RuntimeMXBean runtimeBean = ManagementFactory.getRuntimeMXBean();
        long uptimeMillis = runtimeBean.getUptime();
        Duration uptime = Duration.ofMillis(uptimeMillis);
        String uptimeFormatted = String.format("%dd %dh %dm %ds",
                uptime.toDays(),
                uptime.toHoursPart(),
                uptime.toMinutesPart(),
                uptime.toSecondsPart());

        // Thread info
        ThreadMXBean threadBean = ManagementFactory.getThreadMXBean();
        int threadCount = threadBean.getThreadCount();
        int peakThreadCount = threadBean.getPeakThreadCount();

        // CPU / JVM info
        int availableProcessors = runtime.availableProcessors();
        String javaVersion = System.getProperty("java.version");
        String javaVendor = System.getProperty("java.vendor");
        String osName = System.getProperty("os.name");
        String osArch = System.getProperty("os.arch");

        sendBotMessage(commandInfos, String.format(
                "Memory: %dMB / %dMB used (Max: %dMB)",
                usedMemory / MB, totalMemory / MB, maxMemory / MB
        ));
        sendBotMessage(commandInfos, "Uptime: " + uptimeFormatted);
        sendBotMessage(commandInfos, String.format(
                "Threads: %d active (peak: %d)", threadCount, peakThreadCount
        ));
        sendBotMessage(commandInfos, String.format(
                "JVM: %s (%s) | OS: %s (%s) | Cores: %d",
                javaVersion, javaVendor, osName, osArch, availableProcessors
        ));
    }

}
