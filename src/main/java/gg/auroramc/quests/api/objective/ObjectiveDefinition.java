package gg.auroramc.quests.api.objective;

import gg.auroramc.quests.api.objective.filter.ObjectiveFilter;
import lombok.Builder;
import lombok.Getter;
import org.bukkit.configuration.ConfigurationSection;

import java.util.List;

@Getter
@Builder
public class ObjectiveDefinition {
    private final String id;
    private final String task;
    private final String display;
    private final ConfigurationSection args;
    private final List<ObjectiveFilter> filters;
    private final List<String> onProgress;
    private final List<String> onComplete;
}
