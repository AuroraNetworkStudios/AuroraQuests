package gg.auroramc.quests.api.quest;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import gg.auroramc.aurora.api.AuroraAPI;
import gg.auroramc.aurora.api.item.TypeId;
import gg.auroramc.aurora.api.levels.MatcherManager;
import gg.auroramc.aurora.api.message.Chat;
import gg.auroramc.aurora.api.message.Placeholder;
import gg.auroramc.aurora.api.message.Text;
import gg.auroramc.aurora.api.reward.RewardExecutor;
import gg.auroramc.aurora.api.reward.RewardFactory;
import gg.auroramc.quests.AuroraQuests;
import gg.auroramc.quests.api.data.QuestData;
import gg.auroramc.quests.config.quest.PoolConfig;
import gg.auroramc.quests.util.RomanNumber;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.time.Duration;
import java.util.*;

public class QuestPool {
    @Getter
    private final PoolConfig config;
    @Getter
    private MatcherManager matcherManager;
    private QuestRollerScheduler questRoller;
    private final Map<String, Quest> quests = Maps.newHashMap();
    private Set<String> taskTypes = Sets.newHashSet();

    public QuestPool(PoolConfig config, RewardFactory rewardFactory) {
        this.config = config;

        if (config.getLeveling().getEnabled()) {
            matcherManager = new MatcherManager(rewardFactory);
            matcherManager.reload(config.getLeveling().getLevelMatchers(), config.getLeveling().getCustomLevels());
        }

        for (var q : config.getQuests().entrySet()) {
            var quest = new Quest(this, q.getValue(), rewardFactory);
            quests.put(q.getKey(), quest);
            this.taskTypes.addAll(quest.getTaskTypes());
        }

        if (isTimedRandom()) {
            if (isResetFrequencyValid()) {
                questRoller = new QuestRollerScheduler(this);
            } else {
                AuroraQuests.logger().warning("Invalid reset frequency: " + config.getResetFrequency() + " for pool " + config.getId());
            }
        }

        AuroraQuests.logger().debug("Loaded difficulties for pool " + config.getId() + ": " + String.join(", ", config.getDifficulties().keySet()));
    }

    private boolean canUnlock(Player player) {
        var data = AuroraAPI.getUserManager().getUser(player).getData(QuestData.class);

        if (config.getUnlockRequirements() == null) return true;

        if (config.getUnlockRequirements().getQuests() != null) {
            for (var questId : config.getUnlockRequirements().getQuests()) {
                var typeId = TypeId.fromString(questId);
                var pool = typeId.namespace().equals("minecraft") ? getId() : typeId.namespace();
                if (!data.hasCompletedQuest(pool, typeId.id())) {
                    return false;
                }
            }
        }

        if (config.getUnlockRequirements().getPermissions() != null) {
            for (var perm : config.getUnlockRequirements().getPermissions()) {
                if (!player.hasPermission(perm)) {
                    return false;
                }
            }
        }

        return true;
    }

    private boolean hasStartRequirements() {
        return config.getUnlockRequirements() != null && ((config.getUnlockRequirements().getQuests() != null && !config.getUnlockRequirements().getQuests().isEmpty()) ||
                (config.getUnlockRequirements().getPermissions() != null && !config.getUnlockRequirements().getPermissions().isEmpty()));
    }

    public boolean isUnlocked(Player player) {
        var data = AuroraAPI.getUserManager().getUser(player).getData(QuestData.class);
        return !hasStartRequirements() || data.isPoolUnlocked(getId());
    }

    public void tryUnlock(Player player) {
        if (isUnlocked(player)) return;
        var data = getQuestData(player);

        if (canUnlock(player)) {
            data.unlockPool(getId());
            var msg = AuroraQuests.getInstance().getConfigManager().getMessageConfig().getPoolUnlocked();
            Chat.sendMessage(player, msg, Placeholder.of("{pool}", config.getName()));
            tryStartGlobalQuests(player);
            reRollQuests(player, false);
        }
    }

    public boolean hasTaskType(String taskType) {
        return taskTypes.contains(taskType);
    }

    private boolean isResetFrequencyValid() {
        return config.getResetFrequency() != null && config.getResetFrequency().split("\\s").length == 6;
    }

    public String getId() {
        return config.getId();
    }

    public boolean isGlobal() {
        return !isTimedRandom();
    }

    public boolean isTimedRandom() {
        return config.getType().equals("timed-random");
    }

    public boolean hasLeveling() {
        return config.getLeveling().getEnabled();
    }

    public Quest getQuest(String id) {
        return quests.get(id);
    }

    private QuestData getQuestData(Player player) {
        return AuroraAPI.getUserManager().getUser(player).getData(QuestData.class);
    }

    public List<Quest> getPlayerQuests(Player player) {
        if (isGlobal()) return quests.values().stream().toList();
        var data = getQuestData(player);
        var rolledQuests = data.getPoolRollData(config.getId());
        return rolledQuests.quests().stream().map(this::getQuest).filter(Objects::nonNull).toList();
    }

    public List<Quest> getNotCompletedPlayerQuests(Player player) {
        return getPlayerQuests(player).stream().filter(q -> !q.isCompleted(player)).toList();
    }

    public int getPlayerLevel(Player player) {
        if (!hasLeveling()) return 0;

        var completed = getQuestData(player).getCompletedCount(getId());
        var requirements = config.getLeveling().getRequirements();

        for (int i = requirements.size() - 1; i >= 0; i--) {
            if (completed >= requirements.get(i)) {
                return i + 1;
            }
        }

        return 0;
    }

