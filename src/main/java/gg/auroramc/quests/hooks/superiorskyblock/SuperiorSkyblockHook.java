package gg.auroramc.quests.hooks.superiorskyblock;

import com.bgsoftware.superiorskyblock.api.SuperiorSkyblockAPI;
import com.bgsoftware.superiorskyblock.api.events.*;
import com.bgsoftware.superiorskyblock.api.island.Island;
import gg.auroramc.aurora.api.events.user.AuroraUserLoadedEvent;
import gg.auroramc.quests.AuroraQuests;
import gg.auroramc.quests.api.event.objective.PlayerIslandLevelChangeEvent;
import gg.auroramc.quests.api.event.objective.PlayerIslandWorthChangeEvent;
import gg.auroramc.quests.api.event.objective.PlayerJoinIslandEvent;
import gg.auroramc.quests.api.event.objective.PlayerUpgradeIslandEvent;
import gg.auroramc.quests.hooks.Hook;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class SuperiorSkyblockHook implements Hook, Listener {
    private AuroraQuests plugin;

    @Override
    public void hook(AuroraQuests plugin) {
        this.plugin = plugin;
        AuroraQuests.logger().info("Hooked SuperiorSkyblock2 for island hooks.");
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onIslandCreate(IslandCreateEvent event) {
        Bukkit.getPluginManager().callEvent(new PlayerJoinIslandEvent(event.getPlayer().asPlayer()));
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onIslandJoin(IslandJoinEvent event) {
        Bukkit.getPluginManager().callEvent(new PlayerJoinIslandEvent(event.getPlayer().asPlayer()));
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onIslandWorthCalc(IslandWorthCalculatedEvent event) {
        var worth = event.getWorth().doubleValue();
        var level = event.getLevel().doubleValue();

        updateIslandMembers(worth, level, event.getIsland());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onIslandWorthCalc(IslandWorthUpdateEvent event) {
        var worth = event.getNewWorth().doubleValue();
        var level = event.getNewLevel().doubleValue();

        updateIslandMembers(worth, level, event.getIsland());
    }

    @EventHandler
    public void onUserLoaded(AuroraUserLoadedEvent event) {
        Bukkit.getGlobalRegionScheduler().runDelayed(AuroraQuests.getInstance(), (task) -> {
            var player = event.getUser().getPlayer();
            if (player == null || !player.isOnline()) return;

            var island = SuperiorSkyblockAPI.getPlayer(player).getIsland();

            if (island != null) {
                updateIslandMembers(
                        island.getWorth().doubleValue(),
                        island.getIslandLevel().doubleValue(),
                        island
                );
                updateIslandUpgrades(island);
            }
        }, 100);
    }

    private void updateIslandMembers(double worth, double level, Island island) {
        for (var sPlayer : island.getIslandMembers(true)) {
            var player = sPlayer.asPlayer();
            if (player != null) {
                Bukkit.getPluginManager().callEvent(new PlayerIslandLevelChangeEvent(player, level));
                Bukkit.getPluginManager().callEvent(new PlayerIslandWorthChangeEvent(player, worth));
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onIslandUpgrade(IslandUpgradeEvent event) {
        var island = event.getIsland();
        for (var sPlayer : island.getIslandMembers(true)) {
            var player = sPlayer.asPlayer();
            if (player != null) {
                Bukkit.getPluginManager().callEvent(new PlayerUpgradeIslandEvent(player, event.getUpgrade().getName(), event.getUpgradeLevel().getLevel()));
            }
        }
    }

    private void updateIslandUpgrades(Island island) {
        for (var upgrade : island.getUpgrades().entrySet()) {
            for (var sPlayer : island.getIslandMembers(true)) {
                var player = sPlayer.asPlayer();
                if (player != null) {
                    Bukkit.getPluginManager().callEvent(new PlayerUpgradeIslandEvent(player, upgrade.getKey(), upgrade.getValue()));
                }
            }
        }
    }
}
