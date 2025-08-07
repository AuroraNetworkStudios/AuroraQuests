package gg.auroramc.quests.api.questpool;

import gg.auroramc.aurora.api.AuroraAPI;
import gg.auroramc.aurora.api.message.Chat;
import gg.auroramc.aurora.api.message.Placeholder;
import gg.auroramc.aurora.api.reward.RewardExecutor;
import gg.auroramc.quests.AuroraQuests;
import gg.auroramc.quests.api.event.EventType;
import gg.auroramc.quests.api.event.QuestPoolLevelUpEvent;
import gg.auroramc.quests.api.profile.Profile;
import gg.auroramc.quests.api.quest.Quest;
import gg.auroramc.quests.util.RewardUtil;
import gg.auroramc.quests.util.RomanNumber;
import gg.auroramc.quests.util.SoundUtil;
import lombok.Getter;
import org.bukkit.Bukkit;

import java.time.Duration;
import java.util.*;

@Getter
public class QuestPool {
    private final Pool pool;
    private final Profile profile;
    private final Map<String, Quest> quests = new LinkedHashMap<>();

    public QuestPool(Profile profile, Pool pool) {
        this.profile = profile;
        this.pool = pool;

        for (var questDef : pool.getDefinition().getQuests().values()) {
            var quest = new Quest(this, questDef, profile.toQuestDataWrapper(getId(), questDef.getId()));
            quests.put(questDef.getId(), quest);

            quest.subscribe(EventType.QUEST_COMPLETED, (t) -> {
                var currentLevel = getLevel();

                profile.getData().incrementCompletedCount(getId());
                if (!isGlobal() || AuroraQuests.getInstance().getConfigManager().getConfig().getLeaderboards().getIncludeGlobal()) {
                    AuroraAPI.getLeaderboards().updateUser(profile.getUser(), "quests_" + getId());
                }

                var newLevel = getLevel();
                if (hasLeveling() && newLevel > currentLevel) {
                    reward(newLevel);
                    Bukkit.getPluginManager().callEvent(new QuestPoolLevelUpEvent(profile.getPlayer(), this));
                }
            });
        }
    }

    public void dispose() {
        for (var quest : quests.values()) {
            quest.dispose();
        }
    }

    public void destroy() {
        for (var quest : quests.values()) {
            quest.destroy();
        }
    }

    public String getId() {
        return pool.getId();
    }

    public String getName() {
        return pool.getDefinition().getName();
    }

    public PoolDefinition getDefinition() {
        return pool.getDefinition();
    }

    public List<Quest> getActiveQuests() {
        if (isTimedRandom()) {
            return profile.getData().getPoolRollData(getId()).quests()
                    .stream().map(quests::get).filter(Objects::nonNull)
                    .toList();
        }
        return quests.values().stream().filter(Quest::isUnlocked).toList();
    }

    public boolean isRolledQuest(Quest quest) {
        return profile.getData().getPoolRollData(getId()).quests().contains(quest.getId());
    }

    public List<Quest> getNotCompletedQuests() {
        if (!isTimedRandom()) {
            return quests.values().stream().filter(q -> !q.isCompleted()).toList();
        }
        return getActiveQuests().stream().filter(q -> !q.isCompleted()).toList();
    }

    public Quest getQuest(String id) {
        return quests.get(id);
    }

    public Collection<Quest> getQuests() {
        return quests.values();
    }

    public void resetAllQuestProgress() {
        for (var quest : quests.values()) {
            quest.reset();
            if (isGlobal()) {
                quest.start(false);
            } else if (isRolledQuest(quest)) {
                quest.start(false);
            }
        }
    }

    public boolean hasLeveling() {
        return pool.getDefinition().getLeveling().getEnabled();
    }

    public boolean isUnlocked() {
        return pool.getDefinition().getRequirement().canStart(profile, getId())
                || profile.getData().isPoolUnlocked(getId());
    }

    public boolean unlock(boolean force) {
        if (isUnlocked()) return false;

        if (!force && !pool.getDefinition().getRequirement().canStart(profile, getId())) {
            return false;
        }

        profile.getData().unlockPool(getId());

        var msg = AuroraQuests.getInstance().getConfigManager().getMessageConfig(profile.getPlayer()).getPoolUnlocked();
        Chat.sendMessage(profile.getPlayer(), msg, Placeholder.of("{pool}", pool.getDefinition().getName()));

        return true;
    }

    public void startQuests() {
        if (!isUnlocked()) return;

        if (isTimedRandom()) {
            for (var quest : getActiveQuests()) {
                quest.start();
            }
        } else {
            for (var quest : quests.values()) {
                quest.start(false);
            }
        }
    }

    public int getLevel() {
        if (pool.getDefinition().getLeveling().getEnabled()) {
            var completed = profile.getData().getCompletedCount(getId());
            var requirements = pool.getDefinition().getLeveling().getRequirements();

            for (int i = requirements.size() - 1; i >= 0; i--) {
                if (completed >= requirements.get(i)) {
                    return i + 1;
                }
            }

            return 0;
        }
        return 0;
    }

