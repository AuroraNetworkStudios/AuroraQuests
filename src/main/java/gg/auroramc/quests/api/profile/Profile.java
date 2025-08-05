package gg.auroramc.quests.api.profile;

import gg.auroramc.aurora.api.message.Chat;
import gg.auroramc.aurora.api.message.Placeholder;
import gg.auroramc.aurora.api.user.AuroraUser;
import gg.auroramc.quests.AuroraQuests;
import gg.auroramc.quests.api.data.QuestData;
import gg.auroramc.quests.api.questpool.QuestPool;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.TimeUnit;

@Getter
public class Profile {
    private final AuroraUser user;
    private final Player player;

    private final Map<String, QuestPool> data = new LinkedHashMap<>();

    public Profile(AuroraUser user) {
        this.user = user;
        this.player = user.getPlayer();
        reload(false);
    }

    public void reload(boolean destroy) {
        for (var pool : data.values()) {
            if (destroy) {
                pool.destroy();
            } else {
                pool.dispose();
            }
        }

        data.clear();

        Set<QuestPool> rolledPools = new HashSet<>();

        for (var pool : AuroraQuests.getInstance().getPoolManager().getPools()) {
            var questPool = new QuestPool(this, pool);
            data.put(pool.getId(), questPool);
            questPool.unlock(false);

            if (questPool.rollIfNecessary(false)) {
                rolledPools.add(questPool);
            }

            questPool.startQuests();
        }

        if (!rolledPools.isEmpty() && player.hasPlayedBefore()) {
            var msg = AuroraQuests.getInstance().getConfigManager().getMessageConfig().getReRolledTarget();
            Chat.sendMessage(player, msg, Placeholder.of("{pool}", String.join(", ", rolledPools.stream().map(QuestPool::getName).toList())));
        }

        if (AuroraQuests.getInstance().getConfigManager().getConfig().getPurgeInvalidDataOnLogin()) {
            getData().purgeInvalidData(AuroraQuests.getInstance().getPoolManager().getPools());
        }

        Bukkit.getAsyncScheduler().runDelayed(AuroraQuests.getInstance(),
                (t) -> AuroraQuests.getInstance().getPoolManager().getRewardAutoCorrector().correctRewards(player), 50, TimeUnit.MILLISECONDS);
    }

    public Collection<QuestPool> getQuestPools() {
        return data.values();
    }

    public QuestPool getQuestPool(String id) {
        return data.get(id);
    }

    public TaskDataWrapper toTaskDataWrapper(String poolId, String questId, String taskId) {
        return new TaskDataWrapper(this, poolId, questId, taskId);
    }

    public QuestDataWrapper toQuestDataWrapper(String poolId, String questId) {
        return new QuestDataWrapper(this, poolId, questId);
    }

    public QuestData getData() {
        return user.getData(QuestData.class);
    }

    public void destroy() {
        for (var pool : data.values()) {
            pool.destroy();
        }
        data.clear();
    }

    @Override
    public int hashCode() {
        return user.getUniqueId().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Profile profile) {
            return user == profile.user;
        }
        return false;
    }


    public record TaskDataWrapper(Profile profile, String poolId, String questId, String taskId) {
        public boolean isCompleted(double target) {
            var data = profile.getData();
            return data.hasCompletedQuest(poolId, questId) || data.getProgression(poolId, questId, taskId) >= target;
        }

        public double getProgress() {
            return profile.getData().getProgression(poolId, questId, taskId);
        }

        public void progress(double amount) {
            profile.getData().progress(poolId, questId, taskId, amount);
        }

        public void setProgress(double amount) {
            profile.getData().setProgress(poolId, questId, taskId, amount);
        }

        public void resetProgress() {
            profile.getData().resetTaskProgress(poolId, questId, taskId);
        }
    }


    public record QuestDataWrapper(Profile profile, String poolId, String questId) {
        public void complete() {
            var data = profile.getData();
            data.completeQuest(poolId, questId);
        }

        public void reset() {
            profile.getData().resetQuestProgress(poolId, questId);
        }

        public boolean isCompleted() {
            return profile.getData().hasCompletedQuest(poolId, questId);
        }

        public boolean isUnlocked() {
            return profile.getData().isQuestStartUnlocked(poolId, questId);
        }

        public void unlock() {
            profile.getData().setQuestStartUnlock(poolId, questId);
        }

        public boolean hasCompletedQuest(String poolId, String questId) {
            return profile.getData().hasCompletedQuest(poolId, questId);
        }

        public TaskDataWrapper toTaskDataWrapper(String taskId) {
            return new TaskDataWrapper(profile, poolId, questId, taskId);
        }
    }
}
