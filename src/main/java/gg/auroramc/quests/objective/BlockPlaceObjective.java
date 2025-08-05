package gg.auroramc.quests.objective;

import gg.auroramc.quests.api.event.objective.PlayerPlaceCustomBlockEvent;
import gg.auroramc.quests.api.objective.ObjectiveDefinition;
import gg.auroramc.quests.api.objective.TypedObjective;
import gg.auroramc.quests.api.profile.Profile;
import gg.auroramc.quests.api.quest.Quest;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockPlaceEvent;

public class BlockPlaceObjective extends TypedObjective {

    public BlockPlaceObjective(Quest quest, ObjectiveDefinition definition, Profile.TaskDataWrapper data) {
        super(quest, definition, data);
    }

    @Override
    protected void activate() {
        onEvent(BlockPlaceEvent.class, this::onBlockPlace, EventPriority.MONITOR);
        onEvent(PlayerPlaceCustomBlockEvent.class, this::onBlockPlace, EventPriority.MONITOR);
    }

    public void onBlockPlace(BlockPlaceEvent event) {
        var player = event.getPlayer();
        if (player != data.profile().getPlayer()) return;

        progress(1, meta(event.getBlock().getType()));
    }

    public void onBlockPlace(PlayerPlaceCustomBlockEvent event) {
        progress(1, meta(event.getBlock().getLocation(), event.getType()));
    }
}
