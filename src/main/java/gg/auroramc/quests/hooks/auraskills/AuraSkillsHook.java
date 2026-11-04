package gg.auroramc.quests.hooks.auraskills;

import gg.auroramc.aurora.api.plugin.Hook;
import gg.auroramc.aurora.api.reward.integration.AuraSkillsStatReward;
import gg.auroramc.aurora.api.reward.integration.AuraSkillsXpReward;
import gg.auroramc.aurora.api.util.NamespacedId;
import gg.auroramc.quests.AuroraQuests;
import gg.auroramc.quests.api.factory.ObjectiveFactory;
import gg.auroramc.quests.api.objective.ObjectiveType;
import gg.auroramc.quests.hooks.auraskills.objective.GainAuraSkillsXpObjective;
import lombok.Getter;
import org.bukkit.Bukkit;

@Getter
public class AuraSkillsHook implements Hook<AuroraQuests> {
    // Quests' stat modifiers in AuraSkills start with this, kept apart from other plugins'
    public static final String PREFIX = "aurora_quests_";
    private AuraSkillsCorrector corrector;

    @Override
    public void hook(AuroraQuests plugin) {
        this.corrector = new AuraSkillsCorrector();

        Bukkit.getPluginManager().registerEvents(new AuraSkillsListener(this), plugin);

        plugin.getPoolManager().getRewardFactory()
                .registerRewardType(NamespacedId.fromDefault("auraskills_stat"), () -> new AuraSkillsStatReward(PREFIX, AuroraQuests.logger()));

        plugin.getPoolManager().getRewardFactory()
                .registerRewardType(NamespacedId.fromDefault("auraskills_xp"), () -> new AuraSkillsXpReward(AuroraQuests.logger()));

        plugin.getPoolManager().getRewardAutoCorrector()
                .registerCorrector(NamespacedId.fromDefault("auraskills_stat"), this.corrector);

        ObjectiveFactory.registerObjective(ObjectiveType.GAIN_AURASKILLS_XP, GainAuraSkillsXpObjective.class);

        AuroraQuests.logger().info("Hooked into AuraSkills for GAIN_AURASKILLS_XP objective and for auraskills_stat/auraskills_xp rewards");
    }
}
