package gg.auroramc.quests.listener;

import gg.auroramc.aurora.api.item.TypeId;
import gg.auroramc.quests.AuroraQuests;
import gg.auroramc.quests.api.quest.TaskType;
import io.papermc.paper.event.entity.EntityFertilizeEggEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import java.util.Map;

public class BreedingEggListener implements Listener {

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntityFertilizeEgg(EntityFertilizeEggEvent event) {
        Player player = event.getBreeder();
        if (player != null) {
            AuroraQuests.getInstance().getQuestManager()
                    .progress(player, TaskType.BREED, 1, Map.of("type", TypeId.from(event.getEntity().getType())));
        }
    }
}
