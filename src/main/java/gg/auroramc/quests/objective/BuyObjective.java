package gg.auroramc.quests.objective;

import gg.auroramc.quests.api.event.objective.PlayerPurchaseItemEvent;
import gg.auroramc.quests.api.objective.ObjectiveDefinition;
import gg.auroramc.quests.api.objective.TypedObjective;
import gg.auroramc.quests.api.profile.Profile;
import gg.auroramc.quests.api.quest.Quest;
import org.bukkit.event.EventPriority;

public class BuyObjective extends TypedObjective {

    public BuyObjective(Quest quest, ObjectiveDefinition definition, Profile.TaskDataWrapper data) {
        super(quest, definition, data);
    }

    @Override
    protected void activate() {
        onEvent(PlayerPurchaseItemEvent.class, this::handle, EventPriority.MONITOR);
    }

    public void handle(PlayerPurchaseItemEvent event) {
        progress(event.getItem().amount(), meta(event.getItem().id()));

    }
}
