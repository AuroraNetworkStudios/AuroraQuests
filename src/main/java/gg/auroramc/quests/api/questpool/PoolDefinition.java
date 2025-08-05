package gg.auroramc.quests.api.questpool;

import gg.auroramc.quests.api.quest.QuestDefinition;
import gg.auroramc.quests.api.quest.QuestRequirement;
import lombok.Builder;
import lombok.Getter;

import java.util.LinkedHashMap;
import java.util.Map;

@Getter
@Builder
public class PoolDefinition {
    private final String id;
    private final PoolType type;
    private final String name;
    private final Map<String, Integer> difficulties;
    private final String resetFrequency;
    private final PoolConfig.PoolMenuItem menuItem;
    private final PoolConfig.PoolMenu menu;
    private final PoolConfig.Leveling leveling;
    private final QuestRequirement requirement;

    private final LinkedHashMap<String, QuestDefinition> quests;

    public boolean isResetFrequencyValid() {
        return resetFrequency != null && resetFrequency.split("\\s").length == 6;
    }
}
