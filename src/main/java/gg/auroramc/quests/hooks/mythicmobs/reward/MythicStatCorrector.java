package gg.auroramc.quests.hooks.mythicmobs.reward;

import com.google.common.collect.Maps;
import gg.auroramc.aurora.api.reward.RewardCorrector;
import gg.auroramc.quests.AuroraQuests;
import io.lumine.mythic.bukkit.MythicBukkit;
import io.lumine.mythic.core.skills.stats.StatModifierType;
import io.lumine.mythic.core.skills.stats.StatType;
import org.bukkit.entity.Player;

import java.util.Map;

public class MythicStatCorrector implements RewardCorrector {

    @Override
    public void correctRewards(Player player) {
        var plugin = AuroraQuests.getInstance();
        var profile = plugin.getProfileManager().getProfile(player);
        var mythic = MythicBukkit.inst();
        var registry = mythic.getPlayerManager().getProfile(player).getStatRegistry();

        Map<StatType, Map<StatModifierType, Double>> statMap = Maps.newHashMap();

        mythic.getStatManager().getStats().values()
                .forEach(statType -> {
                    if (!statType.isEnabled()) return;
                    registry.removeValue(statType, MythicStatReward.getSource());
                });

        // Gather new stat modifiers
        for (var pool : profile.getQuestPools()) {
            // Correct global quests
            if (pool.isGlobal()) {
                for (var quest : pool.getQuests()) {
                    if (!quest.isCompleted()) continue;

                    for (var reward : quest.getDefinition().getRewards().values()) {
                        if (reward instanceof MythicStatReward statReward && statReward.isValid()) {
                            statMap.computeIfAbsent(statReward.getStatType(), (key) -> Maps.newHashMap())
                                    .merge(statReward.getModifierType(), statReward.getValue(quest.getPlaceholders()), Double::sum);
                        }
                    }
                }
            }

            // Correct quest pool leveling
            if (!pool.hasLeveling()) continue;
            var level = pool.getLevel();

            for (int i = 1; i < level + 1; i++) {
                var matcher = pool.getPool().getMatcherManager().getBestMatcher(i);
                if (matcher == null) continue;
                var placeholders = pool.getLevelPlaceholders(i);
                for (var reward : matcher.computeRewards(i)) {
                    if (reward instanceof MythicStatReward statReward && statReward.isValid()) {
                        statMap.computeIfAbsent(statReward.getStatType(), (key) -> Maps.newHashMap())
                                .merge(statReward.getModifierType(), statReward.getValue(placeholders), Double::sum);
                    }
                }
            }
        }

        // Apply the new stat modifiers
        for (var entry : statMap.entrySet()) {
            var statType = entry.getKey();
            for (var modifierEntry : entry.getValue().entrySet()) {
                var modifierType = modifierEntry.getKey();
                var value = modifierEntry.getValue();
                AuroraQuests.logger().debug("Adding stat " + statType.getKey() + " with value " + value + " to player " + player.getName());
                registry.putValue(statType, MythicStatReward.getSource(), modifierType, value);
            }
        }
    }
}
