package gg.auroramc.quests.objective;

import gg.auroramc.aurora.api.AuroraAPI;
import gg.auroramc.aurora.api.item.TypeId;
import gg.auroramc.quests.api.event.objective.PlayerCaughtFishEvent;
import gg.auroramc.quests.api.objective.ObjectiveDefinition;
import gg.auroramc.quests.api.objective.TypedObjective;
import gg.auroramc.quests.api.profile.Profile;
import gg.auroramc.quests.api.quest.Quest;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.inventory.ItemStack;

public class FishingObjective extends TypedObjective {

    public FishingObjective(Quest quest, ObjectiveDefinition definition, Profile.TaskDataWrapper data) {
        super(quest, definition, data);
    }

    @Override
    protected void activate() {
        onEvent(PlayerFishEvent.class, this::onFishCaught, EventPriority.MONITOR);
        onEvent(PlayerCaughtFishEvent.class, this::onFishCaught, EventPriority.MONITOR);
    }

    public void onFishCaught(PlayerFishEvent event) {
        PlayerFishEvent.State state = event.getState();
        if (state != PlayerFishEvent.State.CAUGHT_FISH) return;

        final Entity entity = event.getCaught();
        if (!(entity instanceof Item caught)) return;

        ItemStack item = caught.getItemStack();
        var amount = item.getAmount();

        var id = AuroraAPI.getItemManager().resolveId(item);

        if (id.namespace().equals("minecraft") && item.hasItemMeta()) {
            if (item.getItemMeta().hasCustomModelData()) {
                // To support plugins that use custom model data on fishing loot and doesn't have an API otherwise
                id = TypeId.fromDefault(id.id() + ":" + item.getItemMeta().getCustomModelData());
            }
        }

        progress(amount, meta(event.getHook().getLocation(), id));
    }

    public void onFishCaught(PlayerCaughtFishEvent event) {
        progress(event.getAmount(), meta(event.getLocation(), event.getType()));
    }
}
