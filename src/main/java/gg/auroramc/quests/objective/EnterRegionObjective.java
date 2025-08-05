package gg.auroramc.quests.objective;

import gg.auroramc.quests.api.event.objective.PlayerEnterRegionEvent;
import gg.auroramc.quests.api.objective.ObjectiveDefinition;
import gg.auroramc.quests.api.objective.TypedObjective;
import gg.auroramc.quests.api.profile.Profile;
import gg.auroramc.quests.api.quest.Quest;
import org.bukkit.event.EventPriority;

public class EnterRegionObjective extends TypedObjective {

    public EnterRegionObjective(Quest quest, ObjectiveDefinition definition, Profile.TaskDataWrapper data) {
        super(quest, definition, data);
    }

    @Override
    protected void activate() {
        onEvent(PlayerEnterRegionEvent.class, this::handle, EventPriority.MONITOR);
    }

    public void handle(PlayerEnterRegionEvent event) {
        for (var region : event.getRegions()) {
            progress(1, meta(region));
        }
    }
}
