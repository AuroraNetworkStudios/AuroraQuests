package gg.auroramc.quests.objective;

import gg.auroramc.aurora.api.AuroraAPI;
import gg.auroramc.quests.api.objective.ObjectiveDefinition;
import gg.auroramc.quests.api.objective.TypedObjective;
import gg.auroramc.quests.api.profile.Profile;
import gg.auroramc.quests.api.quest.Quest;
import gg.auroramc.quests.util.InventoryUtils;
import org.bukkit.event.EventPriority;
import org.bukkit.event.inventory.SmithItemEvent;

public class SmithObjective extends TypedObjective {

    public SmithObjective(Quest quest, ObjectiveDefinition definition, Profile.TaskDataWrapper data) {
        super(quest, definition, data);
    }

    @Override
    protected void activate() {
        onEvent(SmithItemEvent.class, this::handle, EventPriority.MONITOR);
    }

    public void handle(SmithItemEvent event) {
        if (event.getWhoClicked() != data.profile().getPlayer()) return;
        var result = event.getCurrentItem();
        if (InventoryUtils.isEmptySlot(result)) return;

        int taken = InventoryUtils.calculateResultTakeAmount(event);
        if (taken <= 0) return;
        progress(taken, meta(AuroraAPI.getItemManager().resolveId(result)));
    }
}
