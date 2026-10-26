package gg.auroramc.quests.objective;

import gg.auroramc.quests.api.objective.ObjectiveDefinition;
import gg.auroramc.quests.api.objective.TypedObjective;
import gg.auroramc.quests.api.profile.Profile;
import gg.auroramc.quests.api.quest.Quest;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityChangeBlockEvent;

public class BlockChangeObjective extends TypedObjective {

    public BlockChangeObjective(Quest quest, ObjectiveDefinition definition, Profile.TaskDataWrapper data) {
        super(quest, definition, data);
    }

    @Override
    protected void activate() {
        onEvent(EntityChangeBlockEvent.class, this::handle, EventPriority.MONITOR);
    }

    public void handle(EntityChangeBlockEvent event) {
        if (event.getEntity() != data.profile().getPlayer()) return;
        progress(1, meta(event.getBlock().getLocation(), event.getTo()));
    }
}
