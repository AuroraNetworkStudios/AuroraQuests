package gg.auroramc.quests.api.objective.filter;

import gg.auroramc.aurora.api.menu.Requirement;
import gg.auroramc.aurora.api.message.Placeholder;
import gg.auroramc.quests.api.objective.ObjectiveMeta;
import lombok.AllArgsConstructor;

import java.util.List;

@AllArgsConstructor
public class RequirementFilter implements ObjectiveFilter {
    private final List<String> requirements;

    @Override
    public boolean filter(ObjectiveMeta meta) {
        return Requirement.isAllMet(meta.getPlayer(), requirements, List.of(Placeholder.of("{player}", meta.getPlayer().getName())));
    }
}
