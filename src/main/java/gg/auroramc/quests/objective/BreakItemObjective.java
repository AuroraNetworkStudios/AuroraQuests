package gg.auroramc.quests.objective;

import gg.auroramc.aurora.api.AuroraAPI;
import gg.auroramc.quests.api.objective.ObjectiveDefinition;
import gg.auroramc.quests.api.objective.TypedObjective;
import gg.auroramc.quests.api.profile.Profile;
import gg.auroramc.quests.api.quest.Quest;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerItemBreakEvent;

public class BreakItemObjective extends TypedObjective {

    public BreakItemObjective(Quest quest, ObjectiveDefinition definition, Profile.TaskDataWrapper data) {
        super(quest, definition, data);
    }

    @Override
    protected void activate() {
        onEvent(PlayerItemBreakEvent.class, this::handle, EventPriority.MONITOR);
    }

    public void handle(PlayerItemBreakEvent event) {
        progress(1, meta(AuroraAPI.getItemManager().resolveId(event.getBrokenItem())));
    }
}
