package gg.auroramc.quests.hooks.mmolib;

import gg.auroramc.aurora.api.plugin.Hook;
import gg.auroramc.aurora.api.reward.integration.MMOStatReward;
import gg.auroramc.aurora.api.util.NamespacedId;
import gg.auroramc.quests.AuroraQuests;

public class MMOLibHook implements Hook<AuroraQuests> {
    // Quests' stat modifiers in MythicLib, kept apart from other plugins'
    public static final String KEY = "aurora_quests";
    @Override
    public void hook(AuroraQuests plugin) {
        plugin.getPoolManager().getRewardFactory()
                .registerRewardType(NamespacedId.fromDefault("mmo_stat"), () -> new MMOStatReward(KEY, plugin, AuroraQuests.logger()));

        plugin.getPoolManager().getRewardAutoCorrector()
                .registerCorrector(NamespacedId.fromDefault("mmo_stat"), new MMOStatCorrector(plugin));

        AuroraQuests.logger().info("Hooked into MythicLib (MMOLib - MMOCore, MMOItems) for stat rewards.");
    }
}
