package gg.auroramc.quests.objective;

import gg.auroramc.quests.api.objective.ObjectiveDefinition;
import gg.auroramc.quests.api.objective.TypedObjective;
import gg.auroramc.quests.api.profile.Profile;
import gg.auroramc.quests.api.quest.Quest;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.inventory.EquipmentSlot;

public class InteractEntityObjective extends TypedObjective {

    public InteractEntityObjective(Quest quest, ObjectiveDefinition definition, Profile.TaskDataWrapper data) {
        super(quest, definition, data);
    }

    @Override
    protected void activate() {
        onEvent(PlayerInteractAtEntityEvent.class, this::handle, EventPriority.MONITOR);
    }

    public void handle(PlayerInteractAtEntityEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) return;
        var entity = event.getRightClicked();
        progress(1, meta(entity.getLocation(), entity.getType()));
    }
}
