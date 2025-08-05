package gg.auroramc.quests.api.objective.filter;

import gg.auroramc.quests.api.objective.ObjectiveMeta;
import lombok.AllArgsConstructor;

import java.util.Set;

@AllArgsConstructor
public class BiomeFilter implements ObjectiveFilter {
    private final Set<String> biomes;
    private final boolean blacklist;

    @Override
    public boolean filter(ObjectiveMeta meta) {
        var biome = meta.getLocation().getBlock().getBiome().getKey().toString();

        if (blacklist) {
            return !biomes.contains(biome);
        } else {
            return biomes.contains(biome);
        }
    }
}
