package gg.auroramc.quests.config;

import gg.auroramc.aurora.api.config.AuroraConfig;
import gg.auroramc.quests.AuroraQuests;
import lombok.Getter;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

@Getter
public class MessageConfig extends AuroraConfig {

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
    private String invalidQuestFormat = "&cYou need to specify the quest in this format: 'QUEST_POOL:QUEST_NAME'";
    private String questNotFound = "&cThere isn't any quest with this id: {quest}!";
    private String questStarted = "&aQuest with ID {quest} has started for player {player}.";
    private String errorPrefix = "&cError: {message}";

    public MessageConfig(AuroraQuests plugin, String language) {
        super(getFile(plugin, language));
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
}
