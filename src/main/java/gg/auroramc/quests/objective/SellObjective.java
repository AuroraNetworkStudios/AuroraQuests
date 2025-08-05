package gg.auroramc.quests.objective;

import gg.auroramc.quests.api.event.objective.PlayerSellItemEvent;
import gg.auroramc.quests.api.objective.ObjectiveDefinition;
import gg.auroramc.quests.api.objective.TypedObjective;
import gg.auroramc.quests.api.profile.Profile;
import gg.auroramc.quests.api.quest.Quest;
import org.bukkit.event.EventPriority;

public class SellObjective extends TypedObjective {

    public SellObjective(Quest quest, ObjectiveDefinition definition, Profile.TaskDataWrapper data) {
        super(quest, definition, data);
    }

    @Override
    protected void activate() {
        onEvent(PlayerSellItemEvent.class, this::handle, EventPriority.MONITOR);
    }

    public void handle(PlayerSellItemEvent event) {
        progress(event.getItem().amount(), meta(event.getItem().id()));
    }
}
