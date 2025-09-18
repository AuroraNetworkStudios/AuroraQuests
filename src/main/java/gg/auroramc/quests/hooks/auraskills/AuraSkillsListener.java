package gg.auroramc.quests.hooks.auraskills;

import dev.aurelium.auraskills.api.event.loot.LootDropEvent;
import dev.aurelium.auraskills.api.event.user.UserLoadEvent;
import gg.auroramc.aurora.api.AuroraAPI;
import gg.auroramc.quests.api.event.objective.PlayerCaughtFishEvent;
import gg.auroramc.quests.api.event.objective.PlayerLootEvent;
import gg.auroramc.quests.objective.FarmingObjective;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import java.util.concurrent.CompletableFuture;

public class AuraSkillsListener implements Listener {
    private final AuraSkillsHook hook;

    public AuraSkillsListener(AuraSkillsHook hook) {
        this.hook = hook;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onExtraDrop(LootDropEvent e) {
        var item = e.getItem();
        if (item.getType() == Material.AIR) return;
        var typeId = AuroraAPI.getItemManager().resolveId(item);


        // mushrooms are probably triggered by foraging luck would be from foraging luck
        if (FarmingObjective.specialCrops.contains(item.getType()) && typeId.namespace().equals("minecraft")) {
            Bukkit.getPluginManager().callEvent(new PlayerLootEvent(e.getPlayer(), typeId, item.getAmount(), PlayerLootEvent.Source.FARM));
            return;
        }

        switch (e.getCause()) {
            case FARMING_LUCK, FARMING_OTHER_LOOT ->
                    Bukkit.getPluginManager().callEvent(new PlayerLootEvent(e.getPlayer(), typeId, item.getAmount(), PlayerLootEvent.Source.FARM));
            // case FISHING_LUCK, TREASURE_HUNTER, EPIC_CATCH, FISHING_OTHER_LOOT ->
                    // Bukkit.getPluginManager().callEvent(new PlayerCaughtFishEvent(e.getPlayer(), typeId, item.getAmount(), e.getLocation()));
            case FORAGING_LUCK, FORAGING_OTHER_LOOT, MINING_LUCK, EXCAVATION_OTHER_LOOT, LUCKY_SPADES,
                 MINING_OTHER_LOOT, EXCAVATION_LUCK, METAL_DETECTOR ->
                    Bukkit.getPluginManager().callEvent(new PlayerLootEvent(e.getPlayer(), typeId, item.getAmount(), PlayerLootEvent.Source.BLOCK));
            case MOB_LOOT_TABLE ->
                    Bukkit.getPluginManager().callEvent(new PlayerLootEvent(e.getPlayer(), typeId, item.getAmount(), PlayerLootEvent.Source.ENTITY));
            case LUCK_DOUBLE_DROP ->
                    Bukkit.getPluginManager().callEvent(new PlayerLootEvent(e.getPlayer(), typeId, item.getAmount(), PlayerLootEvent.Source.ALL));

        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onUserLoad(UserLoadEvent event) {
        var player = Bukkit.getPlayer(event.getUser().getUuid());
        if (player == null) return;
        CompletableFuture.runAsync(() -> hook.getCorrector().correctRewardsWhenLoaded(player, false));
    }
}
