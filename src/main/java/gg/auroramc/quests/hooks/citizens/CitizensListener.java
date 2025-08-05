package gg.auroramc.quests.hooks.citizens;

import gg.auroramc.aurora.api.item.TypeId;
import gg.auroramc.quests.api.event.objective.PlayerInteractNpcEvent;
import net.citizensnpcs.api.event.NPCRightClickEvent;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class CitizensListener implements Listener {
    @EventHandler(ignoreCancelled = true)
    public void onNPCRightClick(NPCRightClickEvent e) {
        var player = e.getClicker();
        var npc = e.getNPC();

        var id = new TypeId("citizens", String.valueOf(npc.getId()));

        Bukkit.getPluginManager().callEvent(new PlayerInteractNpcEvent(player, id, PlayerInteractNpcEvent.InteractionType.RIGHT_CLICK));
    }
}
