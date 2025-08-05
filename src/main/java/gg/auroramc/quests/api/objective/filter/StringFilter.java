package gg.auroramc.quests.api.objective.filter;

import gg.auroramc.quests.api.objective.ObjectiveMeta;
import lombok.AllArgsConstructor;
import org.bukkit.configuration.ConfigurationSection;

@AllArgsConstructor
public class StringFilter implements ObjectiveFilter {
    private final String key;
    private final String string;

    @Override
    public boolean filter(ObjectiveMeta meta) {
        if (string == null) return true;
        var current = meta.getVariable(key, String.class);
        return current.map(s -> s.equals(string)).orElse(false);
    }

    public static StringFilter stringFilter(ConfigurationSection args, String key) {
        return new StringFilter(key, args.getString(key));
    }

    public static ObjectiveMeta with(ObjectiveMeta meta, String key, String value) {
        meta.setVariable(key, value);
        return meta;
    }
}
