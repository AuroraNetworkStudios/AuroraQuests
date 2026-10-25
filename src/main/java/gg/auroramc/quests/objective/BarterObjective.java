package gg.auroramc.quests.objective;

import gg.auroramc.aurora.api.AuroraAPI;
import gg.auroramc.quests.api.objective.ObjectiveDefinition;
import gg.auroramc.quests.api.objective.TypedObjective;
import gg.auroramc.quests.api.profile.Profile;
import gg.auroramc.quests.api.quest.Quest;
import org.bukkit.Material;
import org.bukkit.entity.Piglin;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.PiglinBarterEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.inventory.EquipmentSlot;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class BarterObjective extends TypedObjective {
    // PiglinBarterEvent carries no player, so remember which piglins this player gave a barter item to.
    // Piglins that die before bartering stay here until the objective is disposed.
    private final Set<UUID> fedPiglins = ConcurrentHashMap.newKeySet();

    public BarterObjective(Quest quest, ObjectiveDefinition definition, Profile.TaskDataWrapper data) {
        super(quest, definition, data);
    }

    @Override
    protected void activate() {
        onEvent(EntityPickupItemEvent.class, this::onPickup, EventPriority.MONITOR);
        onEvent(PlayerInteractAtEntityEvent.class, this::onInteract, EventPriority.MONITOR);
        onEvent(PiglinBarterEvent.class, this::onBarter, EventPriority.MONITOR);
    }

    public void onPickup(EntityPickupItemEvent event) {
        if (!(event.getEntity() instanceof Piglin piglin)) return;
        var item = event.getItem();
        if (!data.profile().getPlayer().getUniqueId().equals(item.getThrower())) return;
        if (isBarterItem(piglin, item.getItemStack().getType())) {
            fedPiglins.add(piglin.getUniqueId());
        }
    }

    public void onInteract(PlayerInteractAtEntityEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) return;
        if (!(event.getRightClicked() instanceof Piglin piglin)) return;
        // Piglins only take the item when adult and not already admiring one
        if (!piglin.isAdult() || !piglin.getEquipment().getItemInOffHand().isEmpty()) return;
        if (isBarterItem(piglin, event.getPlayer().getInventory().getItemInMainHand().getType())) {
            fedPiglins.add(piglin.getUniqueId());
        }
    }

    public void onBarter(PiglinBarterEvent event) {
        if (!fedPiglins.remove(event.getEntity().getUniqueId())) return;
        var location = event.getEntity().getLocation();
        for (var item : event.getOutcome()) {
            progress(item.getAmount(), meta(location, AuroraAPI.getItemManager().resolveId(item)));
        }
    }

    // getBarterList() only holds items added through the API, gold ingots are hardcoded in vanilla
    private static boolean isBarterItem(Piglin piglin, Material material) {
        return material == Material.GOLD_INGOT || piglin.getBarterList().contains(material);
    }

    @Override
    public void dispose() {
        super.dispose();
        fedPiglins.clear();
    }
}
