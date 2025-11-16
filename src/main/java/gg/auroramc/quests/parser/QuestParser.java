package gg.auroramc.quests.parser;

import gg.auroramc.aurora.api.reward.Reward;
import gg.auroramc.aurora.api.reward.RewardFactory;
import gg.auroramc.quests.api.quest.QuestDefinition;
import gg.auroramc.quests.api.quest.QuestRequirement;
import gg.auroramc.quests.api.objective.ObjectiveDefinition;
import gg.auroramc.quests.config.quest.QuestConfig;
import gg.auroramc.quests.config.quest.StartRequirementConfig;
import gg.auroramc.quests.config.quest.TaskConfig;
import org.bukkit.configuration.ConfigurationSection;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class QuestParser {
    public static QuestDefinition parse(QuestConfig config, RewardFactory rewardFactory) {
        return QuestDefinition.builder()
                .id(config.getId())
                .name(config.getName())
                .difficulty(config.getDifficulty())
                .requirements(parseRequirement(config.getStartRequirements()))
                .rewards(parseRewards(config.getRewards(), rewardFactory))
                .tasks(parseTasks(config.getTasks()))
                .menuItem(config.getMenuItem())
                .completedLore(config.getCompletedLore())
                .lockedLore(config.getLockedLore())
                .uncompletedLore(config.getUncompletedLore())
                .questCompleteMessage(config.getQuestCompleteMessage())
                .questCompleteSound(config.getQuestCompleteSound())
                .milestones(parseMilestones(config.getMilestones(), rewardFactory))
                .build();
    }

    public static QuestRequirement parseRequirement(StartRequirementConfig config) {
        if (config == null) {
            return new QuestRequirement(false, false, null, null);
        }
        return new QuestRequirement(config.isAlwaysShowInMenu(), config.isNeedsManualUnlock(), config.getQuests(), config.getPermissions());
    }

    public static LinkedHashMap<String, Reward> parseRewards(ConfigurationSection config, RewardFactory factory) {
        if (config == null) return new LinkedHashMap<>(); //allow zero quest rewards

        LinkedHashMap<String, Reward> rewards = new LinkedHashMap<>();

        for (String key : config.getKeys(false)) {
            var reward = factory.createReward(config.getConfigurationSection(key));
            reward.ifPresent(value -> rewards.put(key, value));
        }

        return rewards;
    }

    private static LinkedHashMap<String, ObjectiveDefinition> parseTasks(Map<String, TaskConfig> map) {
        LinkedHashMap<String, ObjectiveDefinition> tasks = new LinkedHashMap<>();

        for (String key : map.keySet()) {
            tasks.put(key, ObjectiveParser.parse(key, map.get(key)));
        }

        return tasks;
    }

    public static LinkedHashMap<Integer, List<Reward>> parseMilestones(Map<Integer, ConfigurationSection> milestonesConfig, RewardFactory factory) {
        if (milestonesConfig == null) return new LinkedHashMap<>();

        LinkedHashMap<Integer, List<Reward>> milestones = new LinkedHashMap<>();

        for (Map.Entry<Integer, ConfigurationSection> entry : milestonesConfig.entrySet()) {
            List<Reward> rewardList = new java.util.ArrayList<>();
            ConfigurationSection rewardsSection = entry.getValue();

            if (rewardsSection != null) {
                for (String key : rewardsSection.getKeys(false)) {
                    var reward = factory.createReward(rewardsSection.getConfigurationSection(key));
                    reward.ifPresent(rewardList::add);
                }
            }

            milestones.put(entry.getKey(), rewardList);
        }

        return milestones;
    }
}
