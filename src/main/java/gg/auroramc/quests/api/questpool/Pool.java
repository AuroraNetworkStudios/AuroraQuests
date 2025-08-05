package gg.auroramc.quests.api.questpool;

import gg.auroramc.aurora.api.levels.MatcherManager;
import gg.auroramc.aurora.api.reward.RewardFactory;
import gg.auroramc.quests.AuroraQuests;
import lombok.Getter;

import java.time.Duration;

public class Pool {
    @Getter
    private final PoolDefinition definition;
    @Getter
    private MatcherManager matcherManager;
    @Getter
    private QuestRollerScheduler questRoller;

    public Pool(PoolDefinition definition, RewardFactory rewardFactory) {
        this.definition = definition;

        if (definition.getLeveling().getEnabled()) {
            matcherManager = new MatcherManager(rewardFactory);
            matcherManager.reload(definition.getLeveling().getLevelMatchers(), definition.getLeveling().getCustomLevels());
        }

        AuroraQuests.logger().debug("Loaded difficulties for pool " + definition.getId() + ": " + String.join(", ", definition.getDifficulties().keySet()));
    }

    public void start() {
        if (definition.getType() == PoolType.TIMED_RANDOM) {
            if (definition.isResetFrequencyValid()) {
                questRoller = new QuestRollerScheduler(definition);
            } else {
                AuroraQuests.logger().warning("Invalid reset frequency: " + definition.getResetFrequency() + " for pool " + definition.getId());
            }
        }
    }


    public String getId() {
        return definition.getId();
    }


    public Duration getDurationUntilNextRoll() {
        if (definition.getType() != PoolType.TIMED_RANDOM) return Duration.ZERO;
        return questRoller.getDurationUntilNextRoll();
    }

    public void dispose() {
        if (questRoller != null) {
            questRoller.shutdown();
        }
    }
}
