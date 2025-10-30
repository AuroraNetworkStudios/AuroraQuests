package gg.auroramc.quests.config;

import gg.auroramc.aurora.api.config.AuroraConfig;
import gg.auroramc.quests.AuroraQuests;
import lombok.Getter;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

@Getter
public class MessageConfig extends AuroraConfig {

    private String prefix = "";
    private String reloaded = "&aReloaded configuration!";
    private String dataNotLoadedYet = "&cData for this player hasn't loaded yet, try again later!";
    private String dataNotLoadedYetSelf = "&cYour data isn't loaded yet, please try again later!";
    private String playerOnlyCommand = "&cThis command can only be executed by a player!";
    private String noPermission = "&cYou don't have permission to execute this command!";
    private String invalidSyntax = "&cInvalid command syntax!";
    private String mustBeNumber = "&cArgument must be a number!";
    private String playerNotFound = "&cPlayer not found!";
    private String commandError = "&cAn error occurred while executing this command!";
    private String menuOpened = "&aOpened collection menu for {player}";
    private String reRolledTarget = "&aYour quests for {pool} have been re-rolled!";
    private String reRolledSource = "&aQuests for {player} for pool {pool} have been re-rolled!";
    private String globalQuestUnlocked = "&aYou have unlocked the {quest} quest in {pool}!";
    private String poolNotFound = "&cThere isn't any quest line with this id: {pool}!";
    private String poolUnlocked = "&aYou have unlocked a new quest pool: {pool}!";
    private String unknownCommand = "&cUnknown Command, please type /help";
    private String questNotFound = "&cThere isn't any quest with id {quest} in pool {pool}!";
    private String questUnlocked = "&aQuest {quest} unlocked for {player}.";
    private String questCompleted = "&aQuest {quest} marked completed for {player}.";
    private String questReset = "&aQuest {quest} reset for {player}.";
    private String questAlreadyUnlocked = "&cPlayer {player} has already unlocked quest {quest}.";
    private String questAlreadyCompleted = "&cPlayer {player} has already completed quest {quest}.";
    private String errorPrefix = "&cError: {message}";
    private TimerFormatConfig timerFormat = new TimerFormatConfig();
    private ConfigurationSection custom;

    public MessageConfig(AuroraQuests plugin, String language) {
        super(getFile(plugin, language));
    }

    public Map<String, String> toFlatMap() {
        var map = new HashMap<String, String>();

        for (var key : getRawConfig().getKeys(false)) {
            if (key.equals("custom")) {
                for (var customKey : getRawConfig().getConfigurationSection("custom").getKeys(true)) {
                    map.put(customKey, getRawConfig().getString("custom." + customKey));
                }
            } else {
                map.put(key, getRawConfig().getString(key));
            }

        }

        return map;
    }

    private static File getFile(AuroraQuests plugin, String language) {
        return new File(plugin.getDataFolder(), "messages_" + language + ".yml");
    }

    public static void saveDefault(AuroraQuests plugin, String language) {
        if (!getFile(plugin, language).exists()) {
            try {
                plugin.saveResource("messages_" + language + ".yml", false);
            } catch (Exception e) {
                AuroraQuests.logger().warning("Internal message file for language: " + language + " not found! Creating a new one from english...");

                var file = getFile(plugin, language);


                try (InputStream in = plugin.getResource("messages_en.yml")) {
                    Files.copy(in, file.toPath());
                } catch (IOException ex) {
                    AuroraQuests.logger().severe("Failed to create message file for language: " + language);
                    ex.printStackTrace();
                }
            }
        }
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

    @Override
    protected List<Consumer<YamlConfiguration>> getMigrationSteps() {
        return List.of(
                (yaml) -> {
                    yaml.set("quest-not-found", "&cThere isn't any quest with id {quest} in pool {pool}!");
                    yaml.set("quest-unlocked", "&aQuest {quest} unlocked for {player}.");
                    yaml.set("quest-already-unlocked", "&cPlayer {player} has already unlocked quest {quest}.");
                    yaml.set("error-prefix", "&cError: {message}");
                    yaml.set("config-version", 1);
                },
                (yaml) -> {
                    yaml.set("quest-completed", "&aQuest {quest} marked completed for {player}.");
                    yaml.set("quest-already-completed", "&cPlayer {player} has already completed quest {quest}.");
                    yaml.set("config-version", 2);
                },
                (yaml) -> {
                    yaml.set("quest-reset", "&aQuest {quest} reset for {player}.");
                    yaml.set("config-version", 3);
                },
                (yaml) -> {
                    yaml.set("prefix", "");
                    var timer = yaml.createSection("timer-format");
                    // Short
                    var shortFormat = timer.createSection("short-format");
                    // Short plural
                    var shortPlural = shortFormat.createSection("plural");
                    shortPlural.set("weeks", "{value}w");
                    shortPlural.set("days", "{value}d");
                    shortPlural.set("hours", "{value}h");
                    shortPlural.set("minutes", "{value}m");
                    shortPlural.set("seconds", "{value}s");
                    // Short singular
                    var shortSingular = shortFormat.createSection("singular");
                    shortSingular.set("weeks", "{value}w");
                    shortSingular.set("days", "{value}d");
                    shortSingular.set("hours", "{value}h");
                    shortSingular.set("minutes", "{value}m");
                    shortSingular.set("seconds", "{value}s");


                    // Long
                    var longFormat = timer.createSection("long-format");
                    // Long plural
                    var longPlural = longFormat.createSection("plural");
                    longPlural.set("weeks", "{value} weeks");
                    longPlural.set("days", "{value} days");
                    longPlural.set("hours", "{value} hours");
                    longPlural.set("minutes", "{value} minutes");
                    longPlural.set("seconds", "{value} seconds");
                    // Long singular
                    var longSingular = longFormat.createSection("singular");
                    longSingular.set("weeks", "{value} week");
                    longSingular.set("days", "{value} day");
                    longSingular.set("hours", "{value} hour");
                    longSingular.set("minutes", "{value} minute");
                    longSingular.set("seconds", "{value} second");

                    yaml.set("timer-format", timer);

                    yaml.set("custom", yaml.createSection("custom"));

                    yaml.set("config-version", 4);
                }
        );
    }
}
