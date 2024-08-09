package gg.auroramc.quests.api.quest;

import com.google.common.collect.Maps;
import gg.auroramc.aurora.api.AuroraAPI;
import gg.auroramc.aurora.api.reward.*;
import gg.auroramc.aurora.api.util.NamespacedId;
import gg.auroramc.quests.AuroraQuests;
import gg.auroramc.quests.api.event.QuestsLoadedEvent;
import gg.auroramc.quests.api.quest.task.LevelledTaskEvaluator;
import gg.auroramc.quests.hooks.HookManager;
import gg.auroramc.quests.hooks.worldguard.WorldGuardHook;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.quartz.SchedulerException;
import org.quartz.impl.StdSchedulerFactory;

import java.util.*;
import java.util.concurrent.CompletableFuture;

public class QuestManager {
    @Getter
    private final RewardFactory rewardFactory = new RewardFactory();
    @Getter
    private final RewardAutoCorrector rewardAutoCorrector = new RewardAutoCorrector();
    private final Map<UUID, Object> playerLocks = Maps.newConcurrentMap();

    private final AuroraQuests plugin;
    private final Map<String, QuestPool> pools = Maps.newConcurrentMap();

    public QuestManager(AuroraQuests plugin) {
        this.plugin = plugin;
        rewardFactory.registerRewardType(NamespacedId.fromDefault("command"), CommandReward.class);
        rewardFactory.registerRewardType(NamespacedId.fromDefault("money"), MoneyReward.class);
        rewardFactory.registerRewardType(NamespacedId.fromDefault("item"), ItemReward.class);

        TaskManager.registerEvaluator(TaskType.ENCHANT, new LevelledTaskEvaluator());
        TaskManager.registerEvaluator(TaskType.KILL_LEVELLED_MOB, new LevelledTaskEvaluator());

        try {
            StdSchedulerFactory.getDefaultScheduler().start();
        } catch (SchedulerException e) {
            AuroraQuests.logger().severe("Failed to start scheduler: " + e.getMessage());
        }
    }

    public void reload() {
        if (!pools.isEmpty()) {
            pools.values().forEach(QuestPool::dispose);
        }
        pools.clear();
        for (var poolEntry : plugin.getConfigManager().getQuestPools().entrySet()) {
            pools.put(poolEntry.getKey(), new QuestPool(poolEntry.getValue(), rewardFactory));
        }
        Bukkit.getPluginManager().callEvent(new QuestsLoadedEvent());
    }

    private Object getPlayerLock(Player player) {
        return playerLocks.computeIfAbsent(player.getUniqueId(), (key) -> new Object());
    }

    public void handlePlayerQuit(UUID playerId) {
        playerLocks.remove(playerId);
    }

    public void progress(Player player, String taskType, double amount, Map<String, Object> params) {
        if (!player.hasPermission("aurora.quests.use")) return;
        if (plugin.getConfigManager().getConfig().getPreventCreativeMode() && player.getGameMode() == GameMode.CREATIVE)
            return;
        var user = AuroraAPI.getUserManager().getUser(player);
        if (!user.isLoaded()) return;

        if (HookManager.isEnabled(WorldGuardHook.class)) {
            if (HookManager.getHook(WorldGuardHook.class).isBlocked(player)) return;
        }

        CompletableFuture.runAsync(() -> actuallyProgress(player, taskType, amount, params));
    }

    private void actuallyProgress(Player player, String taskType, double amount, Map<String, Object> params) {
        synchronized (getPlayerLock(player)) {
            for (var pool : pools.values()) {
                if (!pool.hasTaskType(taskType)) continue;
                if (!pool.isUnlocked(player)) continue;
                for (var quest : pool.getNotCompletedPlayerQuests(player)) {
                    quest.progress(player, taskType, amount, params);
                }
            }
        }
    }

    public Collection<QuestPool> getQuestPools() {
        return pools.values();
    }

    public QuestPool getQuestPool(String id) {
        return pools.get(id);
    }

    public List<QuestPool> rollQuestsIfNecessary(Player player) {
        var rolledPools = new ArrayList<QuestPool>();

        for (var pool : pools.values()) {
            if (pool.rollIfNecessary(player, false)) {
                rolledPools.add(pool);
            }
        }

        return rolledPools;
    }

    public void tryStartGlobalQuests(Player player) {
        for (var pool : pools.values()) {
            if (pool.isGlobal()) {
                pool.tryStartGlobalQuests(player);
            }
        }
    }

    public void tryUnlockQuestPools(Player player) {
        for (var pool : pools.values()) {
            pool.tryUnlock(player);
        }
    }
}
