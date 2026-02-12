package gg.auroramc.quests.objective;

import gg.auroramc.quests.api.objective.Objective;
import gg.auroramc.quests.api.objective.ObjectiveDefinition;
import gg.auroramc.quests.api.profile.Profile;
import gg.auroramc.quests.api.quest.Quest;

public class PlayTimeObjective extends Objective {

    public PlayTimeObjective(final Quest quest, final ObjectiveDefinition definition, final Profile.TaskDataWrapper data) {
        super(quest, definition, data);
    }

    @Override
    protected void activate() {
        asyncInterval(() -> progress(1, meta()), 1200, 1200);
    }

}
