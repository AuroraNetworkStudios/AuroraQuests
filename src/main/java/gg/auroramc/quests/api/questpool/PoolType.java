package gg.auroramc.quests.api.questpool;

import java.util.Locale;

public enum PoolType {
    GLOBAL,
    TIMED_RANDOM,
    GLOBAL_SHARED;

    public static PoolType fromString(String string) {
        return PoolType.valueOf(string.toUpperCase(Locale.ROOT).replaceAll("-", "_"));
    }
}
