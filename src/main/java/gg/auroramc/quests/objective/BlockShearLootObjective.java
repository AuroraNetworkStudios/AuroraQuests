package gg.auroramc.quests.objective;

import gg.auroramc.aurora.api.AuroraAPI;
import gg.auroramc.quests.api.objective.ObjectiveDefinition;
import gg.auroramc.quests.api.objective.TypedObjective;
import gg.auroramc.quests.api.profile.Profile;
import gg.auroramc.quests.api.quest.Quest;
import io.papermc.paper.event.block.PlayerShearBlockEvent;
import org.bukkit.event.EventPriority;

public class BlockShearLootObjective extends TypedObjective {

    public BlockShearLootObjective(Quest quest, ObjectiveDefinition definition, Profile.TaskDataWrapper data) {
        super(quest, definition, data);
    }

    @Override
    protected void activate() {
        onEvent(PlayerShearBlockEvent.class, this::handle, EventPriority.MONITOR);
    }

    public void handle(PlayerShearBlockEvent event) {
        for (var drop : event.getDrops()) {
            progress(drop.getAmount(), meta(AuroraAPI.getItemManager().resolveId(drop)));
        }
    }
}
