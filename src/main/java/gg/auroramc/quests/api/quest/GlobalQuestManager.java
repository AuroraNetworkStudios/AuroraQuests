package gg.auroramc.quests.api.quest;

import gg.auroramc.aurora.api.message.Placeholder;
import gg.auroramc.aurora.api.reward.RewardExecutor;
import gg.auroramc.quests.AuroraQuests;
import gg.auroramc.quests.api.data.GlobalQuestData;
import gg.auroramc.quests.api.data.GlobalQuestMySQL;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manager for global shared quests.
 * Requires MySQL for cross-server synchronization.
 * Reads MySQL configuration from Aurora's config.yml.
 */
@Getter
public class GlobalQuestManager {
    private final GlobalQuestData data = new GlobalQuestData();
    private GlobalQuestMySQL mysql;
    private BukkitTask syncTask;
    private final Set<String> processingMilestones = ConcurrentHashMap.newKeySet();

    private boolean enabled = false;
    private int syncInterval = 20; // seconds

    /**
     * Initialize the manager - REQUIRES MySQL
     */
    public void init(boolean enabled, int syncInterval) {
        this.enabled = enabled;
        this.syncInterval = syncInterval;

        if (!enabled) {
            AuroraQuests.logger().info("Global quests are disabled");
            return;
        }

        try {
            // Read MySQL config from Aurora's config.yml
            var auroraConfig = Bukkit.getPluginManager().getPlugin("Aurora").getConfig();

            // Check if Aurora is using MySQL (storage-type: "mysql" or mysql.enabled: true)
            boolean mysqlEnabled = "mysql".equalsIgnoreCase(auroraConfig.getString("storage-type", ""))
                                || auroraConfig.getBoolean("mysql.enabled", false);

            if (mysqlEnabled && auroraConfig.contains("mysql")) {
                // MySQL is configured - initialize global quests
                String host = auroraConfig.getString("mysql.host", "localhost");
                int port = auroraConfig.getInt("mysql.port", 3306);
                String database = auroraConfig.getString("mysql.database", "aurora");
                String username = auroraConfig.getString("mysql.username", "root");
                String password = auroraConfig.getString("mysql.password", "");

                mysql = new GlobalQuestMySQL(host, port, database, username, password);
                mysql.connect();
                mysql.loadGlobalData(data);

                // Start periodic sync task
                startSyncTask();

                AuroraQuests.logger().info("Global quest manager initialized with MySQL");
                AuroraQuests.logger().info("Database: " + database + " at " + host + ":" + port);
                AuroraQuests.logger().info("Sync interval: " + syncInterval + " seconds");
            } else {
                // MySQL not configured - disable global quests
                enabled = false;
                AuroraQuests.logger().warning("╔════════════════════════════════════════════════════════════════╗");
                AuroraQuests.logger().warning("║  Global quests REQUIRE MySQL to function!                     ║");
                AuroraQuests.logger().warning("║  Configure MySQL in Aurora/config.yml to enable global quests ║");
                AuroraQuests.logger().warning("║  Global quest pools will be disabled.                         ║");
                AuroraQuests.logger().warning("╚════════════════════════════════════════════════════════════════╝");
            }
        } catch (Exception e) {
            // Failed to initialize MySQL - disable global quests
            enabled = false;
            AuroraQuests.logger().severe("╔════════════════════════════════════════════════════════════════╗");
            AuroraQuests.logger().severe("║  Failed to initialize MySQL for global quests!                ║");
            AuroraQuests.logger().severe("║  Error: " + e.getMessage());
            AuroraQuests.logger().severe("║  Global quest pools will be disabled.                         ║");
            AuroraQuests.logger().severe("╚════════════════════════════════════════════════════════════════╝");
            e.printStackTrace();
        }
    }

    /**
     * Shutdown the manager
     */
    public void shutdown() {
        if (syncTask != null) {
            syncTask.cancel();
        }

        if (mysql != null) {
            saveAll();
            mysql.disconnect();
        }
    }

    /**
     * Start the periodic sync task - polls MySQL for updates from other servers
     */
    private void startSyncTask() {
        long tickInterval = syncInterval * 20L; // Convert seconds to ticks

        syncTask = Bukkit.getScheduler().runTaskTimerAsynchronously(AuroraQuests.getInstance(), () -> {
            try {
                syncFromDatabase();
            } catch (Exception e) {
                AuroraQuests.logger().severe("Error during global quest sync: " + e.getMessage());
                e.printStackTrace();
            }
        }, tickInterval, tickInterval);
    }

