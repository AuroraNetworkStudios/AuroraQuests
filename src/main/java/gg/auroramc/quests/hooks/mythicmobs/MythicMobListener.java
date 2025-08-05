package gg.auroramc.quests.hooks.mythicmobs;

import gg.auroramc.aurora.api.AuroraAPI;
import gg.auroramc.aurora.api.item.TypeId;
import gg.auroramc.quests.api.event.objective.PlayerKillMobEvent;
import gg.auroramc.quests.api.event.objective.PlayerLootEvent;
import io.lumine.mythic.bukkit.events.MythicMobDeathEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class MythicMobListener implements Listener {
    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onMythicMobDeath(MythicMobDeathEvent e) {
        if (!(e.getKiller() instanceof Player player)) return;

        var mobName = e.getMob().getType().getInternalName();
        var drops = e.getDrops();
        var typeId = new TypeId("mythicmobs", mobName);

        if (e.getMob().getLevel() > 0) {
            Bukkit.getPluginManager().callEvent(new PlayerKillMobEvent(player, typeId, 1, e.getMob().getLevel()));
        }

        Bukkit.getPluginManager().callEvent(new PlayerKillMobEvent(player, typeId, 1));

        for (var drop : drops) {
            var dropId = AuroraAPI.getItemManager().resolveId(drop);
            Bukkit.getPluginManager().callEvent(new PlayerLootEvent(player, dropId, drop.getAmount(), PlayerLootEvent.Source.ENTITY));
        }
    }
}
