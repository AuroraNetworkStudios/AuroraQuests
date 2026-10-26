package gg.auroramc.quests.api.objective.filter;

import gg.auroramc.quests.api.objective.ObjectiveMeta;
import lombok.AllArgsConstructor;

import java.util.Set;

@AllArgsConstructor
public class WeatherFilter implements ObjectiveFilter {
    private final Set<String> weather;
    private final boolean blacklist;

    @Override
    public boolean filter(ObjectiveMeta meta) {
        var world = meta.getLocation().getWorld();
        var current = world.isThundering() ? "thunder" : world.hasStorm() ? "rain" : "clear";

        return weather.contains(current) != blacklist;
    }
}
