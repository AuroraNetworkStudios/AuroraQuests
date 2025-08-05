package gg.auroramc.quests.hooks.auroralevels.objective;

import gg.auroramc.levels.api.event.PlayerLevelUpEvent;
import gg.auroramc.quests.api.objective.Objective;
import gg.auroramc.quests.api.objective.ObjectiveDefinition;
import gg.auroramc.quests.api.profile.Profile;
import gg.auroramc.quests.api.quest.Quest;
import org.bukkit.event.EventPriority;

public class GainAuroraLevelObjective extends Objective {

    public GainAuroraLevelObjective(Quest quest, ObjectiveDefinition definition, Profile.TaskDataWrapper data) {
        super(quest, definition, data);
    }

    @Override
    protected void activate() {
        onEvent(PlayerLevelUpEvent.class, this::handle, EventPriority.MONITOR);
    }

    public void handle(PlayerLevelUpEvent event) {
        progress(1, meta());
    }
}
