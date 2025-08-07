package gg.auroramc.quests.command;

import co.aikar.commands.MessageKeys;
import co.aikar.commands.MinecraftMessageKeys;
import co.aikar.commands.PaperCommandManager;
import gg.auroramc.aurora.api.message.Chat;
import gg.auroramc.aurora.api.message.Text;
import gg.auroramc.quests.AuroraQuests;
import gg.auroramc.quests.api.questpool.PoolConfig;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class CommandManager {
    private final AuroraQuests plugin;
    private final PaperCommandManager commandManager;
    private final LegacyComponentSerializer serializer = LegacyComponentSerializer.legacyAmpersand().toBuilder()
            .hexColors().useUnusualXRepeatedCharacterHexFormat().build();
    private boolean hasSetup = false;

    public CommandManager(AuroraQuests plugin) {
        this.commandManager = new PaperCommandManager(plugin);
        this.plugin = plugin;
    }

    private void setupCommands() {
        if (!this.hasSetup) {
            commandManager.getLocales().setDefaultLocale(Locale.ENGLISH);
            commandManager.usePerIssuerLocale(false);

            var aliases = plugin.getConfigManager().getConfig().getCommandAliases();
            var pools = plugin.getConfigManager().getQuestPools();

            commandManager.getCommandCompletions().registerCompletion("pools", c ->
                    pools.values().stream().map(PoolConfig::getId).collect(Collectors.toList()));

            commandManager.getCommandCompletions().registerCompletion("quests", c ->
                    pools.values().stream()
                            .filter(pool -> c.getContextValue(String.class).equals(pool.getId()))
                            .flatMap(pool -> pool.getQuests().keySet().stream())
                            .collect(Collectors.toList()));

            commandManager.getCommandReplacements().addReplacement("questsAlias", a(aliases.getQuests()));
        }

        var msg = plugin.getConfigManager().getMessageConfig(null);
        commandManager.getLocales().addMessage(Locale.ENGLISH, MinecraftMessageKeys.NO_PLAYER_FOUND, m(msg.getPlayerNotFound()));
        commandManager.getLocales().addMessage(Locale.ENGLISH, MinecraftMessageKeys.NO_PLAYER_FOUND_OFFLINE, m(msg.getPlayerNotFound()));
        commandManager.getLocales().addMessage(Locale.ENGLISH, MinecraftMessageKeys.NO_PLAYER_FOUND_SERVER, m(msg.getPlayerNotFound()));
        commandManager.getLocales().addMessage(Locale.ENGLISH, MinecraftMessageKeys.IS_NOT_A_VALID_NAME, m(msg.getPlayerNotFound()));
        commandManager.getLocales().addMessage(Locale.ENGLISH, MessageKeys.COULD_NOT_FIND_PLAYER, m(msg.getPlayerNotFound()));
        commandManager.getLocales().addMessage(Locale.ENGLISH, MessageKeys.PERMISSION_DENIED, m(msg.getNoPermission()));
        commandManager.getLocales().addMessage(Locale.ENGLISH, MessageKeys.PERMISSION_DENIED_PARAMETER, m(msg.getNoPermission()));
        commandManager.getLocales().addMessage(Locale.ENGLISH, MessageKeys.INVALID_SYNTAX, m(msg.getInvalidSyntax()));
        commandManager.getLocales().addMessage(Locale.ENGLISH, MessageKeys.MUST_BE_A_NUMBER, m(msg.getMustBeNumber()));
        commandManager.getLocales().addMessage(Locale.ENGLISH, MessageKeys.ERROR_PERFORMING_COMMAND, m(msg.getCommandError()));
        commandManager.getLocales().addMessage(Locale.ENGLISH, MessageKeys.ERROR_GENERIC_LOGGED, m(msg.getCommandError()));
        commandManager.getLocales().addMessage(Locale.ENGLISH, MessageKeys.NOT_ALLOWED_ON_CONSOLE, m(msg.getPlayerOnlyCommand()));
        commandManager.getLocales().addMessage(Locale.ENGLISH, MessageKeys.UNKNOWN_COMMAND, m(msg.getUnknownCommand()));
        commandManager.getLocales().addMessage(Locale.ENGLISH, MessageKeys.ERROR_PREFIX, m(msg.getErrorPrefix()));

        if (!this.hasSetup) {
            this.commandManager.registerCommand(new QuestsCommand(plugin));
            this.hasSetup = true;
        }
    }

    public void reload() {
        this.setupCommands();
    }

    private String a(List<String> aliases) {
        return String.join("|", aliases);
    }

    private String m(String msg) {
        return serializer.serialize(Text.component(Chat.translateToMM(msg)));
    }

    public void unregisterCommands() {
        this.commandManager.unregisterCommands();
    }
}