    /**
     * Sync data from database (polling)
     */
    private void syncFromDatabase() {
        if (mysql == null || !mysql.isConnected()) return;

        // Save current data first to avoid losing in-flight changes
        saveAll();

        // Then reload all data from MySQL to get updates from other servers
        mysql.loadGlobalData(data);
    }

    /**
     * Save all current data to database
     */
    public void saveAll() {
        if (mysql == null || !mysql.isConnected()) return;

        try {
            // Save progress
            for (String questId : data.getGlobalProgress().keySet()) {
                for (Map.Entry<String, Long> entry : data.getGlobalProgress().get(questId).entrySet()) {
                    mysql.saveProgress(questId, entry.getKey(), entry.getValue());
                }
            }

            // Save contributions
            for (String questId : data.getContributions().keySet()) {
                for (Map.Entry<UUID, Long> entry : data.getContributions().get(questId).entrySet()) {
                    mysql.saveContribution(questId, entry.getKey(), entry.getValue());
                }
            }

            // Save milestones
            for (String questId : data.getReachedMilestones().keySet()) {
                for (Integer percentage : data.getReachedMilestones().get(questId)) {
                    mysql.saveMilestone(questId, percentage);
                }
            }
        } catch (Exception e) {
            AuroraQuests.logger().severe("Error saving global quest data: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Increment progress for a global quest
     */
    public void incrementProgress(String poolId, String questId, String taskId, UUID playerId, long amount, QuestDefinition questDef) {
        if (!enabled) return;

        // Update local cache
        data.incrementProgress(questId, taskId, playerId, amount);

        // Save to database asynchronously
        if (mysql != null) {
            Bukkit.getScheduler().runTaskAsynchronously(AuroraQuests.getInstance(), () -> {
                if (mysql.isConnected()) {
                    mysql.saveProgress(questId, taskId, data.getProgress(questId, taskId));
                    mysql.saveContribution(questId, playerId, data.getContribution(questId, playerId));
                }
            });
        }

        // Check for milestones
        checkMilestones(poolId, questId, taskId, questDef);
    }

    /**
     * Check if any milestones have been reached
     */
    private void checkMilestones(String poolId, String questId, String taskId, QuestDefinition questDef) {
        if (questDef == null || questDef.getMilestones() == null) return;

        var taskDef = questDef.getTasks().get(taskId);
        if (taskDef == null) return;

        long current = data.getProgress(questId, taskId);
        long target = (long) taskDef.getArgs().getDouble("amount", 1);

        if (target <= 0) return;

        // Check each milestone percentage
        for (int percentage : questDef.getMilestones().keySet()) {
            long milestoneThreshold = (target * percentage) / 100;

            if (current >= milestoneThreshold && !data.isMilestoneReached(questId, percentage)) {
                // Try to mark milestone (thread-safe)
                String milestoneKey = questId + ":" + percentage;

                // Prevent duplicate processing across servers
                if (processingMilestones.add(milestoneKey)) {
                    boolean wasNew = data.markMilestone(questId, percentage);

                    if (wasNew) {
                        // Save to database immediately
                        if (mysql != null && mysql.isConnected()) {
                            mysql.saveMilestone(questId, percentage);
                        }

                        // Distribute rewards on main thread
                        Bukkit.getScheduler().runTask(AuroraQuests.getInstance(), () -> {
                            distributeMilestoneReward(poolId, questId, percentage, questDef);
                            processingMilestones.remove(milestoneKey);
                        });
                    } else {
                        processingMilestones.remove(milestoneKey);
                    }
                }
            }
        }
    }

    /**
     * Distribute milestone rewards to contributors only (online get it immediately, offline get it on login)
     */
    private void distributeMilestoneReward(String poolId, String questId, int percentage, QuestDefinition questDef) {
        var rewards = questDef.getMilestones().get(percentage);
        if (rewards == null || rewards.isEmpty()) return;

        AuroraQuests.logger().info("Global quest milestone reached: " + questId + " - " + percentage + "%");

        // Get placeholders
        List<Placeholder<?>> placeholders = List.of(
                Placeholder.of("{quest}", questDef.getName()),
                Placeholder.of("{pool}", poolId),
                Placeholder.of("{percentage}", percentage),
                Placeholder.of("{milestone}", percentage + "%")
        );

        // Get all contributors for this quest
        var contributors = data.getContributions().get(questId);
        if (contributors == null || contributors.isEmpty()) {
            AuroraQuests.logger().warning("No contributors found for global quest: " + questId);
            return;
        }

        int onlineRewards = 0;
        int offlineRewards = 0;

        // Iterate through all contributors
        for (UUID contributorId : contributors.keySet()) {
            var player = Bukkit.getPlayer(contributorId);

            if (player != null && player.isOnline()) {
                // Player is online - give reward immediately
                var user = gg.auroramc.aurora.api.user.AuroraUser.get(contributorId);
                if (user != null) {
                    var questData = user.getData(gg.auroramc.quests.api.data.QuestData.class);
                    if (questData != null && !questData.hasClaimedGlobalMilestone(questId, percentage)) {
                        try {
                            RewardExecutor.execute(rewards, player, percentage, placeholders);
                            questData.markGlobalMilestoneClaimed(questId, percentage);
                            onlineRewards++;
                        } catch (Exception e) {
                            AuroraQuests.logger().severe("Error executing milestone reward for " + player.getName() + ": " + e.getMessage());
                        }
                    }
                }
            } else {
                // Player is offline - they'll get it on login
                offlineRewards++;
            }
        }

        AuroraQuests.logger().info("Milestone rewards: " + onlineRewards + " online, " + offlineRewards + " pending for offline contributors");
    }

    /**
     * Get global progress for a quest task
     */
    public long getProgress(String questId, String taskId) {
        return data.getProgress(questId, taskId);
    }

    /**
     * Get player's contribution to a quest
     */
    public long getContribution(String questId, UUID playerId) {
        return data.getContribution(questId, playerId);
    }

    /**
     * Get top contributors for a quest
     */
    public List<Map.Entry<UUID, Long>> getTopContributors(String questId, int limit) {
        return data.getTopContributors(questId, limit);
    }

    /**
     * Check if a milestone has been reached
     */
    public boolean isMilestoneReached(String questId, int percentage) {
        return data.isMilestoneReached(questId, percentage);
    }

    /**
     * Reset a global quest (admin command)
     */
    public void resetQuest(String questId) {
        data.resetQuest(questId);
        if (mysql != null && mysql.isConnected()) {
            mysql.resetQuest(questId);
        }
        AuroraQuests.logger().info("Reset global quest: " + questId);
    }

    /**
     * Force a sync from database
     */
    public void forceSync() {
        syncFromDatabase();
    }

    /**
     * Check and distribute pending milestone rewards for a player who just logged in
     */
    public void distributePendingMilestoneRewards(org.bukkit.entity.Player player, gg.auroramc.quests.api.data.QuestData questData) {
        if (!enabled) return;

        // Iterate through all global quests this player contributed to
        for (Map.Entry<String, Map<UUID, Long>> questEntry : data.getContributions().entrySet()) {
            String questId = questEntry.getKey();
            Map<UUID, Long> contributors = questEntry.getValue();

            // Check if this player is a contributor
            if (!contributors.containsKey(player.getUniqueId())) {
                continue;
            }

            // Get reached milestones for this quest
            Set<Integer> reachedMilestones = data.getReachedMilestones().get(questId);
            if (reachedMilestones == null || reachedMilestones.isEmpty()) {
                continue;
            }

            // Find the quest definition to get rewards
            var poolManager = AuroraQuests.getInstance().getPoolManager();
            QuestDefinition questDef = null;
            String poolId = null;

            // Search for the quest definition across all pools
            for (var pool : poolManager.getPools()) {
                var def = pool.getDefinition().getQuests().get(questId);
                if (def != null) {
                    questDef = def;
                    poolId = pool.getId();
                    break;
                }
            }

            if (questDef == null || questDef.getMilestones() == null) {
                continue;
            }

            // Check each reached milestone
            for (Integer percentage : reachedMilestones) {
                // If player hasn't claimed this milestone yet, give the reward
                if (!questData.hasClaimedGlobalMilestone(questId, percentage)) {
                    var rewards = questDef.getMilestones().get(percentage);
                    if (rewards != null && !rewards.isEmpty()) {
                        List<Placeholder<?>> placeholders = List.of(
                                Placeholder.of("{quest}", questDef.getName()),
                                Placeholder.of("{pool}", poolId),
                                Placeholder.of("{percentage}", percentage),
                                Placeholder.of("{milestone}", percentage + "%")
                        );

                        try {
                            RewardExecutor.execute(rewards, player, percentage, placeholders);
                            questData.markGlobalMilestoneClaimed(questId, percentage);
                            AuroraQuests.logger().info("Distributed pending milestone reward to " + player.getName() + " for " + questId + " - " + percentage + "%");
                        } catch (Exception e) {
                            AuroraQuests.logger().severe("Error distributing pending milestone reward to " + player.getName() + ": " + e.getMessage());
                        }
                    }
                }
            }
        }
    }
}
