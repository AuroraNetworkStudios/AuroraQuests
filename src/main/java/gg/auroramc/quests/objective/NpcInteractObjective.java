package gg.auroramc.quests.objective;

import gg.auroramc.quests.api.event.objective.PlayerInteractNpcEvent;
import gg.auroramc.quests.api.objective.ObjectiveDefinition;
import gg.auroramc.quests.api.objective.TypedObjective;
import gg.auroramc.quests.api.profile.Profile;
import gg.auroramc.quests.api.quest.Quest;
import org.bukkit.event.EventPriority;

public class NpcInteractObjective extends TypedObjective {

    public NpcInteractObjective(Quest quest, ObjectiveDefinition definition, Profile.TaskDataWrapper data) {
        super(quest, definition, data);
    }

    @Override
    protected void activate() {
        onEvent(PlayerInteractNpcEvent.class, this::handle, EventPriority.MONITOR);
    }

    public void handle(PlayerInteractNpcEvent event) {
        progress(1, meta(event.getNpc()));
    }
}
