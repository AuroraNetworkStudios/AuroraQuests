package gg.auroramc.quests.objective;

import gg.auroramc.aurora.api.AuroraAPI;
import gg.auroramc.quests.api.event.objective.PlayerCraftedItemEvent;
import gg.auroramc.quests.api.objective.ObjectiveDefinition;
import gg.auroramc.quests.api.objective.TypedObjective;
import gg.auroramc.quests.api.profile.Profile;
import gg.auroramc.quests.api.quest.Quest;
import gg.auroramc.quests.util.InventoryUtils;
import org.bukkit.entity.Player;
import org.bukkit.event.EventPriority;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

public class CraftObjective extends TypedObjective {

    public CraftObjective(Quest quest, ObjectiveDefinition definition, Profile.TaskDataWrapper data) {
        super(quest, definition, data);
    }

    @Override
    protected void activate() {
        onEvent(CraftItemEvent.class, this::onCraftItem, EventPriority.MONITOR);
        onEvent(PlayerCraftedItemEvent.class, this::onCraftItem, EventPriority.MONITOR);
    }

    public void onCraftItem(CraftItemEvent event) {
        if (event.getWhoClicked() instanceof final Player player && player == data.profile().getPlayer()) {
            var item = event.getRecipe().getResult();
            if (item.isEmpty()) return;

            progress(calculateCraftAmount(event), meta(AuroraAPI.getItemManager().resolveId(item)));
        }
    }

    public void onCraftItem(PlayerCraftedItemEvent event) {
        progress(event.getAmount(), meta(event.getItem()));
    }

    private static int calculateCraftAmount(final CraftItemEvent event) {
        final ItemStack result = event.getRecipe().getResult();
        final PlayerInventory inventory = event.getWhoClicked().getInventory();
        final ItemStack[] ingredients = event.getInventory().getMatrix();
        return switch (event.getClick()) {
            case SHIFT_LEFT, SHIFT_RIGHT -> InventoryUtils.calculateShiftCraftAmount(result, inventory, ingredients);
            case CONTROL_DROP -> InventoryUtils.calculateMaximumCraftAmount(result, ingredients);
            case NUMBER_KEY ->
                    InventoryUtils.calculateSwapCraftAmount(result, inventory.getItem(event.getHotbarButton()));
            case SWAP_OFFHAND -> InventoryUtils.calculateSwapCraftAmount(result, inventory.getItemInOffHand());
            case DROP -> InventoryUtils.calculateDropCraftAmount(result, event.getCursor());
            case LEFT, RIGHT -> InventoryUtils.calculateSimpleCraftAmount(result, event.getCursor());
            default -> 0;
        };
    }
}
