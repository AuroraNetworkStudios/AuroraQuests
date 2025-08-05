package gg.auroramc.quests.objective;

import gg.auroramc.aurora.api.AuroraAPI;
import gg.auroramc.aurora.api.events.region.RegionBlockBreakEvent;
import gg.auroramc.quests.api.event.objective.PlayerLootEvent;
import gg.auroramc.quests.api.objective.ObjectiveDefinition;
import gg.auroramc.quests.api.objective.TypedObjective;
import gg.auroramc.quests.api.profile.Profile;
import gg.auroramc.quests.api.quest.Quest;
import org.bukkit.Material;
import org.bukkit.block.data.Ageable;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockDropItemEvent;
import org.bukkit.event.player.PlayerHarvestBlockEvent;

import java.util.List;
import java.util.Set;

public class FarmingObjective extends TypedObjective {

    private static final List<Material> crops = List.of(Material.WHEAT, Material.POTATOES, Material.CARROTS, Material.BEETROOTS, Material.COCOA, Material.NETHER_WART);

    private static final Set<Material> blockCrops = Set.of(Material.SUGAR_CANE, Material.CACTUS, Material.BAMBOO, Material.KELP_PLANT);

    public static final Set<Material> specialCrops = Set.of(Material.WARPED_FUNGUS, Material.CRIMSON_FUNGUS, Material.BROWN_MUSHROOM,
            Material.RED_MUSHROOM, Material.BROWN_MUSHROOM_BLOCK, Material.RED_MUSHROOM_BLOCK, Material.MUSHROOM_STEM, Material.MELON, Material.PUMPKIN);

    public FarmingObjective(Quest quest, ObjectiveDefinition definition, Profile.TaskDataWrapper data) {
        super(quest, definition, data);
    }


    @Override
    protected void activate() {
        onEvent(PlayerHarvestBlockEvent.class, this::onPlayerHarvest, EventPriority.MONITOR);
        onEvent(RegionBlockBreakEvent.class, this::onBlockBreak, EventPriority.MONITOR);
        onEvent(BlockDropItemEvent.class, this::onBlockDrop, EventPriority.MONITOR);
        onEvent(PlayerLootEvent.class, this::handleCustomLoot, EventPriority.MONITOR);

    }

    public void onPlayerHarvest(PlayerHarvestBlockEvent e) {
        // This event will only be called for right click harvestable crops
        for (var item : e.getItemsHarvested()) {
            var meta = meta(e.getHarvestedBlock().getLocation(), AuroraAPI.getItemManager().resolveId(item));
            progress(item.getAmount(), meta);
        }
    }

    public void onBlockBreak(RegionBlockBreakEvent e) {
        if (!e.isNatural()) return;
        if (e.getPlayerWhoBroke() != data.profile().getPlayer()) return;

        var block = e.getBlock();
        if (specialCrops.contains(e.getBlock().getType())) return;

        if (blockCrops.contains(block.getType())) {
            progress(1, meta(block.getLocation(), block.getType()));
        }
    }

    public void onBlockDrop(BlockDropItemEvent event) {
        if (event.getPlayer() != data.profile().getPlayer()) return;

        if (crops.contains(event.getBlockState().getType())) {
            if (event.getBlockState().getBlockData() instanceof Ageable ageable) {
                if (ageable.getAge() != ageable.getMaximumAge()) return;
                for (var drop : event.getItems()) {
                    var item = drop.getItemStack();
                    var meta = meta(event.getBlock().getLocation(), AuroraAPI.getItemManager().resolveId(item));
                    progress(item.getAmount(), meta);
                }
            }
            return;
        }

        if (AuroraAPI.getRegionManager().isPlacedBlock(event.getBlock())) return;

        if (specialCrops.contains(event.getBlockState().getType())) {
            for (var drop : event.getItems()) {
                var item = drop.getItemStack();
                var meta = meta(event.getBlock().getLocation(), AuroraAPI.getItemManager().resolveId(item));
                progress(item.getAmount(), meta);
            }
        }
    }

    public void handleCustomLoot(PlayerLootEvent event) {
        if (event.getSource() == PlayerLootEvent.Source.FARM || event.getSource() == PlayerLootEvent.Source.ALL) {
            progress(event.getAmount(), meta(event.getType()));
        }
    }
}