    public List<Placeholder<?>> getLevelPlaceholders(Player player, int level) {
        var prevLevel = Math.max(0, level - 1);

        return List.of(
                Placeholder.of("{player}", player.getName()),
                Placeholder.of("{level_raw}", level),
                Placeholder.of("{level}", AuroraAPI.formatNumber(level)),
                Placeholder.of("{level_roman}", RomanNumber.toRoman(level)),
                Placeholder.of("{prev_level_raw}", prevLevel),
                Placeholder.of("{prev_level}", AuroraAPI.formatNumber(prevLevel)),
                Placeholder.of("{prev_level_roman}", RomanNumber.toRoman(prevLevel)),
                Placeholder.of("{pool}", config.getName()),
                Placeholder.of("{pool_id}", getId())
        );
    }

    public void reward(Player player, int level) {
        if (!hasLeveling()) return;

        var mc = AuroraQuests.getInstance().getConfigManager().getConfig();
        var rewards = matcherManager.getBestMatcher(level).computeRewards(level);

        List<Placeholder<?>> placeholders = getLevelPlaceholders(player, level);

        if (mc.getLevelUpMessage().getEnabled()) {
            var text = Component.text();
            var messageLines = mc.getLevelUpMessage().getMessage();

            for (var line : messageLines) {
                if (line.equals("component:rewards")) {
                    if (!rewards.isEmpty()) {
                        text.append(Text.component(player, mc.getDisplayComponents().get("rewards").getTitle(), placeholders));
                    }
                    for (var reward : rewards) {
                        text.append(Component.newline());
                        var display = mc.getDisplayComponents().get("rewards").getLine().replace("{reward}", reward.getDisplay(player, placeholders));
                        text.append(Text.component(player, display, placeholders));
                    }
                } else {
                    text.append(Text.component(player, line, placeholders));
                }

                if (!line.equals(messageLines.getLast())) text.append(Component.newline());
            }

            player.sendMessage(text);
        }

        if (mc.getLevelUpSound().getEnabled()) {
            var sound = mc.getQuestCompleteSound();
            player.playSound(player.getLocation(),
                    Sound.valueOf(sound.getSound().toUpperCase()),
                    sound.getVolume(),
                    sound.getPitch());
        }

        RewardExecutor.execute(rewards, player, level, placeholders);
    }

    public Collection<Quest> getQuests() {
        return quests.values();
    }

    public void tryStartGlobalQuests(Player player) {
        if (!isGlobal()) return;
        if (!isUnlocked(player)) return;
        for (var quest : quests.values()) {
            quest.tryStart(player);
        }
    }

    public boolean rollIfNecessary(Player player, boolean sendNotification) {
        AuroraQuests.logger().debug("Checking if player " + player.getName() + " needs to reroll quests for pool " + config.getId());
        if (!isTimedRandom()) return false;
        if (!questRoller.isValid()) return false;
        if (!isUnlocked(player)) return false;
        AuroraQuests.logger().debug("Pool is timed random and quest roller is valid");

        try {
            var data = getQuestData(player);

            var rollData = data.getPoolRollData(getId());
            boolean hasInvalidQuests = rollData != null && rollData.quests().stream().anyMatch(q -> !quests.containsKey(q));


            if (rollData == null || questRoller.shouldReroll(rollData.timestamp()) || hasInvalidQuests) {
                reRollQuests(player, sendNotification);
                return true;
            }
        } catch (Exception e) {
            AuroraQuests.logger().severe("Failed to reroll quests for player " + player.getName() + " in pool " + config.getId() + ": " + e.getMessage());
        }

        return false;
    }

    public void reRollQuests(Player player, boolean sendNotification) {
        if (isGlobal()) return;
        // difficulty -> quest
        var pickedQuests = new HashMap<String, List<Quest>>();
        var difficulties = config.getDifficulties();

        var questsToSelectFrom = quests.values().stream()
                .filter(q -> difficulties.containsKey(q.getDifficulty()) && q.canStart(player))
                .toList();

        AuroraQuests.logger().debug("Picking quests from pool " + config.getId() + " for player " + player.getName() + " with " + questsToSelectFrom.size() + " quests");

        var pickableQuests = new HashMap<String, List<Quest>>();

        for (var quest : questsToSelectFrom) {
            if (quest.getDifficulty() == null) continue;
            pickableQuests.computeIfAbsent(quest.getDifficulty(), k -> new ArrayList<>()).add(quest);
        }

        for (var difficulty : pickableQuests.keySet()) {
            var quests = pickableQuests.get(difficulty);
            Collections.shuffle(quests);
        }

        for (var difficulty : difficulties.entrySet()) {
            var quests = pickableQuests.get(difficulty.getKey());
            if (quests == null || quests.isEmpty()) {
                pickedQuests.put(difficulty.getKey(), Collections.emptyList());
                continue;
            }
            pickedQuests.put(difficulty.getKey(), quests.subList(0, Math.min(difficulty.getValue(), quests.size())));
        }

        var data = getQuestData(player);
        var questIds = pickedQuests.values().stream().flatMap(List::stream).map(Quest::getId).toList();
        data.setRolledQuests(getId(), questIds);

        AuroraQuests.logger().debug("Rolled quests for player " + player.getName() + " in pool " + config.getId() + ": " + String.join(", ", questIds));

        if (sendNotification) {
            var msg = AuroraQuests.getInstance().getConfigManager().getMessageConfig().getReRolledTarget();
            Chat.sendMessage(player, msg, Placeholder.of("{pool}", config.getName()));
        }
    }

    public long getCompletedQuestCount(Player player) {
        return getQuestData(player).getCompletedCount(getId());
    }

    public Duration getDurationUntilNextRoll() {
        if (!isTimedRandom()) return Duration.ZERO;
        return questRoller.getDurationUntilNextRoll();
    }

    public void dispose() {
        if (questRoller != null) {
            questRoller.shutdown();
        }
    }
}
