package gg.auroramc.quests.api.objective.filter;

import gg.auroramc.quests.api.objective.ObjectiveMeta;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class LightFilter implements ObjectiveFilter {
    private Integer min;
    private Integer max;

    @Override
    public boolean filter(ObjectiveMeta meta) {
        var light = meta.getPlayer().getLocation().getBlock().getLightLevel();

        return (min == null || light >= min) && (max == null || light <= max);
    }
}
