package gg.auroramc.quests.objective;

import gg.auroramc.aurora.api.item.TypeId;
import gg.auroramc.quests.AuroraQuests;
import gg.auroramc.quests.api.objective.ObjectiveDefinition;
import gg.auroramc.quests.api.objective.ObjectiveType;
import gg.auroramc.quests.api.objective.TypedObjective;
import gg.auroramc.quests.api.profile.Profile;
import gg.auroramc.quests.api.quest.Quest;
import org.bukkit.Material;
import org.bukkit.entity.Cow;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Goat;
import org.bukkit.entity.Player;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerInteractEntityEvent;

import java.util.Map;

public class MilkingObjective extends TypedObjective {

    public MilkingObjective(Quest quest, ObjectiveDefinition definition, Profile.TaskDataWrapper data) {
        super(quest, definition, data);
    }

    @Override
    protected void activate() {
        onEvent(PlayerInteractEntityEvent.class, this::onMilk, EventPriority.MONITOR);
    }

    public void onMilk(PlayerInteractEntityEvent event) {
        if (!(event.getRightClicked() instanceof Cow || event.getRightClicked() instanceof Goat) || (event.getPlayer().getInventory().getItemInMainHand()).getType() != Material.BUCKET) {
            return;
        }

        progress(1, meta(event.getRightClicked() instanceof Cow ? TypeId.from(EntityType.COW) : TypeId.from(EntityType.GOAT)));
    }
}
