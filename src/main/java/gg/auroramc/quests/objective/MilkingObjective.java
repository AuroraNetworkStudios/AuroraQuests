package gg.auroramc.quests.objective;

import gg.auroramc.aurora.api.item.TypeId;
import gg.auroramc.quests.api.objective.ObjectiveDefinition;
import gg.auroramc.quests.api.objective.TypedObjective;
import gg.auroramc.quests.api.profile.Profile;
import gg.auroramc.quests.api.quest.Quest;
import org.bukkit.Material;
import org.bukkit.entity.Cow;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Goat;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;

public class MilkingObjective extends TypedObjective {

    public MilkingObjective(Quest quest, ObjectiveDefinition definition, Profile.TaskDataWrapper data) {
        super(quest, definition, data);
    }

    @Override
    protected void activate() {
        onEvent(PlayerInteractAtEntityEvent.class, this::onMilk, EventPriority.MONITOR);
    }

    public void onMilk(PlayerInteractAtEntityEvent event) {
        var entity = event.getRightClicked();
        var itemInHand = event.getPlayer().getInventory().getItemInMainHand();

        if (itemInHand.getType() != Material.BUCKET) {
            return;
        }

        if (entity instanceof Cow) {
            progress(1, meta(TypeId.from(EntityType.COW)));
        } else if (entity instanceof Goat) {
            progress(1, meta(TypeId.from(EntityType.GOAT)));
        }
    }
}
