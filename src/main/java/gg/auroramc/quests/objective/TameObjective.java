package gg.auroramc.quests.objective;

import gg.auroramc.quests.api.objective.ObjectiveDefinition;
import gg.auroramc.quests.api.objective.TypedObjective;
import gg.auroramc.quests.api.profile.Profile;
import gg.auroramc.quests.api.quest.Quest;
import org.bukkit.entity.AnimalTamer;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityTameEvent;

public class TameObjective extends TypedObjective {

    public TameObjective(Quest quest, ObjectiveDefinition definition, Profile.TaskDataWrapper data) {
        super(quest, definition, data);
    }

    @Override
    protected void activate() {
        onEvent(EntityTameEvent.class, this::handle, EventPriority.MONITOR);
    }

    public void handle(EntityTameEvent event) {
        AnimalTamer tamer = event.getOwner();
        if (tamer != data.profile().getPlayer()) return;
        progress(1, meta(event.getEntity().getType()));
    }
}
