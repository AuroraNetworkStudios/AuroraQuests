package gg.auroramc.quests.config;

import gg.auroramc.aurora.api.config.AuroraConfig;
import gg.auroramc.aurora.api.config.decorators.IgnoreField;
import gg.auroramc.quests.AuroraQuests;
import lombok.Getter;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

@Getter
public class Config extends AuroraConfig {
    private Boolean debug = false;
    private Boolean purgeInvalidDataOnLogin = false;
    private String language = "en";
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
    private TimerFormatConfig timerFormat = new TimerFormatConfig();

    @IgnoreField
    private Map<String, Integer> sortOderMap;

    @Override
    public void load() {
        super.load();
        Map<String, Integer> difficultyOrder = new HashMap<>();
        for (int i = 0; i < sortOrder.size(); i++) {
            difficultyOrder.put(sortOrder.get(i), i);
        }
        sortOderMap = difficultyOrder;
    }

    public Config(AuroraQuests plugin) {
        super(getFile(plugin));
    }

    @Getter
    public static final class TimerFormatConfig {
        private DurationFormatConfig shortFormat = new DurationFormatConfig();
        private DurationFormatConfig longFormat = new DurationFormatConfig();
    }

    @Getter
    public static final class DurationFormatConfig {
        private DurationConfig plural = new DurationConfig();
        private DurationConfig singular = new DurationConfig();
    }

    @Getter
    public static final class DurationConfig {
        private String weeks = "{value}w";
        private String days = "{value}d";
        private String hours = "{value}h";
        private String minutes = "{value}m";
        private String seconds = "{value}s";
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

    public static File getFile(AuroraQuests plugin) {
        return new File(plugin.getDataFolder(), "config.yml");
    }

    public static void saveDefault(AuroraQuests plugin) {
        if (!getFile(plugin).exists()) {
            plugin.saveResource("config.yml", false);
        }
    }

    @Override
    protected List<Consumer<YamlConfiguration>> getMigrationSteps() {
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
                }
        );
    }
}
