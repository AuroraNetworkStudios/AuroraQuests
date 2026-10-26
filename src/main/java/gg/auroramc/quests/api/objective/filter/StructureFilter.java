package gg.auroramc.quests.api.objective.filter;

import gg.auroramc.quests.api.objective.ObjectiveMeta;
import lombok.AllArgsConstructor;
import org.bukkit.generator.structure.Structure;

import java.util.Set;

@AllArgsConstructor
public class StructureFilter implements ObjectiveFilter {
    private final Set<Structure> structures;
    private final boolean blacklist;

    @Override
    public boolean filter(ObjectiveMeta meta) {
        var location = meta.getLocation();
        var world = location.getWorld();

        var inside = false;
        for (var structure : structures) {
            if (world.hasStructureAt(location, structure)) {
                inside = true;
                break;
            }
        }

        return inside != blacklist;
    }
}
