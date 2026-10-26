package gg.auroramc.quests.parser;

import gg.auroramc.aurora.api.item.TypeId;
import gg.auroramc.quests.api.objective.ObjectiveDefinition;
import gg.auroramc.quests.api.objective.filter.*;
import gg.auroramc.quests.config.quest.FilterConfig;
import gg.auroramc.quests.config.quest.TaskConfig;
import gg.auroramc.quests.AuroraQuests;
import gg.auroramc.quests.hooks.HookManager;
import gg.auroramc.quests.hooks.worldguard.WorldGuardHook;

import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.entity.EntityType;
import org.bukkit.generator.structure.Structure;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class ObjectiveParser {
    private static final List<String> PLACEHOLDER_COMPARISONS = List.of("==", "===", ">=", "<=", "<", ">", "contains", "!contains");

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

        if (config.getTime() != null) {
            if (config.getTime().equalsIgnoreCase("day") || config.getTime().equalsIgnoreCase("night")) {
                taskFilters.add(new TimeFilter(config.getTime().equalsIgnoreCase("day")));
            } else {
                AuroraQuests.logger().warning("Invalid time filter '" + config.getTime() + "', expected day or night");
            }
        }

        if (config.getWeather() != null) {
            var weather = new HashSet<String>();
            for (var value : config.getWeather().getValue()) {
                var lower = value.toLowerCase();
                if (Set.of("clear", "rain", "thunder").contains(lower)) {
                    weather.add(lower);
                } else {
                    AuroraQuests.logger().warning("Invalid weather filter value '" + value + "', expected clear, rain or thunder");
                }
            }
            taskFilters.add(new WeatherFilter(weather, config.getWeather().getType().equals("blacklist")));
        }

        if (config.getMinLight() != null || config.getMaxLight() != null) {
            taskFilters.add(new LightFilter(config.getMinLight(), config.getMaxLight()));
        }

        if (config.getStructures() != null) {
            var structures = new HashSet<Structure>();
            for (var id : config.getStructures().getValue()) {
                var key = NamespacedKey.fromString(id.toLowerCase());
                var structure = key == null ? null : Registry.STRUCTURE.get(key);
                if (structure != null) {
                    structures.add(structure);
                } else {
                    AuroraQuests.logger().warning("Unknown structure '" + id + "' in task filters");
                }
            }
            taskFilters.add(new StructureFilter(structures, config.getStructures().getType().equals("blacklist")));
        }

        if (config.getRiding() != null) {
            var types = new HashSet<EntityType>();
            for (var id : config.getRiding().getValue()) {
                var key = NamespacedKey.fromString(id.toLowerCase());
                var type = key == null ? null : Registry.ENTITY_TYPE.get(key);
                if (type != null) {
                    types.add(type);
                } else {
                    AuroraQuests.logger().warning("Unknown entity type '" + id + "' in task filters");
                }
            }
            taskFilters.add(new RidingFilter(types, config.getRiding().getType().equals("blacklist")));
        }

        if (config.getPlaceholders() != null) {
            for (var entry : config.getPlaceholders().entrySet()) {
                var check = entry.getValue();
                var ph = check.getPlaceholder();
                var cmp = check.getComparison();
                if (ph == null || check.getValue() == null) {
                    AuroraQuests.logger().warning("Invalid placeholder filter '" + entry.getKey() + "', placeholder and value are required");
                } else if (cmp == null || !PLACEHOLDER_COMPARISONS.contains(cmp)) {
                    AuroraQuests.logger().warning("Invalid placeholder filter comparison '" + cmp + "' in '" + entry.getKey() + "', expected one of " + PLACEHOLDER_COMPARISONS);
                } else if (ph.contains(" ")) {
                    AuroraQuests.logger().warning("Invalid placeholder filter '" + entry.getKey() + "', placeholder must not contain spaces");
                } else if (cmp.endsWith("contains") && (check.getSeparator() == null || check.getSeparator().isEmpty())) {
                    AuroraQuests.logger().warning("Invalid placeholder filter '" + entry.getKey() + "', separator must not be empty");
                } else {
                    var requirement = "[placeholder] " + ph + " " + cmp + " " + check.getValue();
                    taskFilters.add(new PlaceholderFilter(ph, cmp, check.getValue(), check.getSeparator(), requirement));
                }
            }
        }

        return taskFilters;
    }
}
