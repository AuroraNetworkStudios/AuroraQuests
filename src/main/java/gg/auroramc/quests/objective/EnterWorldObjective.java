package gg.auroramc.quests.objective;

import gg.auroramc.quests.api.objective.ObjectiveDefinition;
import gg.auroramc.quests.api.objective.StringTypedObjective;
import gg.auroramc.quests.api.profile.Profile;
import gg.auroramc.quests.api.quest.Quest;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerChangedWorldEvent;

public class EnterWorldObjective extends StringTypedObjective {

    public EnterWorldObjective(Quest quest, ObjectiveDefinition definition, Profile.TaskDataWrapper data) {
        super(quest, definition, data);
    }

    @Override
    protected void activate() {
        onEvent(PlayerChangedWorldEvent.class, this::handle, EventPriority.MONITOR);
    }

    public void handle(PlayerChangedWorldEvent event) {
        progress(1, meta(event.getPlayer().getWorld().getName()));
    }
}
