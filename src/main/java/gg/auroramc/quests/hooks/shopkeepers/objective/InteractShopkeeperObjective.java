package gg.auroramc.quests.hooks.shopkeepers.objective;

import com.nisovin.shopkeepers.api.events.ShopkeeperOpenUIEvent;
import gg.auroramc.quests.api.objective.ObjectiveDefinition;
import gg.auroramc.quests.api.objective.StringTypedObjective;
import gg.auroramc.quests.api.profile.Profile;
import gg.auroramc.quests.api.quest.Quest;
import org.bukkit.event.EventPriority;

public class InteractShopkeeperObjective extends StringTypedObjective {

    public InteractShopkeeperObjective(Quest quest, ObjectiveDefinition definition, Profile.TaskDataWrapper data) {
        super(quest, definition, data);
    }

    @Override
    protected void activate() {
        onEvent(ShopkeeperOpenUIEvent.class, this::handle, EventPriority.MONITOR);
    }

    public void handle(ShopkeeperOpenUIEvent event) {
        if (event.getPlayer() != data.profile().getPlayer()) return;

        progress(1, meta(String.valueOf(event.getShopkeeper().getId())));
    }
}
