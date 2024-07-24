package gg.auroramc.quests.api.data;

import com.google.common.collect.Maps;
import gg.auroramc.aurora.api.user.UserDataHolder;
import gg.auroramc.aurora.api.util.NamespacedId;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class QuestData extends UserDataHolder {
    private final Map<String, PoolRollData> rolledQuests = Maps.newConcurrentMap();
    private final Map<String, Map<String, Map<String, Integer>>> progression = Maps.newConcurrentMap();
    private final Map<String, Integer> poolLevels = Maps.newConcurrentMap();
    private final Map<String, Set<String>> completedQuests = Maps.newConcurrentMap();

    public PoolRollData getPoolRollData(String poolId) {
        return rolledQuests.get(poolId);
    }

    public void setRolledQuests(String poolId, List<String> quests) {
        rolledQuests.put(poolId, new PoolRollData(System.currentTimeMillis(), quests));
    }

    public void progress(String poolId, String questId, String taskId, int count) {
        progression.computeIfAbsent(poolId, k -> Maps.newConcurrentMap())
                .computeIfAbsent(questId, k -> Maps.newConcurrentMap())
                .merge(taskId, count, Integer::sum);
    }

    public void completeQuest(String poolId, String questId) {
        completedQuests.computeIfAbsent(poolId, k -> Set.of()).add(questId);
    }

    public boolean hasCompletedQuest(String poolId, String questId) {
        return completedQuests.computeIfAbsent(poolId, k -> Set.of()).contains(questId);
    }

    public int getProgression(String poolId, String questId, String taskId) {
        return progression.computeIfAbsent(poolId, k -> Maps.newConcurrentMap())
                .computeIfAbsent(questId, k -> Maps.newConcurrentMap())
                .computeIfAbsent(taskId, k -> 0);
    }

    public void clearPoolProgression(String poolId) {
        progression.remove(poolId);
    }

    public int getPoolLevel(String poolId) {
        return poolLevels.getOrDefault(poolId, 0);
    }

    public void setPoolLevel(String poolId, int level) {
        poolLevels.put(poolId, level);
    }

    @Override
    public NamespacedId getId() {
        return NamespacedId.fromDefault("quests");
    }

    @Override
    public void serializeInto(ConfigurationSection data) {
        // Reset
        data.getKeys(false).forEach(key -> data.set(key, null));

        // Roll data
        var rolledSection = data.createSection("rolled");
        for (var entry : rolledQuests.entrySet()) {
            var poolSection = rolledSection.createSection(entry.getKey());
            poolSection.set("time", entry.getValue().timestamp());
            poolSection.set("quests", entry.getValue().quests());
        }

        // Progression data
        var progressionSection = data.createSection("progression");
        for (var poolEntry : progression.entrySet()) {
            var poolSection = progressionSection.createSection(poolEntry.getKey());
            for (var questEntry : poolEntry.getValue().entrySet()) {
                if(completedQuests.get(poolEntry.getKey()).contains(questEntry.getKey())) {
                    poolSection.set(questEntry.getKey(), true);
                    continue;
                }
                var questSection = poolSection.createSection(questEntry.getKey());
                for (var taskEntry : questEntry.getValue().entrySet()) {
                    questSection.set(taskEntry.getKey(), taskEntry.getValue());
                }
            }
        }

        // Pool levels
        var poolLevelsSection = data.createSection("levels");
        for (var entry : poolLevels.entrySet()) {
            poolLevelsSection.set(entry.getKey(), entry.getValue());
        }
    }

    @Override
    public void initFrom(@Nullable ConfigurationSection data) {
        if (data == null) return;
        var rolledSection = data.getConfigurationSection("rolled");
        if (rolledSection != null) {
            for (var key : rolledSection.getKeys(false)) {
                var poolSection = rolledSection.getConfigurationSection(key);
                var quests = poolSection.getStringList("quests");
                rolledQuests.put(key, new PoolRollData(poolSection.getLong("time"), quests));
            }
        }

        var progressionSection = data.getConfigurationSection("progression");
        if (progressionSection != null) {
            for (var poolKey : progressionSection.getKeys(false)) {
                var poolSection = progressionSection.getConfigurationSection(poolKey);
                for (var questKey : poolSection.getKeys(false)) {
                    if (poolSection.isBoolean(questKey)) {
                        completedQuests.computeIfAbsent(poolKey, k -> Set.of()).add(questKey);
                        continue;
                    }
                    var questSection = poolSection.getConfigurationSection(questKey);
                    for (var taskKey : questSection.getKeys(false)) {
                        var count = questSection.getInt(taskKey, 0);
                        progression.computeIfAbsent(poolKey, k -> Maps.newConcurrentMap())
                                .computeIfAbsent(questKey, k -> Maps.newConcurrentMap())
                                .put(taskKey, count);
                    }
                }
            }
        }

        var poolLevelsSection = data.getConfigurationSection("levels");
        if (poolLevelsSection != null) {
            for (var key : poolLevelsSection.getKeys(false)) {
                poolLevels.put(key, poolLevelsSection.getInt(key));
            }
        }
    }
}
