package gg.auroramc.quests.objective;

import gg.auroramc.aurora.api.AuroraAPI;
import gg.auroramc.quests.api.objective.ObjectiveDefinition;
import gg.auroramc.quests.api.objective.TypedObjective;
import gg.auroramc.quests.api.profile.Profile;
import gg.auroramc.quests.api.quest.Quest;
import io.papermc.paper.event.entity.EntityCompostItemEvent;
import org.bukkit.event.EventPriority;

public class CompostObjective extends TypedObjective {

    public CompostObjective(Quest quest, ObjectiveDefinition definition, Profile.TaskDataWrapper data) {
        super(quest, definition, data);
    }

    @Override
    protected void activate() {
        onEvent(EntityCompostItemEvent.class, this::handle, EventPriority.MONITOR);
    }

    public void handle(EntityCompostItemEvent event) {
        if (event.getEntity() != data.profile().getPlayer()) return;
        progress(1, meta(event.getBlock().getLocation(), AuroraAPI.getItemManager().resolveId(event.getItem())));
    }
}
