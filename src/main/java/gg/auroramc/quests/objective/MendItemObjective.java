package gg.auroramc.quests.objective;

import gg.auroramc.aurora.api.AuroraAPI;
import gg.auroramc.quests.api.objective.ObjectiveDefinition;
import gg.auroramc.quests.api.objective.TypedObjective;
import gg.auroramc.quests.api.profile.Profile;
import gg.auroramc.quests.api.quest.Quest;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerItemMendEvent;

public class MendItemObjective extends TypedObjective {

    public MendItemObjective(Quest quest, ObjectiveDefinition definition, Profile.TaskDataWrapper data) {
        super(quest, definition, data);
    }

    @Override
    protected void activate() {
        onEvent(PlayerItemMendEvent.class, this::handle, EventPriority.MONITOR);
    }

    public void handle(PlayerItemMendEvent event) {
        progress(event.getRepairAmount(), meta(AuroraAPI.getItemManager().resolveId(event.getItem())));
    }
}
