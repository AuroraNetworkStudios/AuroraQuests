package gg.auroramc.quests.objective;

import gg.auroramc.quests.api.objective.Objective;
import gg.auroramc.quests.api.objective.ObjectiveDefinition;
import gg.auroramc.quests.api.profile.Profile;
import gg.auroramc.quests.api.quest.Quest;
import org.bukkit.entity.ZombieVillager;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityTransformEvent;

public class CureObjective extends Objective {

    public CureObjective(Quest quest, ObjectiveDefinition definition, Profile.TaskDataWrapper data) {
        super(quest, definition, data);
    }

    @Override
    protected void activate() {
        onEvent(EntityTransformEvent.class, this::handle, EventPriority.MONITOR);
    }

    public void handle(EntityTransformEvent event) {
        if (event.getTransformReason() != EntityTransformEvent.TransformReason.CURED) return;
        if (!(event.getEntity() instanceof ZombieVillager zombie)) return;

        var curer = zombie.getConversionPlayer();
        if (curer == null || !curer.getUniqueId().equals(data.profile().getPlayer().getUniqueId())) return;

        progress(1, meta(zombie.getLocation()));
    }
}
