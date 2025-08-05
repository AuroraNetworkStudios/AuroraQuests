package gg.auroramc.quests.hooks.mythicmobs;

import gg.auroramc.aurora.api.util.NamespacedId;
import gg.auroramc.quests.AuroraQuests;
import gg.auroramc.quests.hooks.Hook;
import gg.auroramc.quests.hooks.mythicmobs.reward.MythicStatCorrector;
import gg.auroramc.quests.hooks.mythicmobs.reward.MythicStatReward;
import org.bukkit.Bukkit;

public class MythicHook implements Hook {
    @Override
    public void hook(AuroraQuests plugin) {
        Bukkit.getPluginManager().registerEvents(new MythicMobListener(), plugin);

        plugin.getPoolManager().getRewardFactory()
                .registerRewardType(NamespacedId.fromDefault("mythic_stat"), MythicStatReward.class);

        plugin.getPoolManager().getRewardAutoCorrector()
                .registerCorrector(NamespacedId.fromDefault("mythic_stat"), new MythicStatCorrector());

        AuroraQuests.logger().info("Hooked into MythicMobs for KILL_MOB, ENTITY_LOOT and KILL_LEVELLED_MOB objectives.");
    }
}
