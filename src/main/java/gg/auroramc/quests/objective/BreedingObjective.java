package gg.auroramc.quests.objective;

import gg.auroramc.quests.api.objective.ObjectiveDefinition;
import gg.auroramc.quests.api.objective.TypedObjective;
import gg.auroramc.quests.api.profile.Profile;
import gg.auroramc.quests.api.quest.Quest;
import io.papermc.paper.event.entity.EntityFertilizeEggEvent;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityBreedEvent;

public class BreedingObjective extends TypedObjective {

    public BreedingObjective(Quest quest, ObjectiveDefinition definition, Profile.TaskDataWrapper data) {
        super(quest, definition, data);
    }

    @Override
    protected void activate() {
        onEvent(EntityFertilizeEggEvent.class, this::onBreedEgg, EventPriority.MONITOR);
        onEvent(EntityBreedEvent.class, this::onBreed, EventPriority.MONITOR);
    }

    public void onBreedEgg(EntityFertilizeEggEvent event) {
        var player = event.getBreeder();

        if (player == data.profile().getPlayer()) {
            progress(1, meta(event.getEntity().getType()));
        }
    }

    public void onBreed(EntityBreedEvent event) {
        LivingEntity breeder = event.getBreeder();
        if (breeder instanceof Player player && player == data.profile().getPlayer()) {
            progress(1, meta(event.getEntity().getType()));
        }
    }
}
