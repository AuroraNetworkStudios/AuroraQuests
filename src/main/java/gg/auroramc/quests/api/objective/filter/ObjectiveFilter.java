package gg.auroramc.quests.api.objective.filter;

import gg.auroramc.quests.api.objective.ObjectiveMeta;

public interface ObjectiveFilter {
    boolean filter(ObjectiveMeta meta);
}
