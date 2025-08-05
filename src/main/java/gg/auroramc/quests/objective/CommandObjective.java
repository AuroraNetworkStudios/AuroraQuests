package gg.auroramc.quests.objective;

import gg.auroramc.quests.api.objective.ObjectiveDefinition;
import gg.auroramc.quests.api.objective.StringTypedObjective;
import gg.auroramc.quests.api.profile.Profile;
import gg.auroramc.quests.api.quest.Quest;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

public class CommandObjective extends StringTypedObjective {

    public CommandObjective(Quest quest, ObjectiveDefinition definition, Profile.TaskDataWrapper data) {
        super(quest, definition, data);
    }

    @Override
    protected void activate() {
        onEvent(PlayerCommandPreprocessEvent.class, this::handle, EventPriority.LOWEST, false);
    }

    public void handle(PlayerCommandPreprocessEvent event) {
        String message = event.getMessage();
        if (!message.isEmpty()) {
            message = message.substring(1);
        }

        progress(1, meta(message));
    }
}
