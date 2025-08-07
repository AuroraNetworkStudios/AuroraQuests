package gg.auroramc.quests.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import gg.auroramc.aurora.api.AuroraAPI;
import gg.auroramc.aurora.api.message.Chat;
import gg.auroramc.aurora.api.message.Placeholder;
import gg.auroramc.quests.AuroraQuests;
import gg.auroramc.quests.api.questpool.Pool;
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
        var profile = plugin.getProfileManager().getProfile(player);
        if (profile == null) {
            Chat.sendMessage(player, plugin.getConfigManager().getMessageConfig(player).getDataNotLoadedYetSelf());
            return;
        }
        new MainMenu(profile).open();
    }

    @Subcommand("reload")
    @Description("Reloads the plugin configs and applies reward auto correctors to players")
    @CommandPermission("aurora.quests.admin.reload")
    public void onReload(CommandSender sender) {
        plugin.reload();
        Chat.sendMessage(sender, plugin.getConfigManager().getMessageConfig(sender).getReloaded());
    }

    @Subcommand("open")
    @Description("Opens the quest menu for another player in a specific pool")
    @CommandCompletion("@players @pools|none|all true|false")
    @CommandPermission("aurora.quests.admin.open")
    public void onOpenMenu(CommandSender sender, @Flags("other") Player target, @Default("none") String poolId, @Default("false") Boolean silent) {
        var profile = plugin.getProfileManager().getProfile(target);
        if (profile == null) {
            Chat.sendMessage(sender, plugin.getConfigManager().getMessageConfig(sender).getDataNotLoadedYet(), Placeholder.of("{target}", target.getName()));
            return;
        }

        if (poolId.equals("none") || poolId.equals("all")) {
            new MainMenu(profile).open();
        } else {
            var pool = profile.getQuestPool(poolId);
            if (pool == null) {
                Chat.sendMessage(sender, plugin.getConfigManager().getMessageConfig(sender).getPoolNotFound(), Placeholder.of("{pool}", poolId));
                return;
            }
            if (pool.isUnlocked()) {
                new PoolMenu(profile, pool).open();
                if (!silent) {
                    Chat.sendMessage(sender, plugin.getConfigManager().getMessageConfig(sender).getMenuOpened(), Placeholder.of("{player}", target.getName()));
                }
            }
        }
    }

    @Subcommand("reroll")
    @Description("Rerolls quests for another player in a specific pool")
    @CommandCompletion("@players @pools|none|all true|false")
    @CommandPermission("aurora.quests.admin.reroll")
    public void onReroll(CommandSender sender, @Flags("other") Player target, @Default("all") String poolId, @Default("false") Boolean silent) {
        var profile = plugin.getProfileManager().getProfile(target);
        if (profile == null) {
            Chat.sendMessage(sender, plugin.getConfigManager().getMessageConfig(sender).getDataNotLoadedYet(), Placeholder.of("{target}", target.getName()));
            return;
        }

        if (poolId.equals("none") || poolId.equals("all")) {
            profile.getQuestPools().forEach((pool) -> pool.reRollQuests(!silent));
        } else {
            var pool = profile.getQuestPool(poolId);
            if (pool != null) {
                if (!pool.isUnlocked()) return;
                pool.reRollQuests(!silent);
                if (!silent) {
                    Chat.sendMessage(sender, plugin.getConfigManager().getMessageConfig(sender).getReRolledSource(), Placeholder.of("{player}", target.getName()), Placeholder.of("{pool}", pool.getDefinition().getName()));
                }
            } else {
                Chat.sendMessage(sender, plugin.getConfigManager().getMessageConfig(sender).getPoolNotFound(), Placeholder.of("{pool}", poolId));
            }
        }
    }

    @Subcommand("unlock")
    @Description("Unlocks quest for player")
    @CommandCompletion("@players @pools @quests true|false")
    @CommandPermission("aurora.quests.admin.unlock")
    public void onQuestUnlock(CommandSender sender, @Flags("other") Player target, String poolId, String questId, @Default("false") Boolean silent) {
        var profile = plugin.getProfileManager().getProfile(target);
        if (profile == null) {
            Chat.sendMessage(sender, plugin.getConfigManager().getMessageConfig(sender).getDataNotLoadedYet(), Placeholder.of("{target}", target.getName()));
            return;
        }

        var pool = profile.getQuestPool(poolId);
        if (pool == null) {
            Chat.sendMessage(sender, plugin.getConfigManager().getMessageConfig(sender).getPoolNotFound(), Placeholder.of("{pool}", poolId));
            return;
        }

        var quest = pool.getQuest(questId);
        if (quest == null) {
            Chat.sendMessage(sender, plugin.getConfigManager().getMessageConfig(sender).getQuestNotFound(), Placeholder.of("{pool}", pool.getId()), Placeholder.of("{quest}", questId));
            return;
        }

        if (!quest.isUnlocked()) {
            // Will unlock any locked quest, not just the ones that have manual-unlock requirement
            quest.start(true);
            if (!silent) {
                Chat.sendMessage(sender, plugin.getConfigManager().getMessageConfig(sender).getQuestUnlocked(), Placeholder.of("{player}", target.getName()), Placeholder.of("{quest}", questId));
            }
        } else {
            Chat.sendMessage(sender, plugin.getConfigManager().getMessageConfig(sender).getQuestAlreadyUnlocked(), Placeholder.of("{player}", target.getName()), Placeholder.of("{quest}", questId));
        }
    }

    @Subcommand("complete")
    @Description("Completes a quest for a player")
    @CommandCompletion("@players @pools @quests true|false")
    @CommandPermission("aurora.quests.admin.complete")
    public void onQuestComplete(CommandSender sender, @Flags("other") Player target, String poolId, String questId, @Default("false") Boolean silent) {
        var profile = plugin.getProfileManager().getProfile(target);
        if (profile == null) {
            Chat.sendMessage(sender, plugin.getConfigManager().getMessageConfig(sender).getDataNotLoadedYet(), Placeholder.of("{target}", target.getName()));
            return;
        }

        var pool = profile.getQuestPool(poolId);
        if (pool == null) {
            Chat.sendMessage(sender, plugin.getConfigManager().getMessageConfig(sender).getPoolNotFound(), Placeholder.of("{pool}", poolId));
            return;
        }

        var quest = pool.getQuest(questId);
        if (quest == null) {
            Chat.sendMessage(sender, plugin.getConfigManager().getMessageConfig(sender).getQuestNotFound(), Placeholder.of("{pool}", pool.getId()), Placeholder.of("{quest}", questId));
            return;
        }

        if (!quest.isCompleted()) {
            quest.complete();
            if (!silent) {
                Chat.sendMessage(sender, plugin.getConfigManager().getMessageConfig(sender).getQuestCompleted(), Placeholder.of("{player}", target.getName()), Placeholder.of("{quest}", questId));
            }
        } else {
            Chat.sendMessage(sender, plugin.getConfigManager().getMessageConfig(sender).getQuestAlreadyCompleted(), Placeholder.of("{player}", target.getName()), Placeholder.of("{quest}", questId));
        }
    }

    @Subcommand("reset")
    @Description("Reset quest progress a player")
    @CommandCompletion("@players @pools @quests|all true|false")
    @CommandPermission("aurora.quests.admin.reset")
    public void onQuestReset(CommandSender sender, @Flags("other") Player target, String poolId, @Default("all") String questId, @Default("false") Boolean silent) {
        var profile = plugin.getProfileManager().getProfile(target);
        if (profile == null) {
            Chat.sendMessage(sender, plugin.getConfigManager().getMessageConfig(sender).getDataNotLoadedYet(), Placeholder.of("{target}", target.getName()));
            return;
        }

        var pool = profile.getQuestPool(poolId);
        if (pool == null) {
            Chat.sendMessage(sender, plugin.getConfigManager().getMessageConfig(sender).getPoolNotFound(), Placeholder.of("{pool}", poolId));
            return;
        }

        var quest = pool.getQuest(questId);
        if (questId.equals("all")) {
            pool.resetAllQuestProgress();
            if (!silent) {
                Chat.sendMessage(sender, plugin.getConfigManager().getMessageConfig(sender).getQuestReset(), Placeholder.of("{player}", target.getName()), Placeholder.of("{quest}", "all"));
            }
            return;
        } else if (quest == null) {
            Chat.sendMessage(sender, plugin.getConfigManager().getMessageConfig(sender).getQuestNotFound(), Placeholder.of("{pool}", pool.getId()), Placeholder.of("{quest}", questId));
            return;
        }

        quest.reset();
        if (pool.isGlobal()) {
            quest.start(false);
        } else if (pool.isRolledQuest(quest)) {
            quest.start(false);
        }

        if (!silent) {
            Chat.sendMessage(sender, plugin.getConfigManager().getMessageConfig(sender).getQuestReset(), Placeholder.of("{player}", target.getName()), Placeholder.of("{quest}", questId));
        }
    }
}
