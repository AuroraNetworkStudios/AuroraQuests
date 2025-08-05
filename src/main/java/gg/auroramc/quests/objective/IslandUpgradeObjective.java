package gg.auroramc.quests.objective;

import gg.auroramc.quests.api.event.objective.PlayerUpgradeIslandEvent;
import gg.auroramc.quests.api.objective.ObjectiveDefinition;
import gg.auroramc.quests.api.objective.StringTypedObjective;
import gg.auroramc.quests.api.profile.Profile;
import gg.auroramc.quests.api.quest.Quest;
import org.bukkit.event.EventPriority;

public class IslandUpgradeObjective extends StringTypedObjective {

    public IslandUpgradeObjective(Quest quest, ObjectiveDefinition definition, Profile.TaskDataWrapper data) {
        super(quest, definition, data);
    }

    @Override
    protected void activate() {
        onEvent(PlayerUpgradeIslandEvent.class, this::handle, EventPriority.MONITOR);
    }

    public void handle(PlayerUpgradeIslandEvent event) {
        if (event.getValue() > data.getProgress()) {
            progress(event.getValue() - data.getProgress(), meta(event.getUpgrade()));
        }
    }
}
