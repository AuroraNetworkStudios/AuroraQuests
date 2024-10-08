package gg.auroramc.quests.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import gg.auroramc.aurora.api.AuroraAPI;
import gg.auroramc.aurora.api.message.Chat;
import gg.auroramc.aurora.api.message.Placeholder;
import gg.auroramc.quests.AuroraQuests;
import gg.auroramc.quests.api.quest.Quest;
import gg.auroramc.quests.api.quest.QuestPool;
import gg.auroramc.quests.menu.MainMenu;
import gg.auroramc.quests.menu.PoolMenu;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@CommandAlias("%questsAlias")
public class QuestsCommand extends BaseCommand {
    private final AuroraQuests plugin;

    public QuestsCommand(AuroraQuests plugin) {
        this.plugin = plugin;
    }

    @Default
    @Description("Opens the quests menu")
    @CommandPermission("aurora.quests.use")
    public void onMenu(Player player) {
        if (!AuroraAPI.getUser(player.getUniqueId()).isLoaded()) {
            Chat.sendMessage(player, plugin.getConfigManager().getMessageConfig().getDataNotLoadedYetSelf());
            return;
        }
        new MainMenu(player).open();
    }

    @Subcommand("reload")
    @Description("Reloads the plugin configs and applies reward auto correctors to players")
    @CommandPermission("aurora.quests.admin.reload")
    public void onReload(CommandSender sender) {
        plugin.reload();
        Chat.sendMessage(sender, plugin.getConfigManager().getMessageConfig().getReloaded());
    }

    @Subcommand("open")
    @Description("Opens the quest menu for another player in a specific pool")
    @CommandCompletion("@players @pools|none|all true|false")
    @CommandPermission("aurora.quests.admin.open")
    public void onOpenMenu(CommandSender sender, @Flags("other") Player target, @Default("none") String poolId, @Default("false") Boolean silent) {
        if (poolId.equals("none") || poolId.equals("all")) {
            new MainMenu(target).open();
        } else {
            var pool = plugin.getQuestManager().getQuestPool(poolId);
            if (pool == null) {
                Chat.sendMessage(sender, plugin.getConfigManager().getMessageConfig().getPoolNotFound(), Placeholder.of("{pool}", poolId));
                return;
            }
            if (pool.isUnlocked(target)) {
                new PoolMenu(target, pool).open();
                if (!silent) {
                    Chat.sendMessage(sender, plugin.getConfigManager().getMessageConfig().getMenuOpened(), Placeholder.of("{player}", target.getName()));
                }
            }
        }
    }

    @Subcommand("reroll")
    @Description("Rerolls quests for another player in a specific pool")
    @CommandCompletion("@players @pools|none|all true|false")
    @CommandPermission("aurora.quests.admin.reroll")
    public void onReroll(CommandSender sender, @Flags("other") Player target, @Default("all") String poolId, @Default("false") Boolean silent) {
        if (poolId.equals("none") || poolId.equals("all")) {
            plugin.getQuestManager().getQuestPools().forEach((pool) -> pool.reRollQuests(target, !silent));
        } else {
            var pool = plugin.getQuestManager().getQuestPool(poolId);
            if (pool != null) {
                if (!pool.isUnlocked(target)) return;
                pool.reRollQuests(target, !silent);
                if (!silent) {
                    Chat.sendMessage(sender, plugin.getConfigManager().getMessageConfig().getReRolledSource(), Placeholder.of("{player}", target.getName()), Placeholder.of("{pool}", pool.getConfig().getName()));
                }
            } else {
                Chat.sendMessage(sender, plugin.getConfigManager().getMessageConfig().getPoolNotFound(), Placeholder.of("{pool}", poolId));
            }
        }
    }

    @Subcommand("unlock")
    @Description("Unlocks quest for player")
    @CommandCompletion("@players @pools @quests true|false")
    @CommandPermission("aurora.quests.admin.unlock")
    public void onQuestUnlock(CommandSender sender, @Flags("other") Player target, String poolId, String questId, @Default("false") Boolean silent) {
        QuestPool pool = plugin.getQuestManager().getQuestPool(poolId);
        if (pool == null) {
            Chat.sendMessage(sender, plugin.getConfigManager().getMessageConfig().getPoolNotFound(), Placeholder.of("{pool}", poolId));
            return;
        }

        Quest quest = pool.getQuest(questId);
        if (quest == null) {
            Chat.sendMessage(sender, plugin.getConfigManager().getMessageConfig().getQuestNotFound(), Placeholder.of("{pool}", pool.getId()), Placeholder.of("{quest}", questId));
            return;
        }

        if (!quest.isUnlocked(target)) {
            // Will unlock any locked quest, not just the ones that have manual-unlock requirement
            quest.forceStart(target);
            if (!silent) {
                Chat.sendMessage(sender, plugin.getConfigManager().getMessageConfig().getQuestUnlocked(), Placeholder.of("{player}", target.getName()), Placeholder.of("{quest}", questId));
            }
        } else {
            Chat.sendMessage(sender, plugin.getConfigManager().getMessageConfig().getQuestAlreadyUnlocked(), Placeholder.of("{player}", target.getName()), Placeholder.of("{quest}", questId));
        }
    }

    @Subcommand("complete")
    @Description("Completes a quest for a player")
    @CommandCompletion("@players @pools @quests true|false")
    @CommandPermission("aurora.quests.admin.complete")
    public void onQuestComplete(CommandSender sender, @Flags("other") Player target, String poolId, String questId, @Default("false") Boolean silent) {
        QuestPool pool = plugin.getQuestManager().getQuestPool(poolId);
        if (pool == null) {
            Chat.sendMessage(sender, plugin.getConfigManager().getMessageConfig().getPoolNotFound(), Placeholder.of("{pool}", poolId));
            return;
        }

        Quest quest = pool.getQuest(questId);
        if (quest == null) {
            Chat.sendMessage(sender, plugin.getConfigManager().getMessageConfig().getQuestNotFound(), Placeholder.of("{pool}", pool.getId()), Placeholder.of("{quest}", questId));
            return;
        }

        if (!quest.isCompleted(target)) {
            quest.complete(target);
            if (!silent) {
                Chat.sendMessage(sender, plugin.getConfigManager().getMessageConfig().getQuestCompleted(), Placeholder.of("{player}", target.getName()), Placeholder.of("{quest}", questId));
            }
        } else {
            Chat.sendMessage(sender, plugin.getConfigManager().getMessageConfig().getQuestAlreadyCompleted(), Placeholder.of("{player}", target.getName()), Placeholder.of("{quest}", questId));
        }
    }
}
