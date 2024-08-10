package gg.auroramc.quests.placeholder;

import gg.auroramc.aurora.api.AuroraAPI;
import gg.auroramc.aurora.api.placeholder.PlaceholderHandler;
import gg.auroramc.quests.AuroraQuests;
import gg.auroramc.quests.util.DurationFormatter;
import gg.auroramc.quests.util.RomanNumber;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class QuestPlaceholderHandler implements PlaceholderHandler {
    @Override
    public String getIdentifier() {
        return "quests";
    }

    @Override
    public String onPlaceholderRequest(Player player, String[] args) {
        if (args.length < 2) return null;
        var manager = AuroraQuests.getInstance().getQuestManager();
        var full = String.join("_", args);

        if (full.endsWith("total_completed_raw")) {
            var sum = manager.getQuestPools().stream().mapToLong(p -> p.getCompletedQuestCount(player)).sum();
            return String.valueOf(sum);
        } else if (full.endsWith("total_completed")) {
            var sum = manager.getQuestPools().stream().mapToLong(p -> p.getCompletedQuestCount(player)).sum();
            return AuroraAPI.formatNumber(sum);
        } else if (full.endsWith("level_roman")) {
            var pool = manager.getQuestPool(full.substring(0, full.length() - 12));
            if (pool == null) return null;
            return RomanNumber.toRoman(pool.getPlayerLevel(player));
        } else if (full.endsWith("level_raw")) {
            var pool = manager.getQuestPool(full.substring(0, full.length() - 10));
            if (pool == null) return null;
            return String.valueOf(pool.getPlayerLevel(player));
        } else if (full.endsWith("level")) {
            var pool = manager.getQuestPool(full.substring(0, full.length() - 6));
            if (pool == null) return null;
            return AuroraAPI.formatNumber(pool.getPlayerLevel(player));
        } else if (full.endsWith("current_count")) {
            var pool = manager.getQuestPool(full.substring(0, full.length() - 14));
            if (pool == null) return null;
            return AuroraAPI.formatNumber(pool.getPlayerQuests(player).size());
        } else if (full.endsWith("current_completed")) {
            var pool = manager.getQuestPool(full.substring(0, full.length() - 18));
            if (pool == null) return null;
            return AuroraAPI.formatNumber(pool.getPlayerQuests(player).stream().filter(q -> q.isCompleted(player)).count());
        } else if (full.endsWith("count_raw")) {
            var pool = manager.getQuestPool(full.substring(0, full.length() - 10));
            if (pool == null) return null;
            return String.valueOf(pool.getCompletedQuestCount(player));
        } else if (full.endsWith("count")) {
            var pool = manager.getQuestPool(full.substring(0, full.length() - 6));
            if (pool == null) return null;
            return AuroraAPI.formatNumber(pool.getCompletedQuestCount(player));
        } else if (full.endsWith("countdown_long")) {
            var pool = manager.getQuestPool(full.substring(0, full.length() - 15));
            if (pool == null) return null;
            if (pool.isGlobal()) return null;
            return DurationFormatter.format(pool.getDurationUntilNextRoll(), DurationFormatter.Type.LONG);
        } else if (full.endsWith("countdown")) {
            var pool = manager.getQuestPool(full.substring(0, full.length() - 10));
            if (pool == null) return null;
            if (pool.isGlobal()) return null;
            return DurationFormatter.format(pool.getDurationUntilNextRoll(), DurationFormatter.Type.SHORT);
        }

        return null;
    }

    @Override
    public List<String> getPatterns() {
        var manager = AuroraQuests.getInstance().getQuestManager();

        var list = new ArrayList<String>(manager.getQuestPools().size() * 7 + 2);

        list.add("total_completed_raw");
        list.add("total_completed");

        for (var pool : manager.getQuestPools()) {
            list.add(pool.getId() + "_level");
            list.add(pool.getId() + "_level_roman%");
            list.add(pool.getId() + "_level_raw");
            list.add(pool.getId() + "_count");
            list.add(pool.getId() + "_count_raw");
            list.add(pool.getId() + "_current_count");
            list.add(pool.getId() + "_current_completed");
            list.add(pool.getId() + "_countdown");
            list.add(pool.getId() + "_countdown_long");
        }

        return list;
    }
}
