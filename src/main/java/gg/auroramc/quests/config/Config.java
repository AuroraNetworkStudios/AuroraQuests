package gg.auroramc.quests.config;

import gg.auroramc.aurora.api.config.decorators.IgnoreField;
import lombok.Getter;
import org.bukkit.configuration.file.YamlConfiguration;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

@Getter
public class Config {
    private Boolean debug = false;
    private Boolean purgeInvalidDataOnLogin = false;
    private Boolean hideOutOfRangePageButtons = false;
    private String language = "en";
    private Boolean perPlayerLocale = false;
    private Map<String, String> difficulties;
    private Boolean preventCreativeMode = false;
    private LeaderboardConfig leaderboards;
    private Map<String, DisplayComponent> displayComponents;
    private LevelUpSound levelUpSound;
    private LevelUpMessage levelUpMessage;
    private LevelUpSound questCompleteSound;
    private LevelUpMessage questCompleteMessage;
    private CommandAliasConfig commandAliases;
    private List<String> sortOrder;
    private UnlockTaskConfig unlockTask = new UnlockTaskConfig();

    @IgnoreField
    private volatile Map<String, Integer> sortOderMap;

    // Worked out from sort-order on first use, the config doesn't change after it's loaded
    public Map<String, Integer> getSortOderMap() {
        var map = sortOderMap;
        if (map == null) {
            Map<String, Integer> difficultyOrder = new HashMap<>();
            for (int i = 0; i < sortOrder.size(); i++) {
                difficultyOrder.put(sortOrder.get(i), i);
            }
            sortOderMap = map = difficultyOrder;
        }
        return map;
    }

    @Getter
    public static final class LeaderboardConfig {
        private Integer cacheSize = 10;
        private Integer minCompleted = 3;
        private Boolean includeGlobal = false;
    }

    @Getter
    public static final class DisplayComponent {
        private String title;
        private String line;
    }

    @Getter
    public static final class UnlockTaskConfig {
        private Boolean enabled = false;
        private Integer interval = 5;
    }

    @Getter
    public static final class LevelUpMessage {
        private Boolean enabled;
        private List<String> message;
    }

    @Getter
    public static final class LevelUpSound {
        private Boolean enabled;
        private String sound;
        private Float volume;
        private Float pitch;
    }

    @Getter
    public static final class CommandAliasConfig {
        private List<String> quests = List.of("quests");
    }

    // Steps by config-version, only ever appended, each sets config-version itself
    public static List<Consumer<YamlConfiguration>> migrations() {
        return List.of(
                (yaml) -> {
                    yaml.set("purge-invalid-data-on-login", false);
                    yaml.setComments("purge-invalid-data-on-login", List.of("Only enable this if you are heavily modifying your quest pools/quests data in production (WHICH YOU SHOULDN'T)"));

                    yaml.set("unlock-task.enabled", false);
                    yaml.set("unlock-task.interval", 5);
                    yaml.setComments("unlock-task", List.of("Timer to try to unlock global quests and quest pools if for some reason the event driven method doesn't work"));
                    yaml.setComments("unlock-task.interval", List.of("Interval in seconds"));

                    for (var key : List.of("weeks", "days", "hours", "minutes", "seconds")) {
                        yaml.set("timer-format.short-format.plural." + key, "{value}" + key.charAt(0));
                        yaml.set("timer-format.short-format.singular." + key, "{value}" + key.charAt(0));
                        yaml.set("timer-format.long-format.plural." + key, "{value} " + key);
                        yaml.set("timer-format.long-format.singular." + key, "{value} " + key.substring(0, key.length() - 1));
                    }

                    yaml.set("config-version", 1);
                },
                (yaml) -> {
                    yaml.set("level-up-sound.sound", "entity.player.levelup");
                    yaml.set("quest-complete-sound.sound", "entity.player.levelup");

                    yaml.set("config-version", 2);
                },
                (yaml) -> {
                    yaml.set("timer-format", null);
                    yaml.set("config-version", 3);
                },
                (yaml) -> {
                    yaml.set("per-player-locale", false);
                    yaml.set("config-version", 4);
                },
                (yaml) -> {
                    yaml.set("hide-out-of-range-page-buttons", false);
                    yaml.setComments("hide-out-of-range-page-buttons", List.of("Hide the previous/next page button when there is no such page"));
                    yaml.set("config-version", 5);
                }
        );
    }
}
