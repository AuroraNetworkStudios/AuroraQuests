package gg.auroramc.quests.objective;

import gg.auroramc.quests.api.objective.ObjectiveDefinition;
import gg.auroramc.quests.api.objective.TypedObjective;
import gg.auroramc.quests.api.profile.Profile;
import gg.auroramc.quests.api.quest.Quest;
import org.bukkit.block.BrushableBlock;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockDropItemEvent;

public final class BlockBrushObjective extends TypedObjective {

    public BlockBrushObjective(final Quest quest, final ObjectiveDefinition definition, final Profile.TaskDataWrapper data) {
        super(quest, definition, data);
    }

    @Override
    protected void activate() {
        onEvent(BlockDropItemEvent.class, this::onBlockBrush, EventPriority.MONITOR);
    }

    public void onBlockBrush(final BlockDropItemEvent e) {
        if (e.getPlayer() != data.profile().getPlayer()) return;

        // Brushable blocks drop nothing when broken and (by default) always generate an item when brushed. These two checks should be enough.
        if (e.getBlockState() instanceof BrushableBlock && !e.getItems().isEmpty())
            progress(1, meta(e.getBlock().getLocation(), e.getBlock().getType()));
    }

}
