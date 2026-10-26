package gg.auroramc.quests.objective;

import gg.auroramc.aurora.api.AuroraAPI;
import gg.auroramc.quests.api.objective.ObjectiveDefinition;
import gg.auroramc.quests.api.objective.TypedObjective;
import gg.auroramc.quests.api.profile.Profile;
import gg.auroramc.quests.api.quest.Quest;
import gg.auroramc.quests.util.InventoryUtils;
import org.bukkit.entity.Player;
import org.bukkit.event.EventPriority;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;

public class SmeltObjective extends TypedObjective {

    public SmeltObjective(Quest quest, ObjectiveDefinition definition, Profile.TaskDataWrapper data) {
        super(quest, definition, data);
    }

    @Override
    protected void activate() {
        onEvent(InventoryClickEvent.class, this::handle, EventPriority.MONITOR);
    }

    public void handle(InventoryClickEvent event) {
        InventoryType inventoryType = event.getInventory().getType();
        if (event.getWhoClicked() == data.profile().getPlayer() && isSmeltingResultExtraction(event, inventoryType)) {
            int taken = InventoryUtils.calculateResultTakeAmount(event);
            ItemStack item = event.getCurrentItem();
            var id = AuroraAPI.getItemManager().resolveId(item);
            progress(taken, meta(id));
        }
    }

    private static boolean isSmeltingResultExtraction(final InventoryClickEvent event, final InventoryType inventoryType) {
        return (inventoryType == InventoryType.FURNACE
                || inventoryType == InventoryType.SMOKER
                || inventoryType == InventoryType.BLAST_FURNACE)
                && event.getWhoClicked() instanceof Player
                && event.getRawSlot() == 2
                && !InventoryUtils.isEmptySlot(event.getCurrentItem());
    }
}
