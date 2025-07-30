package gg.auroramc.quests.hooks.znpcsplus;

import gg.auroramc.aurora.api.item.TypeId;
import gg.auroramc.quests.api.event.objective.PlayerInteractNpcEvent;
import lol.pyr.znpcsplus.api.event.NpcInteractEvent;
import lol.pyr.znpcsplus.api.interaction.InteractionType;
import lol.pyr.znpcsplus.api.npc.NpcEntry;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class ZnpcPlusListener implements Listener {
    @EventHandler(ignoreCancelled = true)
    public void onNPCRightClick(NpcInteractEvent e) {

        NpcEntry npc = e.getEntry();
        Player player = e.getPlayer();

        PlayerInteractNpcEvent.InteractionType interactionType = e.getClickType() == InteractionType.RIGHT_CLICK
                ? PlayerInteractNpcEvent.InteractionType.RIGHT_CLICK : e.getClickType() == InteractionType.LEFT_CLICK
                ? PlayerInteractNpcEvent.InteractionType.LEFT_CLICK : PlayerInteractNpcEvent.InteractionType.UNKNOWN;

        var id = new TypeId("znpcsplus", String.valueOf(npc.getId()));

        Bukkit.getPluginManager().callEvent(new PlayerInteractNpcEvent(player, id, interactionType));
    }
}
