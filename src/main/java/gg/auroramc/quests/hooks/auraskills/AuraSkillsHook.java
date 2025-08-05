package gg.auroramc.quests.hooks.auraskills;

import gg.auroramc.aurora.api.util.NamespacedId;
import gg.auroramc.quests.AuroraQuests;
import gg.auroramc.quests.api.factory.ObjectiveFactory;
import gg.auroramc.quests.api.objective.ObjectiveType;
import gg.auroramc.quests.hooks.Hook;
import gg.auroramc.quests.hooks.auraskills.objective.GainAuraSkillsXpObjective;
import lombok.Getter;
import org.bukkit.Bukkit;

@Getter
public class AuraSkillsHook implements Hook {
    private AuraSkillsCorrector corrector;

    @Override
    public void hook(AuroraQuests plugin) {
        this.corrector = new AuraSkillsCorrector();

        Bukkit.getPluginManager().registerEvents(new AuraSkillsListener(this), plugin);

        plugin.getPoolManager().getRewardFactory()
                .registerRewardType(NamespacedId.fromDefault("auraskills_stat"), AuraSkillsStatReward.class);

        plugin.getPoolManager().getRewardFactory()
                .registerRewardType(NamespacedId.fromDefault("auraskills_xp"), AuraSkillsXpReward.class);

        plugin.getPoolManager().getRewardAutoCorrector()
                .registerCorrector(NamespacedId.fromDefault("auraskills_stat"), this.corrector);

        ObjectiveFactory.registerObjective(ObjectiveType.GAIN_AURASKILLS_XP, GainAuraSkillsXpObjective.class);

        AuroraQuests.logger().info("Hooked into AuraSkills for GAIN_AURASKILLS_XP objective and for auraskills_stat/auraskills_xp rewards");
    }
}
