package gg.auroramc.quests.parser;

import gg.auroramc.aurora.api.item.TypeId;
import gg.auroramc.quests.api.objective.ObjectiveDefinition;
import gg.auroramc.quests.api.objective.filter.*;
import gg.auroramc.quests.config.quest.FilterConfig;
import gg.auroramc.quests.config.quest.TaskConfig;
import gg.auroramc.quests.hooks.HookManager;
import gg.auroramc.quests.hooks.worldguard.WorldGuardHook;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ObjectiveParser {
    public static ObjectiveDefinition parse(String id, TaskConfig config) {
        return ObjectiveDefinition.builder()
                .id(id)
                .display(config.getDisplay())
                .task(config.getTask())
                .args(config.getArgs())
                .filters(parseFilters(config.getFilters()))
                .onProgress(config.getOnProgress())
                .onComplete(config.getOnComplete())
                .build();
    }

    private static List<ObjectiveFilter> parseFilters(FilterConfig config) {
        var taskFilters = new ArrayList<ObjectiveFilter>();

        if (config == null) return taskFilters;

        if (config.getBiomes() != null) {
            taskFilters.add(new BiomeFilter(config.getBiomes().getValue(), config.getBiomes().getType().equals("blacklist")));
        }

        if (config.getWorlds() != null) {
            taskFilters.add(new WorldFilter(config.getWorlds().getValue(), config.getWorlds().getType().equals("blacklist")));
        }

        if (config.getHand() != null) {
            taskFilters.add(new HandItemFilter(config.getHand().getItems().stream().map(TypeId::fromString).collect(Collectors.toSet())));
        }

        if (config.getMaxYLevel() != null || config.getMinYLevel() != null) {
            taskFilters.add(new YLevelFilter(config.getMinYLevel(), config.getMaxYLevel()));
        }

        if (config.getRequirements() != null) {
            taskFilters.add(new RequirementFilter(config.getRequirements()));
        }

        if (config.getRegions() != null && HookManager.isEnabled(WorldGuardHook.class)) {
            taskFilters.add(new RegionFilter(config.getRegions().getValue(), config.getRegions().getType().equals("blacklist")));
        }

        return taskFilters;
    }
}
