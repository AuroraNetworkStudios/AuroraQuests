package gg.auroramc.quests.hooks.auroralevels;

import gg.auroramc.aurora.api.util.NamespacedId;
import gg.auroramc.quests.AuroraQuests;
import gg.auroramc.quests.api.factory.ObjectiveFactory;
import gg.auroramc.quests.api.objective.ObjectiveType;
import gg.auroramc.quests.hooks.Hook;
import gg.auroramc.quests.hooks.auroralevels.objective.GainAuroraLevelObjective;
import gg.auroramc.quests.hooks.auroralevels.objective.GainAuroraXpObjective;

public class AuroraLevelsHook implements Hook {
    @Override
    public void hook(AuroraQuests plugin) {
        plugin.getPoolManager().getRewardFactory()
                .registerRewardType(NamespacedId.fromDefault("levels_xp"), AuroraLevelsReward.class);

        ObjectiveFactory.registerObjective(ObjectiveType.GAIN_AURORA_XP, GainAuroraXpObjective.class);
        ObjectiveFactory.registerObjective(ObjectiveType.GAIN_AURORA_LEVEL, GainAuroraLevelObjective.class);

        AuroraQuests.logger().info("Hooked into AuroraLevels for GAIN_AURORA_XP and GAIN_AURORA_LEVEL objective and for levels_xp reward");
    }
}
