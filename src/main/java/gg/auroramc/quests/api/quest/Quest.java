package gg.auroramc.quests.api.quest;

import gg.auroramc.aurora.api.AuroraAPI;
import gg.auroramc.aurora.api.command.CommandDispatcher;
import gg.auroramc.aurora.api.message.Placeholder;
import gg.auroramc.aurora.api.reward.RewardExecutor;
import gg.auroramc.quests.AuroraQuests;
import gg.auroramc.quests.api.event.EventBus;
import gg.auroramc.quests.api.event.EventType;
import gg.auroramc.quests.api.event.QuestCompletedEvent;
import gg.auroramc.quests.api.factory.ObjectiveFactory;
import gg.auroramc.quests.api.profile.Profile;
import gg.auroramc.quests.api.questpool.QuestPool;
import gg.auroramc.quests.api.objective.Objective;
import gg.auroramc.quests.util.RewardUtil;
import gg.auroramc.quests.util.SoundUtil;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Getter
public class Quest extends EventBus {
    private final QuestDefinition definition;
    private final Profile.QuestDataWrapper data;
    private final List<Objective> objectives;
    private final QuestPool pool;
    private boolean started = false;

    public Quest(QuestPool pool, QuestDefinition definition, Profile.QuestDataWrapper data) {
        this.pool = pool;
        this.data = data;
        this.definition = definition;
        this.objectives = definition.getTasks().values().stream()
                .map(d -> ObjectiveFactory.createObjective(this, d))
                .filter(Objects::nonNull)
                .toList();

        for (var obj : objectives) {
            obj.subscribe(EventType.TASK_PROGRESS, objective -> {
                this.publish(EventType.TASK_PROGRESS, objective);
                if (!objective.getDefinition().getOnProgress().isEmpty()) {
                    List<Placeholder<?>> pl = List.of(
                            Placeholder.of("{player}", data.profile().getPlayer().getName()),
                            Placeholder.of("{progress_raw}", objective.getProgress()),
                            Placeholder.of("{progress}", AuroraAPI.formatNumber(objective.getProgress())),
                            Placeholder.of("{target_raw}", objective.getTarget()),
                            Placeholder.of("{target}", AuroraAPI.formatNumber(objective.getTarget())),
                            Placeholder.of("{percent}", AuroraAPI.formatNumber(objective.getProgress() / objective.getTarget() * 100))
                    );
                    for (var command : objective.getDefinition().getOnProgress()) {
                        CommandDispatcher.dispatch(data.profile().getPlayer(), command, pl);
                    }
                }
            });

            obj.subscribe(EventType.TASK_COMPLETED, objective -> {
                this.publish(EventType.TASK_COMPLETED, objective);

                if (!objective.getDefinition().getOnComplete().isEmpty()) {
                    var pl = Placeholder.of("{player}", data.profile().getPlayer().getName());
                    for (var command : objective.getDefinition().getOnComplete()) {
                        CommandDispatcher.dispatch(data.profile().getPlayer(), command, pl);
                    }
                }

                var completed = true;

                for (var obj2 : objectives) {
                    completed = completed && obj2.isCompleted();
                }

                if (completed) this.handleCompletion(objective);
            });
        }
    }

    private void handleCompletion(@Nullable Objective trigger) {
        data.complete();

        Bukkit.getPluginManager().callEvent(new QuestCompletedEvent(data.profile().getPlayer(), pool, this));

        reward();
        dispose();

        this.publish(EventType.QUEST_COMPLETED, trigger);
    }

    public boolean isUnlocked() {
        return !definition.getRequirements().hasRequirements() || data.isUnlocked();
    }

    public boolean canStart() {
        return definition.getRequirements().canStart(data);
    }

    public boolean start() {
        return start(false);
    }

    public boolean start(boolean force) {
        if (started) return false;

        // For timed random quests, once they are rolled, they should always start
        if (pool.isGlobal()) {
            if (!force && !definition.getRequirements().canStart(data)) {
                return false;
            }
        }

        for (var obj : objectives) {
            obj.start();
        }

        if (pool.isGlobal()) {
            data.unlock();
        }

        started = true;

        return true;
    }

    public void unlock() {
        data.unlock();
    }

    public String getId() {
        return definition.getId();
    }

