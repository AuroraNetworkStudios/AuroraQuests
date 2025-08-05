package gg.auroramc.quests.hooks.auroralevels.objective;

import gg.auroramc.levels.api.event.PlayerXpGainEvent;
import gg.auroramc.quests.api.objective.Objective;
import gg.auroramc.quests.api.objective.ObjectiveDefinition;
import gg.auroramc.quests.api.profile.Profile;
import gg.auroramc.quests.api.quest.Quest;
import org.bukkit.event.EventPriority;

public class GainAuroraXpObjective extends Objective {

    public GainAuroraXpObjective(Quest quest, ObjectiveDefinition definition, Profile.TaskDataWrapper data) {
        super(quest, definition, data);
    }

    @Override
    protected void activate() {
        onEvent(PlayerXpGainEvent.class, this::handle, EventPriority.MONITOR);
    }

    public void handle(PlayerXpGainEvent event) {
        progress(event.getXp(), meta());
    }
}
