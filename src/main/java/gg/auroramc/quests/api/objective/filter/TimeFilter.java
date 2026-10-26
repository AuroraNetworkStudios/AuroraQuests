package gg.auroramc.quests.api.objective.filter;

import gg.auroramc.quests.api.objective.ObjectiveMeta;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class TimeFilter implements ObjectiveFilter {
    private final boolean day;

    @Override
    public boolean filter(ObjectiveMeta meta) {
        return meta.getLocation().getWorld().isDayTime() == day;
    }
}
