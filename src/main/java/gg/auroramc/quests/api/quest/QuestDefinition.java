package gg.auroramc.quests.api.quest;

import gg.auroramc.aurora.api.config.premade.ItemConfig;
import gg.auroramc.aurora.api.reward.Reward;
import gg.auroramc.quests.api.objective.ObjectiveDefinition;
import gg.auroramc.quests.config.quest.QuestConfig;
import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.LinkedHashMap;

@Builder
@Getter
public class QuestDefinition {
    private final String id;
    private final String name;
    private final String difficulty;
    private final ItemConfig menuItem;
    private final List<String> lockedLore;
    private final List<String> completedLore;
    private final List<String> uncompletedLore;
    private final LinkedHashMap<String, ObjectiveDefinition> tasks;
    private final LinkedHashMap<String, Reward> rewards;
    private final QuestRequirement requirements;
    private final QuestConfig.LevelUpMessage questCompleteMessage;
    private final QuestConfig.LevelUpSound questCompleteSound;
}
