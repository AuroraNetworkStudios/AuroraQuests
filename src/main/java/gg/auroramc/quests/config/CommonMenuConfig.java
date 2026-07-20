package gg.auroramc.quests.config;

import gg.auroramc.aurora.api.config.AuroraConfig;
import gg.auroramc.aurora.api.config.premade.ItemConfig;
import gg.auroramc.quests.AuroraQuests;
import lombok.Getter;

import java.io.File;
import java.util.Map;

@Getter
public class CommonMenuConfig extends AuroraConfig {
    private Map<String, Config.DisplayComponent> displayComponents;
    private Map<String, ItemConfig> items;
    private ItemConfig completedQuestItem;
    private TaskStatuses taskStatuses;
    private ProgressBar progressBar;

    @Getter
    public static final class ProgressBar {
        private Integer length = 20;
        private String filledCharacter;
        private String unfilledCharacter;
    }

    public CommonMenuConfig(AuroraQuests plugin) {
        super(getFile(plugin));
    }


    @Getter
    public static class TaskStatuses {
        private String completed = "";
        private String notCompleted = "";
    }

    public static File getFile(AuroraQuests plugin) {
        return new File(plugin.getDataFolder(), "menu_common.yml");
    }

    public static void saveDefault(AuroraQuests plugin) {
        if (!getFile(plugin).exists()) {
            plugin.saveResource("menu_common.yml", false);
        }
    }
}
