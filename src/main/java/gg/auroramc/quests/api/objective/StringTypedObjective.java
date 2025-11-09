package gg.auroramc.quests.api.objective;

import gg.auroramc.quests.api.objective.filter.StringTypeFilter;
import gg.auroramc.quests.api.profile.Profile;
import gg.auroramc.quests.api.quest.Quest;
import org.bukkit.Location;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public abstract class StringTypedObjective extends Objective {
    private final Map<String, Double> multipliers = new HashMap<>();

    public StringTypedObjective(Quest quest, ObjectiveDefinition definition, Profile.TaskDataWrapper data) {
        super(quest, definition, data);
        var filter = new StringTypeFilter(
                new HashSet<>(definition.getArgs().getStringList("types")),
                StringTypeFilter.Mode.parse(definition.getArgs().getString("mode", "whitelist"))
        );
        this.filters.add(filter);

        var multipliersSection = definition.getArgs().getConfigurationSection("multipliers");
        if (multipliersSection == null) return;

        for (var type : multipliersSection.getKeys(false)) {
            multipliers.put(type, multipliersSection.getDouble(type));
        }
    }

    @Override
    public double applyMultipliers(double progress, ObjectiveMeta meta) {
        var variable = meta.getVariable("type", String.class);
        return variable.map(typeId -> progress * multipliers.getOrDefault(typeId, 1D)).orElse(progress);
    }

    protected ObjectiveMeta meta(Location location, String type) {
        var meta = new ObjectiveMeta(data.profile().getPlayer(), location);
        meta.setVariable("type", type);
        return meta;
    }

    protected ObjectiveMeta meta(String type) {
        return meta(data.profile().getPlayer().getLocation(), type);
    }
}