package gg.auroramc.quests.objective;

import com.google.common.collect.Lists;
import gg.auroramc.quests.api.event.objective.PlayerSpendOnPurchaseEvent;
import gg.auroramc.quests.api.objective.Objective;
import gg.auroramc.quests.api.objective.ObjectiveDefinition;
import gg.auroramc.quests.api.objective.filter.ObjectiveFilter;
import gg.auroramc.quests.api.objective.filter.StringFilter;
import gg.auroramc.quests.api.profile.Profile;
import gg.auroramc.quests.api.quest.Quest;
import org.bukkit.event.EventPriority;

import java.util.List;

public class BuyWorthObjective extends Objective {

    public BuyWorthObjective(Quest quest, ObjectiveDefinition definition, Profile.TaskDataWrapper data) {
        super(quest, definition, data);
    }

    @Override
    protected void activate() {
        onEvent(PlayerSpendOnPurchaseEvent.class, this::handle, EventPriority.MONITOR);
    }

    public void handle(PlayerSpendOnPurchaseEvent event) {
        progress(event.getAmount(), StringFilter.with(meta(), "currency", event.getCurrency()));
    }

    @Override
    public List<ObjectiveFilter> getFilters() {
        return Lists.newArrayList(StringFilter.stringFilter(definition.getArgs(), "currency"));
    }
}
