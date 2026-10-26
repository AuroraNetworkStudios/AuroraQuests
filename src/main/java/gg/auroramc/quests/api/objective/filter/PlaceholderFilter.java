package gg.auroramc.quests.api.objective.filter;

import gg.auroramc.aurora.api.dependency.Dep;
import gg.auroramc.aurora.api.dependency.DependencyManager;
import gg.auroramc.aurora.api.menu.Requirement;
import gg.auroramc.aurora.api.message.Text;
import gg.auroramc.quests.api.objective.ObjectiveMeta;
import lombok.AllArgsConstructor;

import java.util.List;

@AllArgsConstructor
public class PlaceholderFilter implements ObjectiveFilter {
    private final String placeholder;
    private final String comparison;
    private final String value;
    private final String separator;
    private final String requirement;

    @Override
    public boolean filter(ObjectiveMeta meta) {
        if (comparison.endsWith("contains")) {
            if (!DependencyManager.hasDep(Dep.PAPI)) return false;
            var resolved = Text.fillPlaceholders(meta.getPlayer(), placeholder, List.of());
            return containsEntry(resolved, separator, value) == comparison.equals("contains");
        }

        try {
            return Requirement.isMet(meta.getPlayer(), requirement, List.of());
        } catch (NumberFormatException e) {
            // Requirement throws on a non-numeric placeholder value; count that as not met
            return false;
        }
    }

    static boolean containsEntry(String list, String sep, String value) {
        var start = 0;
        while (start <= list.length()) {
            var end = list.indexOf(sep, start);
            if (end < 0) end = list.length();
            var from = start;
            var to = end;
            while (from < to && Character.isWhitespace(list.charAt(from))) from++;
            while (to > from && Character.isWhitespace(list.charAt(to - 1))) to--;
            if (to - from == value.length() && list.regionMatches(from, value, 0, value.length())) return true;
            start = end + sep.length();
        }
        return false;
    }
}
