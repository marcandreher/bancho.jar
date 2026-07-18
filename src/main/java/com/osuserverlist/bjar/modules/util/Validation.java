package com.osuserverlist.bjar.modules.util;

import java.util.Arrays;

import com.osuserverlist.bjar.modules.main.Commands.Session;

import lombok.experimental.UtilityClass;

@UtilityClass
public class Validation {
    private static final int MAX_REASON_LENGTH = 100;

    /**
     * Parses a duration string like "30s", "10m", "2h", "1d", "1w" into seconds.
     */
    public int parseDuration(String input) {
        if (input == null || input.isEmpty()) {
            return 0; // Treat invalid numbers as 0 seconds
        }

        char unit = Character.toLowerCase(input.charAt(input.length() - 1));
        String numberPart = input.substring(0, input.length() - 1);

        int value;
        try {
            value = Integer.parseInt(numberPart);
        } catch (NumberFormatException e) {
            return 0; // Treat invalid numbers as 0 seconds
        }

        if (value <= 0) {
            return 0; // Treat non-positive durations as 0 seconds
        }

        switch (unit) {
            case 's': return value;
            case 'm': return value * 60;
            case 'h': return value * 3600;
            case 'd': return value * 86400;
            case 'w': return value * 604800;
            default:
                throw new IllegalArgumentException("Unknown duration unit: " + unit);
        }
    }

    public String formatDuration(long seconds) {
        if (seconds % 604800 == 0) return (seconds / 604800) + "w";
        if (seconds % 86400 == 0) return (seconds / 86400) + "d";
        if (seconds % 3600 == 0) return (seconds / 3600) + "h";
        if (seconds % 60 == 0) return (seconds / 60) + "m";
        return seconds + "s";
    }

    public boolean isValidReason(Session session, String reason) {
        if (reason.isEmpty()) {
            session.sendAnswer("Reason cannot be empty.");
            return false;
        }

        if (reason.length() > MAX_REASON_LENGTH) {
            session.sendAnswer("Reason cannot exceed " + MAX_REASON_LENGTH + " characters.");
            return false;
        }

        return true;
    }

    public String joinReason(String[] args, int fromIndex) {
        return String.join(" ", Arrays.copyOfRange(args, fromIndex, args.length)).trim();
    }
}
