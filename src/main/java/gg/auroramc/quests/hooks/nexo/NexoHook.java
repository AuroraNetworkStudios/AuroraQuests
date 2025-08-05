package gg.auroramc.quests.hooks.nexo;

import com.nexomc.nexo.api.events.custom_block.chorusblock.NexoChorusBlockBreakEvent;
import com.nexomc.nexo.api.events.custom_block.chorusblock.NexoChorusBlockDropLootEvent;
import com.nexomc.nexo.api.events.custom_block.chorusblock.NexoChorusBlockPlaceEvent;
import com.nexomc.nexo.api.events.custom_block.noteblock.NexoNoteBlockBreakEvent;
import com.nexomc.nexo.api.events.custom_block.noteblock.NexoNoteBlockDropLootEvent;
import com.nexomc.nexo.api.events.custom_block.noteblock.NexoNoteBlockPlaceEvent;
import com.nexomc.nexo.api.events.custom_block.stringblock.NexoStringBlockBreakEvent;
import com.nexomc.nexo.api.events.custom_block.stringblock.NexoStringBlockDropLootEvent;
import com.nexomc.nexo.api.events.custom_block.stringblock.NexoStringBlockPlaceEvent;
import com.nexomc.nexo.api.events.furniture.NexoFurniturePlaceEvent;
import com.nexomc.nexo.utils.drops.DroppedLoot;
import gg.auroramc.aurora.api.AuroraAPI;
import gg.auroramc.aurora.api.item.TypeId;
import gg.auroramc.quests.AuroraQuests;
import gg.auroramc.quests.api.event.objective.PlayerBreakCustomBlockEvent;
import gg.auroramc.quests.api.event.objective.PlayerLootEvent;
import gg.auroramc.quests.api.event.objective.PlayerPlaceCustomBlockEvent;
import gg.auroramc.quests.hooks.Hook;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import java.util.List;

public class NexoHook implements Hook, Listener {

    @Override
    public void hook(AuroraQuests plugin) {
        AuroraQuests.logger().info("Hooked into Nexo for BLOCK_BREAK, BLOCK_PLACE, BLOCK_LOOT objectives.");
    }

    private boolean invalid(Player player, Block block) {
        return player == null || block == null || AuroraAPI.getRegionManager().isPlacedBlock(block);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onCustomBlockDrop(NexoStringBlockDropLootEvent e) {
        if (invalid(e.getPlayer(), e.getBlock())) return;
        handleLootProgression(e.getPlayer(), e.getLoots());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onCustomBlockDrop(NexoChorusBlockDropLootEvent e) {
        if (invalid(e.getPlayer(), e.getBlock())) return;
        handleLootProgression(e.getPlayer(), e.getLoots());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onCustomBlockDrop(NexoNoteBlockDropLootEvent e) {
        if (invalid(e.getPlayer(), e.getBlock())) return;
        handleLootProgression(e.getPlayer(), e.getLoots());
    }

    private void handleLootProgression(Player player, List<DroppedLoot> droppedLootList) {
        for (DroppedLoot droppedLoot : droppedLootList) {
            var itemStack = droppedLoot.loot().itemStack();
            var typeId = AuroraAPI.getItemManager().resolveId(itemStack);
            Bukkit.getPluginManager().callEvent(new PlayerLootEvent(player, typeId, droppedLoot.amount(), PlayerLootEvent.Source.BLOCK));
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onCustomBlockBreak(NexoStringBlockBreakEvent e) {
        handleBreakProgression(e.getPlayer(), e.getMechanic().getItemID(), e.getBlock());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onCustomBlockBreak(NexoChorusBlockBreakEvent e) {
        handleBreakProgression(e.getPlayer(), e.getMechanic().getItemID(), e.getBlock());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onCustomBlockBreak(NexoNoteBlockBreakEvent e) {
        handleBreakProgression(e.getPlayer(), e.getMechanic().getItemID(), e.getBlock());
    }

    private void handleBreakProgression(Player player, String blockId, Block block) {
        if (invalid(player, block)) return;
        var typeId = new TypeId("nexo", blockId);
        Bukkit.getPluginManager().callEvent(new PlayerBreakCustomBlockEvent(player, typeId, block));
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onCustomBlockPlace(NexoStringBlockPlaceEvent e) {
        handlePlaceProgression(e.getPlayer(), e.getMechanic().getItemID(), e.getBlock());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onCustomBlockPlace(NexoChorusBlockPlaceEvent e) {
        handlePlaceProgression(e.getPlayer(), e.getMechanic().getItemID(), e.getBlock());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onCustomBlockPlace(NexoNoteBlockPlaceEvent e) {
        handlePlaceProgression(e.getPlayer(), e.getMechanic().getItemID(), e.getBlock());
    }

    private void handlePlaceProgression(Player player, String blockId, Block block) {
        if (invalid(player, block)) return;
        var typeId = new TypeId("nexo", blockId);
        Bukkit.getPluginManager().callEvent(new PlayerPlaceCustomBlockEvent(player, typeId, block));
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onFurniturePlace(NexoFurniturePlaceEvent e) {
        handlePlaceProgression(e.getPlayer(), e.getMechanic().getItemID(), e.getBlock());
    }
}
