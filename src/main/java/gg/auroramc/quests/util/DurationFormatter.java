package gg.auroramc.quests.util;


import gg.auroramc.quests.AuroraQuests;

import java.time.Duration;

public class DurationFormatter {
    private static final long SECONDS_IN_A_MINUTE = 60;
    private static final long SECONDS_IN_AN_HOUR = 3600;
    private static final long SECONDS_IN_A_DAY = 86400;
    private static final long SECONDS_IN_A_WEEK = 604800;

    public enum Type {
        LONG,
        SHORT
    }

    public static String format(Duration duration, Type type) {
        var config = AuroraQuests.getInstance().getConfigManager().getConfig().getTimerFormat();
        var format = type == Type.LONG ? config.getLongFormat() : config.getShortFormat();

        long seconds = duration.getSeconds();
        long weeks = seconds / SECONDS_IN_A_WEEK;
        seconds %= SECONDS_IN_A_WEEK;

        long days = seconds / SECONDS_IN_A_DAY;
        seconds %= SECONDS_IN_A_DAY;

        long hours = seconds / SECONDS_IN_AN_HOUR;
        seconds %= SECONDS_IN_AN_HOUR;

        long minutes = seconds / SECONDS_IN_A_MINUTE;
        seconds %= SECONDS_IN_A_MINUTE;

        StringBuilder result = new StringBuilder();

        if (weeks > 0) {
            if(weeks > 1) {
                result.append(format.getPlural().getWeeks().replace("{value}", weeks + ""));
            } else {
                result.append(format.getSingular().getWeeks().replace("{value}", weeks + ""));
            }
        }
        if (days > 0) {
            if (!result.isEmpty()) result.append(" ");
            if(days > 1) {
                result.append(format.getPlural().getDays().replace("{value}", days + ""));
            } else {
                result.append(format.getSingular().getDays().replace("{value}", days + ""));
            }
        }
        if (hours > 0) {
            if (!result.isEmpty()) result.append(" ");
            if(hours > 1) {
                result.append(format.getPlural().getHours().replace("{value}", hours + ""));
            } else {
                result.append(format.getSingular().getHours().replace("{value}", hours + ""));
            }
        }
        if (minutes > 0) {
            if (!result.isEmpty()) result.append(" ");
            if(minutes > 1) {
                result.append(format.getPlural().getMinutes().replace("{value}", minutes + ""));
            } else {
                result.append(format.getSingular().getMinutes().replace("{value}", minutes + ""));
            }
        }
        if (seconds > 0 || result.isEmpty()) {
            if (!result.isEmpty()) result.append(" ");
            if(seconds > 1) {
                result.append(format.getPlural().getSeconds().replace("{value}", seconds + ""));
            } else {
                result.append(format.getSingular().getSeconds().replace("{value}", seconds + ""));
            }
        }

        return result.toString();
    }
}
