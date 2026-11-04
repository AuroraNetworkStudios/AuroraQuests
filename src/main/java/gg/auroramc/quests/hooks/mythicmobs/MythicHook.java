package gg.auroramc.quests.hooks.mythicmobs;

import gg.auroramc.aurora.api.plugin.Hook;
import gg.auroramc.aurora.api.reward.integration.MythicStatReward;
import gg.auroramc.aurora.api.util.NamespacedId;
import gg.auroramc.quests.AuroraQuests;
import gg.auroramc.quests.hooks.mythicmobs.reward.AuroraQuestsStatSource;
import gg.auroramc.quests.hooks.mythicmobs.reward.MythicStatCorrector;
import io.lumine.mythic.core.skills.stats.StatSource;
import org.bukkit.Bukkit;

public class MythicHook implements Hook<AuroraQuests> {
    // Quests' stats in MythicMobs, kept apart from other plugins'
    public static final StatSource SOURCE = new AuroraQuestsStatSource();
    @Override
    public void hook(AuroraQuests plugin) {
        Bukkit.getPluginManager().registerEvents(new MythicMobListener(), plugin);

        plugin.getPoolManager().getRewardFactory()
                .registerRewardType(NamespacedId.fromDefault("mythic_stat"), () -> new MythicStatReward(SOURCE, AuroraQuests.logger()));

        plugin.getPoolManager().getRewardAutoCorrector()
                .registerCorrector(NamespacedId.fromDefault("mythic_stat"), new MythicStatCorrector());

        AuroraQuests.logger().info("Hooked into MythicMobs for KILL_MOB, ENTITY_LOOT and KILL_LEVELLED_MOB objectives.");
    }
}
