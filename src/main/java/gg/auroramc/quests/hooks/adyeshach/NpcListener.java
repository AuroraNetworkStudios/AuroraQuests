package gg.auroramc.quests.hooks.adyeshach;

import gg.auroramc.aurora.api.item.TypeId;
import gg.auroramc.quests.api.event.objective.PlayerInteractNpcEvent;
import ink.ptms.adyeshach.core.event.AdyeshachEntityInteractEvent;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class NpcListener implements Listener {
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onInteractNPC(AdyeshachEntityInteractEvent event) {
        var npcID = event.getEntity().getId();
        var id = new TypeId("adyeshach", npcID);

        Bukkit.getPluginManager().callEvent(new PlayerInteractNpcEvent(event.getPlayer(), id));
    }
}
