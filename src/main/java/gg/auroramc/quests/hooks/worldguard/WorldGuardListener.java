package gg.auroramc.quests.hooks.worldguard;

import gg.auroramc.aurora.api.events.region.PlayerRegionEnterEvent;
import gg.auroramc.aurora.api.item.TypeId;
import gg.auroramc.quests.api.event.objective.PlayerEnterRegionEvent;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.stream.Collectors;

public class WorldGuardListener implements Listener {
    @EventHandler
    public void onPlayerRegionEnter(PlayerRegionEnterEvent e) {
        // This event is built into AuroraLib already, so we don't need to do any funky stuff here.
        var regions = e.getRegions().stream().map(r -> new TypeId("wg", r.getId())).collect(Collectors.toSet());

        Bukkit.getPluginManager().callEvent(new PlayerEnterRegionEvent(e.getPlayer(), regions));
    }
}
