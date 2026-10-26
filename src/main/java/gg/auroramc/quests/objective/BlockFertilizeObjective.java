package gg.auroramc.quests.objective;

import gg.auroramc.quests.api.objective.ObjectiveDefinition;
import gg.auroramc.quests.api.objective.TypedObjective;
import gg.auroramc.quests.api.profile.Profile;
import gg.auroramc.quests.api.quest.Quest;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockFertilizeEvent;

public class BlockFertilizeObjective extends TypedObjective {

    public BlockFertilizeObjective(Quest quest, ObjectiveDefinition definition, Profile.TaskDataWrapper data) {
        super(quest, definition, data);
    }

    @Override
    protected void activate() {
        onEvent(BlockFertilizeEvent.class, this::handle, EventPriority.MONITOR);
    }

    public void handle(BlockFertilizeEvent event) {
        if (event.getPlayer() != data.profile().getPlayer()) return;
        var block = event.getBlock();
        progress(1, meta(block.getLocation(), block.getType()));
    }
}
