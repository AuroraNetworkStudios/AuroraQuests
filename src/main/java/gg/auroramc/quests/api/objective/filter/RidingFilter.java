package gg.auroramc.quests.api.objective.filter;

import gg.auroramc.quests.api.objective.ObjectiveMeta;
import lombok.AllArgsConstructor;
import org.bukkit.entity.EntityType;

import java.util.Set;

@AllArgsConstructor
public class RidingFilter implements ObjectiveFilter {
    private final Set<EntityType> types;
    private final boolean blacklist;

    @Override
    public boolean filter(ObjectiveMeta meta) {
        var vehicle = meta.getPlayer().getVehicle();
        var riding = vehicle != null && types.contains(vehicle.getType());
        return riding != blacklist;
    }
}
