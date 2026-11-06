package gg.auroramc.quests.config;

import gg.auroramc.aurora.api.config.premade.ItemConfig;
import lombok.Getter;

import java.util.Map;

@Getter
public class CommonMenuConfig {
    private Map<String, Config.DisplayComponent> displayComponents;
    private Map<String, ItemConfig> items;
    private ItemConfig completedQuestItem;
    private ItemConfig lockedQuestItem;
    private TaskStatuses taskStatuses;
    private ProgressBar progressBar;

    @Getter
    public static final class ProgressBar {
        private Integer length = 20;
        private String filledCharacter;
        private String unfilledCharacter;
    }

    @Getter
    public static class TaskStatuses {
        private String completed = "";
        private String notCompleted = "";
    }
}
