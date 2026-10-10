package gg.auroramc.quests.hooks.mmoitems;

import gg.auroramc.aurora.api.item.TypeId;
import gg.auroramc.quests.AuroraQuests;
import gg.auroramc.quests.api.event.objective.PlayerCraftedItemEvent;
import gg.auroramc.quests.hooks.Hook;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.event.PlayerUseCraftingStationEvent;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

public class MMOItemsHook implements Hook, Listener {

    @Override
    public void hook(AuroraQuests plugin) {
        AuroraQuests.logger().info("Hooked into MMOItems for CRAFT objectives.");
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onCraftItem(PlayerUseCraftingStationEvent event) {
        PlayerUseCraftingStationEvent.StationAction action = event.getInteraction();
        // Only when the player actually gets the item in hand
        if (action != PlayerUseCraftingStationEvent.StationAction.CRAFTING_QUEUE
                && action != PlayerUseCraftingStationEvent.StationAction.INSTANT_RECIPE) {
            return;
        }
        // If no result, then skip
        if (!event.hasResult()) {
            return;
        }
        // Check if the result is an actual item
        ItemStack stack = event.getResult();
        if (stack == null || stack.getType().isAir()) {
            return;
        }
        // MMOItems item ID
        String mmoId = MMOItems.getID(stack);
        if (mmoId == null) {
            // That means this is not mmo item, we should ignore it
            return;
        }

        // Call the craft item event
        PlayerCraftedItemEvent craftEvent = new PlayerCraftedItemEvent(event.getPlayer(), TypeId.fromString("mmoitems:" + mmoId), stack.getAmount());
        Bukkit.getPluginManager().callEvent(craftEvent);
    }

}
