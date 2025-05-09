package com.banchojar.utils;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.IThrowableProxy;
import ch.qos.logback.classic.spi.ThrowableProxyUtil;
import ch.qos.logback.core.LayoutBase;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PrettyLogLayout extends LayoutBase<ILoggingEvent> {

    private static final Pattern PAREN_NUMBER = Pattern.compile("\\((\\d+)\\)");
    private static final Pattern ANGLE_STRING = Pattern.compile("<([^>]+)>");

    @Override
    public String doLayout(ILoggingEvent event) {
        StringBuilder sb = new StringBuilder();

        switch (event.getLevel().toString()) {
            case "INFO":
                sb.append(Color.BLUE);
                break;
            case "DEBUG":
                sb.append(Color.YELLOW_BOLD);
                break;
            case "ERROR":
                sb.append(Color.RED_BOLD);
                break;
            case "WARN":
                sb.append(Color.YELLOW_BOLD);
                break;
            default:
                sb.append(Color.WHITE_BOLD);
        }

        sb.append(event.getLevel()).append(Color.RESET).append(" | ");

        String msg = event.getFormattedMessage();

        // Highlight methods
        msg = msg.replace("[GET]", Color.BLUE + "[GET]" + Color.RESET)
                 .replace("[POST]", Color.RED_BRIGHT + "[POST]" + Color.RESET)
                 .replace("[BANCHO]", Color.GREEN_BRIGHT + "[BANCHO]" + Color.RESET)
                 .replace("[SQL]", Color.YELLOW_BOLD + "[SQL]" + Color.RESET);

        // Highlight (numbers)
        Matcher parenMatcher = PAREN_NUMBER.matcher(msg);
        StringBuffer result = new StringBuffer();
        while (parenMatcher.find()) {
            parenMatcher.appendReplacement(result,
                Color.MAGENTA + "(" + parenMatcher.group(1) + ")" + Color.RESET);
        }
        parenMatcher.appendTail(result);
        msg = result.toString();

        // Highlight <strings>
        Matcher angleMatcher = ANGLE_STRING.matcher(msg);
        result = new StringBuffer();
        while (angleMatcher.find()) {
            angleMatcher.appendReplacement(result,
                Color.CYAN + "<" + angleMatcher.group(1) + ">" + Color.RESET);
        }
        angleMatcher.appendTail(result);
        msg = result.toString();

        sb.append(msg).append("\n");

        IThrowableProxy throwableProxy = event.getThrowableProxy();
        if (throwableProxy != null) {
            sb.append(Color.RED);
            sb.append(ThrowableProxyUtil.asString(throwableProxy));
            sb.append(Color.RESET);
        }
        return sb.toString();
    }
}
