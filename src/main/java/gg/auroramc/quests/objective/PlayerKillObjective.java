package gg.auroramc.quests.objective;

import gg.auroramc.quests.api.objective.ObjectiveDefinition;
import gg.auroramc.quests.api.objective.StringTypedObjective;
import gg.auroramc.quests.api.profile.Profile;
import gg.auroramc.quests.api.quest.Quest;
import org.bukkit.entity.Player;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.PlayerDeathEvent;

public class PlayerKillObjective extends StringTypedObjective {

    public PlayerKillObjective(Quest quest, ObjectiveDefinition definition, Profile.TaskDataWrapper data) {
        super(quest, definition, data);
    }

    @Override
    protected void activate() {
        onEvent(PlayerDeathEvent.class, this::handle, EventPriority.MONITOR);
    }

    public void handle(PlayerDeathEvent event) {
        if (event.getEntity().getKiller() != data.profile().getPlayer()) return;
        Player victim = event.getEntity();

        if (victim == data.profile().getPlayer()) {
            return;
        }

        progress(1, meta(victim.getName()));
    }
}
