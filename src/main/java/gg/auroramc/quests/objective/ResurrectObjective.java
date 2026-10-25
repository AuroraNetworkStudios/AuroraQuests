package gg.auroramc.quests.objective;

import gg.auroramc.quests.api.objective.Objective;
import gg.auroramc.quests.api.objective.ObjectiveDefinition;
import gg.auroramc.quests.api.profile.Profile;
import gg.auroramc.quests.api.quest.Quest;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityResurrectEvent;

public class ResurrectObjective extends Objective {

    public ResurrectObjective(Quest quest, ObjectiveDefinition definition, Profile.TaskDataWrapper data) {
        super(quest, definition, data);
    }

    @Override
    protected void activate() {
        // Fired pre-cancelled when no totem is held, which ignoreCancelled filters out
        onEvent(EntityResurrectEvent.class, this::handle, EventPriority.MONITOR);
    }

    public void handle(EntityResurrectEvent event) {
        if (event.getEntity() != data.profile().getPlayer()) return;
        progress(1, meta());
    }
}
