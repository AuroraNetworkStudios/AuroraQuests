package gg.auroramc.quests.objective;

import gg.auroramc.quests.api.objective.ObjectiveDefinition;
import gg.auroramc.quests.api.objective.TypedObjective;
import gg.auroramc.quests.api.profile.Profile;
import gg.auroramc.quests.api.quest.Quest;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerEggThrowEvent;

public class HatchObjective extends TypedObjective {

    public HatchObjective(Quest quest, ObjectiveDefinition definition, Profile.TaskDataWrapper data) {
        super(quest, definition, data);
    }

    @Override
    protected void activate() {
        onEvent(PlayerEggThrowEvent.class, this::handle, EventPriority.MONITOR);
    }

    public void handle(PlayerEggThrowEvent event) {
        if (!event.isHatching()) return;
        progress(event.getNumHatches(), meta(event.getEgg().getLocation(), event.getHatchingType()));
    }
}
