package gg.auroramc.quests.api.objective.filter;

import gg.auroramc.quests.api.objective.ObjectiveMeta;
import gg.auroramc.quests.hooks.HookManager;
import gg.auroramc.quests.hooks.worldguard.WorldGuardHook;
import lombok.AllArgsConstructor;

import java.util.Set;

@AllArgsConstructor
public class RegionFilter implements ObjectiveFilter {
    private Set<String> regions;
    private boolean blacklist;

    @Override
    public boolean filter(ObjectiveMeta meta) {
        var inRegion = HookManager.getHook(WorldGuardHook.class)
                .isInAnyRegion(meta.getPlayer(), meta.getLocation(), regions);

        return inRegion != blacklist;
    }
}
