package gg.auroramc.quests.objective;

import gg.auroramc.quests.api.event.objective.PlayerIslandWorthChangeEvent;
import gg.auroramc.quests.api.objective.Objective;
import gg.auroramc.quests.api.objective.ObjectiveDefinition;
import gg.auroramc.quests.api.profile.Profile;
import gg.auroramc.quests.api.quest.Quest;
import org.bukkit.event.EventPriority;

public class IslandWorthObjective extends Objective {

    public IslandWorthObjective(Quest quest, ObjectiveDefinition definition, Profile.TaskDataWrapper data) {
        super(quest, definition, data);
    }

    @Override
    protected void activate() {
        onEvent(PlayerIslandWorthChangeEvent.class, this::handle, EventPriority.MONITOR);
    }

    public void handle(PlayerIslandWorthChangeEvent event) {
        setProgress(event.getWorth());
    }
}