    public void reset() {
        for (var obj : objectives) {
            obj.resetProgress();
            if (started) obj.start();
        }
        data.reset();
    }

    public boolean isCompleted() {
        return data.isCompleted();
    }

    public void complete() {
        for (var obj : objectives) {
            obj.complete(true);
        }
        handleCompletion(null);
    }

    public void dispose() {
        for (var obj : objectives) {
            obj.dispose();
        }

        started = false;
    }

    public void destroy() {
        super.dispose();

        for (var obj : objectives) {
            obj.destroy();
        }

        started = false;
    }

    public List<Placeholder<?>> getPlaceholders() {
        var gc = AuroraQuests.getInstance().getConfigManager().getConfig();
        List<Placeholder<?>> placeholders = new ArrayList<>(9 + objectives.size() + definition.getRewards().size());

        placeholders.add(Placeholder.of("{name}", definition.getName()));
        placeholders.add(Placeholder.of("{difficulty}", gc.getDifficulties().get(definition.getDifficulty())));
        placeholders.add(Placeholder.of("{difficulty_id}", definition.getDifficulty()));
        placeholders.add(Placeholder.of("{quest_id}", definition.getId()));
        placeholders.add(Placeholder.of("{quest}", definition.getName()));
        placeholders.add(Placeholder.of("{pool_id}", pool.getId()));
        placeholders.add(Placeholder.of("{pool}", pool.getName()));
        placeholders.add(Placeholder.of("{player}", data.profile().getPlayer().getName()));
        placeholders.add(Placeholder.of("{pool_level}", pool.getLevel()));

        for (var objective : objectives) {
            placeholders.add(Placeholder.of("{task_" + objective.getId() + "}", objective.display()));
        }

        for (var reward : definition.getRewards().entrySet()) {
            placeholders.add(Placeholder.of("{reward_" + reward.getKey() + "}", reward.getValue().getDisplay(data.profile().getPlayer(), placeholders)));
        }

        return placeholders;
    }

    private void reward() {
        var gConfig = AuroraQuests.getInstance().getConfigManager().getConfig();

        List<Placeholder<?>> placeholders = getPlaceholders();
        var player = data.profile().getPlayer();
        var rewards = definition.getRewards();

        //check if this quest has its own complete message
        if (definition.getQuestCompleteMessage() != null) {
            //separate check - we do NOT want to show the global quest complete message if the quest overrides the enable state
            if (definition.getQuestCompleteMessage().getEnabled()) {
                var lines = definition.getQuestCompleteMessage().getMessage();
                var text = RewardUtil.fillRewardMessage(player, gConfig.getDisplayComponents().get("rewards"), lines, placeholders, rewards.values());
                var delay = definition.getQuestCompleteMessage().getDelay();
                if (delay > 0) {
                    player.getScheduler().runDelayed(AuroraQuests.getInstance(), (task) -> player.sendMessage(text), null, delay);
                } else {
                    player.sendMessage(text);
                }
            }
        } else if (gConfig.getQuestCompleteMessage().getEnabled()) {
            var lines = gConfig.getQuestCompleteMessage().getMessage();
            var text = RewardUtil.fillRewardMessage(player, gConfig.getDisplayComponents().get("rewards"), lines, placeholders, rewards.values());
            player.sendMessage(text);
        }

        //same check, now for the quest complete sound
        if (definition.getQuestCompleteSound() != null) {
            //separate check - we do NOT want to play the global quest complete sound if the quest overrides the enable state
            if (definition.getQuestCompleteSound().getEnabled()) {
                var sound = definition.getQuestCompleteSound();
                var delay = definition.getQuestCompleteSound().getDelay();
                if (delay > 0) {
                    player.getScheduler().runDelayed(AuroraQuests.getInstance(), (task) ->
                            SoundUtil.playSound(player, sound.getSound(), sound.getVolume(), sound.getPitch()), null, delay);
                } else {
                    SoundUtil.playSound(player, sound.getSound(), sound.getVolume(), sound.getPitch());
                }
            }
        } else if (gConfig.getQuestCompleteSound().getEnabled()) {
            var sound = gConfig.getQuestCompleteSound();
            SoundUtil.playSound(player, sound.getSound(), sound.getVolume(), sound.getPitch());
        }

        RewardExecutor.execute(rewards.values().stream().toList(), player, 1, placeholders);
    }
}