    public List<Placeholder<?>> getLevelPlaceholders(int level) {
        var prevLevel = Math.max(0, level - 1);

        return List.of(
                Placeholder.of("{player}", profile.getPlayer().getName()),
                Placeholder.of("{level_raw}", level),
                Placeholder.of("{level}", AuroraAPI.formatNumber(level)),
                Placeholder.of("{level_roman}", RomanNumber.toRoman(level)),
                Placeholder.of("{prev_level_raw}", prevLevel),
                Placeholder.of("{prev_level}", AuroraAPI.formatNumber(prevLevel)),
                Placeholder.of("{prev_level_roman}", RomanNumber.toRoman(prevLevel)),
                Placeholder.of("{pool}", pool.getDefinition().getName()),
                Placeholder.of("{pool_id}", getId())
        );
    }

    public long getCompletedQuestCount() {
        return profile.getData().getCompletedCount(getId());
    }

    public boolean isGlobal() {
        return !isTimedRandom();
    }

    public boolean isTimedRandom() {
        return pool.getDefinition().getType() == PoolType.TIMED_RANDOM;
    }

    public void reRollQuests(boolean sendNotification) {
        if (isGlobal()) return;
        if (!isUnlocked()) return;
        var definition = pool.getDefinition();
        // difficulty -> quest
        var pickedQuests = new HashMap<String, List<Quest>>();
        var difficulties = definition.getDifficulties();

        var questsToSelectFrom = quests.values().stream()
                .filter(q -> difficulties.containsKey(q.getDefinition().getDifficulty()) && q.canStart())
                .toList();

        AuroraQuests.logger().debug("Picking quests from pool " + definition.getId() + " for player " + profile.getPlayer().getName() + " with " + questsToSelectFrom.size() + " quests");

        var pickableQuests = new HashMap<String, List<Quest>>();

        for (var quest : questsToSelectFrom) {
            if (quest.getDefinition().getDifficulty() == null) continue;
            pickableQuests.computeIfAbsent(quest.getDefinition().getDifficulty(), k -> new ArrayList<>()).add(quest);
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

        var data = profile.getData();
        var questIds = pickedQuests.values().stream().flatMap(List::stream).map(Quest::getId).toList();

        if (data.getPoolRollData(getId()) != null) {
            for (var quest : getActiveQuests()) {
                quest.dispose();
            }
        }

        data.setRolledQuests(getId(), questIds);

        for (var quest : getActiveQuests()) {
            quest.start();
        }

        AuroraQuests.logger().debug("Rolled quests for player " + profile.getPlayer().getName() + " in pool " + definition.getId() + ": " + String.join(", ", questIds));

        if (sendNotification) {
            var msg = AuroraQuests.getInstance().getConfigManager().getMessageConfig(profile.getPlayer()).getReRolledTarget();
            var placholder = Placeholder.of("{pool}", definition.getName());
            Chat.sendMessage(profile.getPlayer(), AuroraQuests.getInstance().getLocalizationProvider().fillVariables(profile.getPlayer(), Placeholder.execute(msg, placholder)), placholder);
        }
    }

    public boolean rollIfNecessary(boolean sendNotification) {
        AuroraQuests.logger().debug("Checking if player " + profile.getPlayer().getName() + " needs to reroll quests for pool " + pool.getDefinition().getId());
        if (!isTimedRandom()) return false;
        if (!pool.getQuestRoller().isValid()) return false;
        if (!isUnlocked()) return false;
        AuroraQuests.logger().debug("Pool is timed random and quest roller is valid");

        try {
            var data = profile.getData();

            var rollData = data.getPoolRollData(getId());
            boolean hasInvalidQuests = rollData != null && rollData.quests().stream().anyMatch(q -> !pool.getDefinition().getQuests().containsKey(q));


            if (rollData == null || pool.getQuestRoller().shouldReroll(rollData.timestamp()) || hasInvalidQuests) {
                reRollQuests(sendNotification);
                return true;
            }
        } catch (Exception e) {
            AuroraQuests.logger().severe("Failed to reroll quests for player " + profile.getPlayer().getName() + " in pool " + pool.getDefinition().getId() + ": " + e.getMessage());
        }

        return false;
    }

    public Duration getDurationUntilNextRoll() {
        return pool.getDurationUntilNextRoll();
    }

    private void reward(int level) {
        var mc = AuroraQuests.getInstance().getConfigManager().getConfig();
        var rewards = pool.getMatcherManager().getBestMatcher(level).computeRewards(level);

        List<Placeholder<?>> placeholders = getLevelPlaceholders(level);
        var player = profile.getPlayer();

        if (mc.getLevelUpMessage().getEnabled()) {
            var lines = mc.getLevelUpMessage().getMessage();
            var text = RewardUtil.fillRewardMessage(player, mc.getDisplayComponents().get("rewards"), lines, placeholders, rewards);
            player.sendMessage(text);
        }

        if (mc.getLevelUpSound().getEnabled()) {
            var sound = mc.getLevelUpSound();
            SoundUtil.playSound(player, sound.getSound(), sound.getVolume(), sound.getPitch());
        }

        RewardExecutor.execute(rewards, player, level, placeholders);
    }

}
