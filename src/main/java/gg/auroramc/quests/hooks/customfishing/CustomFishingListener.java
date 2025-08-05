package gg.auroramc.quests.hooks.customfishing;

import gg.auroramc.aurora.api.AuroraAPI;
import gg.auroramc.quests.AuroraQuests;
import gg.auroramc.quests.api.event.objective.PlayerCaughtFishEvent;
import gg.auroramc.quests.api.objective.ObjectiveType;
import net.momirealms.customfishing.api.event.FishingLootSpawnEvent;
import net.momirealms.customfishing.api.mechanic.loot.LootType;
import org.bukkit.Bukkit;
import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.Map;


public class CustomFishingListener implements Listener {
    @EventHandler(ignoreCancelled = true)
    public void onFishLootSpawn(FishingLootSpawnEvent e) {
        if (e.getLoot().type() != LootType.ITEM) return;

        if (e.getEntity() instanceof Item item) {
            int quantity = item.getItemStack().getAmount();
            var id = AuroraAPI.getItemManager().resolveId(item.getItemStack());
            Bukkit.getPluginManager().callEvent(new PlayerCaughtFishEvent(e.getPlayer(), id, quantity, e.getLocation()));
        }
    }
}
