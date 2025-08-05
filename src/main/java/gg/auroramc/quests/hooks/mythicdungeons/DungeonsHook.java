package gg.auroramc.quests.hooks.mythicdungeons;

import gg.auroramc.aurora.api.item.TypeId;
import gg.auroramc.quests.AuroraQuests;
import gg.auroramc.quests.api.event.objective.PlayerCompleteDungeonEvent;
import gg.auroramc.quests.hooks.Hook;
import net.playavalon.mythicdungeons.api.events.dungeon.PlayerFinishDungeonEvent;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class DungeonsHook implements Hook, Listener {
    private AuroraQuests plugin;

    @Override
    public void hook(AuroraQuests plugin) {
        this.plugin = plugin;
        AuroraQuests.logger().info("Hooked into MythicDungeons for COMPLETE_DUNGEON objective.");
    }

    @EventHandler
    public void onDungeonComplete(PlayerFinishDungeonEvent event) {
        var player = event.getPlayer();
        var dungeon = event.getDungeon();
        var instance = event.getInstance().asPlayInstance();

        var id = new TypeId("mythicdungeons", dungeon.getFolder().getName());
        var difficulty = instance != null && instance.getDifficulty() != null ? instance.getDifficulty().getNamespace() : null;

        Bukkit.getPluginManager().callEvent(new PlayerCompleteDungeonEvent(player, id, difficulty));
    }
}
