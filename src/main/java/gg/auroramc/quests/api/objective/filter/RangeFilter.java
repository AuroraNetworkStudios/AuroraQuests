package gg.auroramc.quests.api.objective.filter;

import gg.auroramc.quests.api.objective.ObjectiveMeta;
import org.bukkit.configuration.ConfigurationSection;


public class RangeFilter implements ObjectiveFilter {
    private final String key;
    private final Double concreteValue;
    private final Double minValue;
    private final Double maxValue;

    public RangeFilter(String key, Double minValue, Double maxValue) {
        this.key = key;
        this.minValue = minValue;
        this.maxValue = maxValue;
        this.concreteValue = null;
    }

    public RangeFilter(String key, Double concreteValue) {
        this.key = key;
        this.concreteValue = concreteValue;
        this.minValue = null;
        this.maxValue = null;
    }


    @Override
    public boolean filter(ObjectiveMeta meta) {
        var value = meta.getVariable(key, Double.class);
        if (value.isEmpty()) return false;

        if (concreteValue != null) {
            return value.get().equals(concreteValue);
        }

        var min = minValue != null ? minValue : 0;
        var max = maxValue != null ? maxValue : Integer.MAX_VALUE;
        return value.get() >= min && value.get() <= max;
    }

    public static RangeFilter rangeFilter(ConfigurationSection args, String key) {
        if (args.contains(key)) {
            return new RangeFilter(key, args.getDouble(key));
        } else {
            return new RangeFilter(key, args.getDouble("min-" + key), args.getDouble("max-" + key));
        }
    }

    public static ObjectiveMeta with(ObjectiveMeta meta, String key, Double value) {
        meta.setVariable(key, value);
        return meta;
    }
}
