package com.banchojar.handlers.bancho;

import java.util.Set;

import org.jetbrains.annotations.NotNull;

import com.banchojar.Server;
import com.banchojar.packets.client.ClientPackets;
import com.banchojar.utils.VersionInfo;

import io.javalin.http.Context;
import io.javalin.http.Handler;

public class MainPageHandler implements Handler {

    @Override
    public void handle(@NotNull Context ctx) throws Exception {
        // Get server status information
        int onlinePlayers = Server.players.size();
        int channels = Server.channels.size();
        String version = VersionInfo.getVersion();
        String build = VersionInfo.getBuildTimestamp();

        // Generate packet list
        Set<ClientPackets> packets = BanchoHandler.packetHandlers.keySet();
        StringBuilder packetList = new StringBuilder();
        for (ClientPackets packet : packets) {
            packetList.append("<span class='packet'>")
                    .append(packet.name())
                    .append(" (")
                    .append(packet.getValue())
                    .append(")</span> ");
        }

        // Set response headers
        ctx.header("Cache-Control", "no-store");
        ctx.contentType("text/html");
        
        // Return the HTML template with server data
        ctx.result(getHtmlTemplate(version, build, onlinePlayers, channels, packetList.toString()));
    }
    
    /**
     * Returns the HTML template with server data inserted
     */
    private String getHtmlTemplate(String version, String build, int onlinePlayers, int channels, String packetList) {
        return "<!DOCTYPE html>\n" +
            "<html lang=\"en\">\n" +
            "<head>\n" +
            "    <meta charset=\"UTF-8\">\n" +
            "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n" +
            "    <title>Bancho.jar Server</title>\n" +
            "    <style>\n" +
            "        body {\n" +
            "            background-color: #0c0c0c;\n" +
            "            color: #0f0;\n" +
            "            font-family: 'Courier New', Courier, monospace;\n" +
            "            margin: 0;\n" +
            "            padding: 20px;\n" +
            "            line-height: 1.4;\n" +
            "        }\n" +
            "        .terminal {\n" +
            "            background-color: #000;\n" +
            "            border-radius: 8px;\n" +
            "            padding: 20px;\n" +
            "            box-shadow: 0 0 10px rgba(0, 255, 0, 0.2);\n" +
            "            max-width: 800px;\n" +
            "            margin: 0 auto;\n" +
            "            border: 1px solid #1a1a1a;\n" +
            "            overflow: hidden;\n" +
            "        }\n" +
            "        .terminal-header {\n" +
            "            display: flex;\n" +
            "            margin-bottom: 15px;\n" +
            "            border-bottom: 1px solid #333;\n" +
            "            padding-bottom: 10px;\n" +
            "        }\n" +
            "        .title {\n" +
            "            font-size: 24px;\n" +
            "            font-weight: bold;\n" +
            "            margin: 0;\n" +
            "            color: #fff;\n" +
            "            text-shadow: 0 0 5px #0f0;\n" +
            "        }\n" +
            "        .content {\n" +
            "            font-size: 14px;\n" +
            "        }\n" +
            "        .prompt {\n" +
            "            color: #ff5f5f;\n" +
            "            font-weight: bold;\n" +
            "        }\n" +
            "        .command {\n" +
            "            color: #5f5fff;\n" +
            "        }\n" +
            "        .output {\n" +
            "            color: #0f0;\n" +
            "            margin-bottom: 15px;\n" +
            "        }\n" +
            "        .section {\n" +
            "            margin-bottom: 20px;\n" +
            "            padding-bottom: 10px;\n" +
            "            border-bottom: 1px dashed #333;\n" +
            "        }\n" +
            "        .stat-label {\n" +
            "            color: #ff5f5f;\n" +
            "            font-weight: bold;\n" +
            "            display: inline-block;\n" +
            "            width: 150px;\n" +
            "        }\n" +
            "        .stat-value {\n" +
            "            color: #fff;\n" +
            "        }\n" +
            "        .packets-container {\n" +
            "            max-height: 200px;\n" +
            "            overflow-y: auto;\n" +
            "            background-color: #0a0a0a;\n" +
            "            padding: 10px;\n" +
            "            border: 1px solid #333;\n" +
            "            margin-top: 10px;\n" +
            "        }\n" +
            "        .packet {\n" +
            "            display: inline-block;\n" +
            "            background-color: #1a1a1a;\n" +
            "            padding: 2px 6px;\n" +
            "            border-radius: 4px;\n" +
            "            margin: 3px;\n" +
            "            border: 1px solid #333;\n" +
            "        }\n" +
            "        .blink {\n" +
            "            animation: blinker 1s linear infinite;\n" +
            "        }\n" +
            "        @keyframes blinker {\n" +
            "            50% { opacity: 0; }\n" +
            "        }\n" +
            "        .logo {\n" +
            "            text-align: center;\n" +
            "            margin-bottom: 20px;\n" +
            "            color: #fff;\n" +
            "            text-shadow: 0 0 10px #0f0;\n" +
            "        }\n" +
            "        pre {\n" +
            "            margin: 0;\n" +
            "        }\n" +
            "    </style>\n" +
            "</head>\n" +
            "<body>\n" +
            "    <div class=\"terminal\">\n" +
            "        <div class=\"terminal-header\">\n" +
            "            <h1 class=\"title\">Bancho.jar Server</h1>\n" +
            "        </div>\n" +
            "\n" +
            "        <div class=\"logo\">\n" +
            "<pre>\n" +
            "██████╗  █████╗ ███╗   ██╗ ██████╗██╗  ██╗ ██████╗ \n" +
            "██╔══██╗██╔══██╗████╗  ██║██╔════╝██║  ██║██╔═══██╗\n" +
            "██████╔╝███████║██╔██╗ ██║██║     ███████║██║   ██║\n" +
            "██╔══██╗██╔══██║██║╚██╗██║██║     ██╔══██║██║   ██║\n" +
            "██████╔╝██║  ██║██║ ╚████║╚██████╗██║  ██║╚██████╔╝\n" +
            "╚═════╝ ╚═╝  ╚═╝╚═╝  ╚═══╝ ╚═════╝╚═╝  ╚═╝ ╚═════╝ \n" +
            "</pre>\n" +
            "        </div>\n" +
            "\n" +
            "        <div class=\"content\">\n" +
            "            <div class=\"section\">\n" +
            "                <div class=\"prompt\">root@bancho.jar:~#</div>\n" +
            "                <div class=\"command\">java stats.class</div>\n" +
            "                <div class=\"output\">\n" +
            "                    <p><span class=\"stat-label\">Server Status:</span> <span class=\"stat-value\">Online <span class=\"blink\">▮</span></span></p>\n" +
            "                    <p><span class=\"stat-label\">Version:</span> <span class=\"stat-value\" id=\"version\">" + version + "</span></p>\n" +
            "                    <p><span class=\"stat-label\">Build:</span> <span class=\"stat-value\" id=\"build\">" + build + "</span></p>\n" +
            "                    <p><span class=\"stat-label\">Players Online:</span> <span class=\"stat-value\" id=\"players\">" + onlinePlayers + "</span></p>\n" +
            "                    <p><span class=\"stat-label\">Channels:</span> <span class=\"stat-value\" id=\"channels\">" + channels + "</span></p>\n" +
            "                </div>\n" +
            "            </div>\n" +
            "\n" +
            "            <div class=\"section\">\n" +
            "                <div class=\"prompt\">root@bancho.jar:~#</div>\n" +
            "                <div class=\"command\">java show-packets.class</div>\n" +
            "                <div class=\"output\">\n" +
            "                    <p>Registered client packet handlers:</p>\n" +
            "                    <div class=\"packets-container\" id=\"packets\">" + packetList + "</div>\n" +
            "                </div>\n" +
            "            </div>\n" +
            "\n" +
            "            <div class=\"section\">\n" +
            "                <div class=\"prompt\">root@bancho.jar:~#</div>\n" +
            "                <div class=\"command\">java motd.class</div>\n" +
            "                <div class=\"output\">\n" +
            "                    <p>This server runs bancho.jar and is ready to serve connections.</p>\n" +
            "                    <p>&copy; marcandreher MIT License <a href=\"https://github.com/marcandreher/bancho.jar\">GitHub</a></p>\n" +
            "                </div>\n" +
            "            </div>\n" +
            "        </div>\n" +
            "    </div>\n" +
            "</body>\n" +
            "</html>";
    }
}