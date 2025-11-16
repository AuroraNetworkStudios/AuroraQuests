package gg.auroramc.quests.api.data;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory storage for global quest data.
 * Thread-safe data structures for tracking cross-server quest progress.
 */
public class GlobalQuestData {
    // Global progress: questId -> taskId -> progress
    private final Map<String, Map<String, Long>> globalProgress = new ConcurrentHashMap<>();

    // Player contributions: questId -> playerId -> contribution
    private final Map<String, Map<UUID, Long>> contributions = new ConcurrentHashMap<>();

    // Reached milestones: questId -> set of percentages
    private final Map<String, Set<Integer>> reachedMilestones = new ConcurrentHashMap<>();

    /**
     * Increment progress for a quest task and track player contribution
     */
    public synchronized void incrementProgress(String questId, String taskId, UUID playerId, long amount) {
        globalProgress.computeIfAbsent(questId, k -> new ConcurrentHashMap<>())
                .merge(taskId, amount, Long::sum);
        contributions.computeIfAbsent(questId, k -> new ConcurrentHashMap<>())
                .merge(playerId, amount, Long::sum);
    }

    /**
     * Set progress for a quest task (used when loading from database)
     */
    public void setProgress(String questId, String taskId, long progress) {
        globalProgress.computeIfAbsent(questId, k -> new ConcurrentHashMap<>())
                .put(taskId, progress);
    }

    /**
     * Get current progress for a quest task
     */
    public long getProgress(String questId, String taskId) {
        return globalProgress.getOrDefault(questId, new ConcurrentHashMap<>())
                .getOrDefault(taskId, 0L);
    }

    /**
     * Get player's contribution to a quest
     */
    public long getContribution(String questId, UUID playerId) {
        return contributions.getOrDefault(questId, new ConcurrentHashMap<>())
                .getOrDefault(playerId, 0L);
    }

    /**
     * Get top contributors for a quest
     */
    public List<Map.Entry<UUID, Long>> getTopContributors(String questId, int limit) {
        var questContribs = contributions.getOrDefault(questId, new ConcurrentHashMap<>());
        return questContribs.entrySet().stream()
                .sorted(Map.Entry.<UUID, Long>comparingByValue().reversed())
                .limit(limit)
                .toList();
    }

    /**
     * Mark a milestone as reached (thread-safe)
     */
    public synchronized boolean markMilestone(String questId, int percentage) {
        var milestones = reachedMilestones.computeIfAbsent(questId, k -> ConcurrentHashMap.newKeySet());
        return milestones.add(percentage);
    }

    /**
     * Check if a milestone has been reached
     */
    public boolean isMilestoneReached(String questId, int percentage) {
        return reachedMilestones.getOrDefault(questId, ConcurrentHashMap.newKeySet())
                .contains(percentage);
    }

    /**
     * Reset all data for a quest
     */
    public void resetQuest(String questId) {
        globalProgress.remove(questId);
        contributions.remove(questId);
        reachedMilestones.remove(questId);
    }

    /**
     * Get all global progress data (for saving to database)
     */
    public Map<String, Map<String, Long>> getGlobalProgress() {
        return globalProgress;
    }

    /**
     * Get all contribution data (for saving to database)
     */
    public Map<String, Map<UUID, Long>> getContributions() {
        return contributions;
    }

    /**
     * Get all reached milestones (for saving to database)
     */
    public Map<String, Set<Integer>> getReachedMilestones() {
        return reachedMilestones;
    }
}
