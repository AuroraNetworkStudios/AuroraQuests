package gg.auroramc.quests.api.objective.filter;

import gg.auroramc.quests.api.objective.ObjectiveMeta;
import lombok.AllArgsConstructor;
import org.bukkit.configuration.ConfigurationSection;

@AllArgsConstructor
public class DoubleFilter implements ObjectiveFilter {
    private final String key;
    private final double value;

    @Override
    public boolean filter(ObjectiveMeta meta) {
        if (value == -1) return true;
        var current = meta.getVariable(key, Double.class);
        return current.map(s -> s.equals(value)).orElse(false);
    }

    public static DoubleFilter doubleFilter(ConfigurationSection args, String key) {
        return new DoubleFilter(key, args.getDouble(key, -1));
    }

    public static ObjectiveMeta with(ObjectiveMeta meta, String key, Double value) {
        meta.setVariable(key, value);
        return meta;
    }
}
