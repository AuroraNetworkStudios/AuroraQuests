package gg.auroramc.quests.hooks.auraskills;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import dev.aurelium.auraskills.api.AuraSkillsApi;
import dev.aurelium.auraskills.api.stat.Stat;
import dev.aurelium.auraskills.api.stat.StatModifier;
import gg.auroramc.aurora.api.reward.RewardCorrector;
import gg.auroramc.quests.AuroraQuests;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class AuraSkillsCorrector implements RewardCorrector {
    private final Set<UUID> toLoad = Sets.newConcurrentHashSet();

    @Override
    public void correctRewards(Player player) {
        if (AuraSkillsApi.get().getUser(player.getUniqueId()).isLoaded()) {
            correctRewardsWhenLoaded(player, true);
        } else {
            toLoad.add(player.getUniqueId());
        }
    }

    public void correctRewardsWhenLoaded(Player player, boolean force) {
        if (!force && !toLoad.contains(player.getUniqueId())) return;
        toLoad.remove(player.getUniqueId());

        var profile = AuroraQuests.getInstance().getProfileManager().getProfile(player);

        Map<Stat, Double> statMap = Maps.newHashMap();

        // Reset all stat modifiers first
        for (var stat : AuraSkillsApi.get().getGlobalRegistry().getStats()) {
            statMap.put(stat, 0.0);
        }

        // Gather new stat modifiers
        for (var pool : profile.getQuestPools()) {
            // Correct global quests
            if (pool.isGlobal()) {
                for (var quest : pool.getQuests()) {
                    if (!quest.isCompleted()) continue;

                    for (var reward : quest.getDefinition().getRewards().values()) {
                        if (reward instanceof AuraSkillsStatReward statReward) {
                            statMap.merge(statReward.getStat(), statReward.getValue(quest.getPlaceholders()), Double::sum);
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
                    if (reward instanceof AuraSkillsStatReward statReward) {
                        statMap.merge(statReward.getStat(), statReward.getValue(placeholders), Double::sum);
                    }
                }
            }
        }

        player.getScheduler().run(AuroraQuests.getInstance(), (task) -> {
            var user = AuraSkillsApi.get().getUser(player.getUniqueId());
            if (!user.isLoaded()) return;

            for (var entry : statMap.entrySet()) {
                var statKey = AuraSkillsStatReward.getAURA_SKILLS_STAT() + entry.getKey().getId().toString();

                var oldModifier = user.getStatModifier(statKey);

                if (oldModifier == null) {
                    if (entry.getValue() > 0) {
                        user.addStatModifier(new StatModifier(statKey, entry.getKey(), entry.getValue()));
                    }
                } else if (entry.getValue() <= 0) {
                    user.removeStatModifier(statKey);
                } else if (entry.getValue() != oldModifier.value()) {
                    user.addStatModifier(new StatModifier(statKey, entry.getKey(), entry.getValue()));
                }
            }
        }, null);
    }
}
