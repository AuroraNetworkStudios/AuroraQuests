package gg.auroramc.quests.objective;

import gg.auroramc.quests.api.objective.ObjectiveDefinition;
import gg.auroramc.quests.api.objective.TypedObjective;
import gg.auroramc.quests.api.profile.Profile;
import gg.auroramc.quests.api.quest.Quest;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerShearEntityEvent;

public class ShearObjective extends TypedObjective {

    public ShearObjective(Quest quest, ObjectiveDefinition definition, Profile.TaskDataWrapper data) {
        super(quest, definition, data);
    }

    @Override
    protected void activate() {
        onEvent(PlayerShearEntityEvent.class, this::handle, EventPriority.MONITOR);
    }

    public void handle(PlayerShearEntityEvent event) {
        progress(1, meta(event.getEntity().getType()));
    }
}
