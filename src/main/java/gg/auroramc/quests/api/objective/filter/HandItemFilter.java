package gg.auroramc.quests.api.objective.filter;

import gg.auroramc.aurora.api.AuroraAPI;
import gg.auroramc.aurora.api.item.TypeId;
import gg.auroramc.quests.api.objective.ObjectiveMeta;
import lombok.AllArgsConstructor;

import java.util.Set;

@AllArgsConstructor
public class HandItemFilter implements ObjectiveFilter {
    private final Set<TypeId> items;

    @Override
    public boolean filter(ObjectiveMeta meta) {
        var itemId = AuroraAPI.getItemManager().resolveId(meta.getPlayer().getInventory().getItemInMainHand());
        return items.contains(itemId);
    }
}
