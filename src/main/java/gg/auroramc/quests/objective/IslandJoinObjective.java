package gg.auroramc.quests.objective;

import gg.auroramc.quests.api.event.objective.PlayerJoinIslandEvent;
import gg.auroramc.quests.api.objective.Objective;
import gg.auroramc.quests.api.objective.ObjectiveDefinition;
import gg.auroramc.quests.api.profile.Profile;
import gg.auroramc.quests.api.quest.Quest;
import org.bukkit.event.EventPriority;

public class IslandJoinObjective extends Objective {

    public IslandJoinObjective(Quest quest, ObjectiveDefinition definition, Profile.TaskDataWrapper data) {
        super(quest, definition, data);
    }

    @Override
    protected void activate() {
        onEvent(PlayerJoinIslandEvent.class, this::handle, EventPriority.MONITOR);
    }

    public void handle(PlayerJoinIslandEvent event) {
        progress(1, meta());
    }
}
