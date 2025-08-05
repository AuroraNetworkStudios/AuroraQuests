package gg.auroramc.quests.objective;

import gg.auroramc.quests.api.event.objective.PlayerIslandLevelChangeEvent;
import gg.auroramc.quests.api.objective.Objective;
import gg.auroramc.quests.api.objective.ObjectiveDefinition;
import gg.auroramc.quests.api.profile.Profile;
import gg.auroramc.quests.api.quest.Quest;
import org.bukkit.event.EventPriority;

public class IslandLevelObjective extends Objective {

    public IslandLevelObjective(Quest quest, ObjectiveDefinition definition, Profile.TaskDataWrapper data) {
        super(quest, definition, data);
    }

    @Override
    protected void activate() {
        onEvent(PlayerIslandLevelChangeEvent.class, this::handle, EventPriority.MONITOR);
    }

    public void handle(PlayerIslandLevelChangeEvent event) {
        setProgress(event.getLevel());
    }
}
