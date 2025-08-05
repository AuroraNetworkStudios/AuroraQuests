package gg.auroramc.quests.objective;

import com.google.common.collect.Lists;
import gg.auroramc.aurora.api.item.TypeId;
import gg.auroramc.quests.api.objective.ObjectiveDefinition;
import gg.auroramc.quests.api.objective.TypedObjective;
import gg.auroramc.quests.api.objective.filter.RangeFilter;
import gg.auroramc.quests.api.objective.filter.ObjectiveFilter;
import gg.auroramc.quests.api.profile.Profile;
import gg.auroramc.quests.api.quest.Quest;
import org.bukkit.event.EventPriority;
import org.bukkit.event.enchantment.EnchantItemEvent;

import java.util.List;

public class EnchantObjective extends TypedObjective {

    public EnchantObjective(Quest quest, ObjectiveDefinition definition, Profile.TaskDataWrapper data) {
        super(quest, definition, data);
    }

    @Override
    protected void activate() {
        onEvent(EnchantItemEvent.class, this::onEnchant, EventPriority.MONITOR);
    }

    public void onEnchant(EnchantItemEvent e) {
        var player = e.getEnchanter();
        if (player != data.profile().getPlayer()) return;

        for (var enchant : e.getEnchantsToAdd().entrySet()) {
            var id = new TypeId(enchant.getKey().getKey().getNamespace(), enchant.getKey().getKey().getKey());
            var meta = RangeFilter.with(meta(id), "level", enchant.getValue().doubleValue());

            progress(1, meta);
        }
    }

    @Override
    public List<ObjectiveFilter> getFilters() {
        return Lists.newArrayList(RangeFilter.rangeFilter(definition.getArgs(), "level"));
    }
}
