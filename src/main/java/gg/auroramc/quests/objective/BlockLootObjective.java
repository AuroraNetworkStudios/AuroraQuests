package gg.auroramc.quests.objective;

import gg.auroramc.aurora.api.AuroraAPI;
import gg.auroramc.aurora.api.events.region.RegionBlockBreakEvent;
import gg.auroramc.quests.api.event.objective.PlayerLootEvent;
import gg.auroramc.quests.api.objective.ObjectiveDefinition;
import gg.auroramc.quests.api.objective.TypedObjective;
import gg.auroramc.quests.api.profile.Profile;
import gg.auroramc.quests.api.quest.Quest;
import gg.auroramc.quests.util.AnnoyingPluginUtil;
import org.bukkit.Material;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockDropItemEvent;

import java.util.Set;

public class BlockLootObjective extends TypedObjective {

    private static final Set<Material> blacklist = Set.of(
            Material.CHEST, Material.TRAPPED_CHEST, Material.BARREL,
            Material.HOPPER, Material.FURNACE, Material.SMOKER, Material.BLAST_FURNACE,
            Material.DISPENSER, Material.ITEM_FRAME, Material.BEACON,
            Material.DROPPER, Material.ARMOR_STAND, Material.BREWING_STAND,
            Material.CAMPFIRE, Material.SOUL_CAMPFIRE, Material.FLOWER_POT,
            Material.JUKEBOX, Material.LOOM, Material.CARTOGRAPHY_TABLE,
            Material.DECORATED_POT
    );

    public BlockLootObjective(Quest quest, ObjectiveDefinition definition, Profile.TaskDataWrapper data) {
        super(quest, definition, data);
    }

    @Override
    protected void activate() {
        onEvent(BlockDropItemEvent.class, this::onBlockDrop, AnnoyingPluginUtil.getBlockDropItemPriority());
        onEvent(PlayerLootEvent.class, this::handleCustomLoot, EventPriority.MONITOR);
    }

    public void onBlockDrop(BlockDropItemEvent e) {
        if (AuroraAPI.getRegionManager().isPlacedBlock(e.getBlock())) return;
        if (e.getPlayer() != data.profile().getPlayer()) return;

        if (blacklist.contains(e.getBlockState().getType())) return;

        for (var drop : e.getItems()) {
            var item = drop.getItemStack();
            var id = AuroraAPI.getItemManager().resolveId(item);
            progress(item.getAmount(), meta(e.getBlock().getLocation(), id));
        }
    }

    public void handleCustomLoot(PlayerLootEvent event) {
        if (event.getSource() == PlayerLootEvent.Source.BLOCK || event.getSource() == PlayerLootEvent.Source.ALL) {
            progress(event.getAmount(), meta(event.getType()));
        }
    }
}
