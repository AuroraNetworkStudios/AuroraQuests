package gg.auroramc.quests.hooks.shopkeepers.objective;

import com.nisovin.shopkeepers.api.events.ShopkeeperTradeEvent;
import gg.auroramc.aurora.api.AuroraAPI;
import gg.auroramc.quests.api.objective.ObjectiveDefinition;
import gg.auroramc.quests.api.objective.TypedObjective;
import gg.auroramc.quests.api.profile.Profile;
import gg.auroramc.quests.api.quest.Quest;
import org.bukkit.event.EventPriority;

public class TradeShopkeeperObjective extends TypedObjective {

    public TradeShopkeeperObjective(Quest quest, ObjectiveDefinition definition, Profile.TaskDataWrapper data) {
        super(quest, definition, data);
    }

    @Override
    protected void activate() {
        onEvent(ShopkeeperTradeEvent.class, this::handle, EventPriority.MONITOR);
    }

    public void handle(ShopkeeperTradeEvent event) {
        if (event.getPlayer() != data.profile().getPlayer()) return;

        var item = event.getTradingRecipe().getResultItem().copy();
        var quantity = item.getAmount();

        var id = AuroraAPI.getItemManager().resolveId(item);

        progress(quantity, meta(id));
    }
}
