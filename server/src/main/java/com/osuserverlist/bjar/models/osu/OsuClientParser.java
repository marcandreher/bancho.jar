package com.osuserverlist.bjar.models.osu;

import java.time.LocalDate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class OsuClientParser {
    
    public static LocalDate parseOsuVersionDate(String buildName) {
        if (buildName == null) {
            return LocalDate.now();
        }

        Matcher matcher = Pattern.compile("(\\d{4})(\\d{2})(\\d{2})").matcher(buildName);
        if (!matcher.find()) {
            return LocalDate.now();
        }

        int year = Integer.parseInt(matcher.group(1));
        int month = Integer.parseInt(matcher.group(2));
        int day = Integer.parseInt(matcher.group(3));
        try {
            return LocalDate.of(year, month, day);
        } catch (RuntimeException ex) {
            return LocalDate.now();
        }
    }

}
