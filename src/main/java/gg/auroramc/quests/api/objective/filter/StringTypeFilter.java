package gg.auroramc.quests.api.objective.filter;

import gg.auroramc.quests.api.objective.ObjectiveMeta;
import lombok.AllArgsConstructor;

import java.util.Set;

@AllArgsConstructor
public class StringTypeFilter implements ObjectiveFilter {
    private Set<String> types;
    private Mode mode;

    @Override
    public boolean filter(ObjectiveMeta meta) {
        if (types == null || types.isEmpty()) return true;
        var type = meta.getVariable("type", String.class);

        var value = type.filter(typeId -> types.contains(typeId));

        if (mode == Mode.WHITELIST) {
            return value.isPresent();
        } else {
            return value.isEmpty();
        }
    }

    public enum Mode {
        WHITELIST,
        BLACKLIST;

        public static Mode parse(String mode) {
            if (mode.equalsIgnoreCase("blacklist")) return Mode.BLACKLIST;
            // Default
            return Mode.WHITELIST;
        }
    }
}
