package gg.auroramc.quests.api.objective.filter;

import gg.auroramc.quests.api.objective.ObjectiveMeta;
import lombok.AllArgsConstructor;

import java.util.Set;

@AllArgsConstructor
public class WorldFilter implements ObjectiveFilter {
    private final Set<String> worlds;
    private final boolean blacklist;

    @Override
    public boolean filter(ObjectiveMeta meta) {
        var worldName = meta.getLocation().getWorld().getName();

        if (blacklist) {
            return !worlds.contains(worldName);
        } else {
            return worlds.contains(worldName);
        }
    }
}
