package gg.auroramc.quests.api.objective.filter;

import gg.auroramc.quests.api.objective.ObjectiveMeta;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class YLevelFilter implements ObjectiveFilter {
    private Integer min;
    private Integer max;

    @Override
    public boolean filter(ObjectiveMeta meta) {
        var location = meta.getLocation();

        var valid = true;

        if (min != null) {
            valid = location.getY() >= min;
        }

        if (max != null) {
            valid = valid && location.getY() <= max;
        }

        return valid;
    }
}
