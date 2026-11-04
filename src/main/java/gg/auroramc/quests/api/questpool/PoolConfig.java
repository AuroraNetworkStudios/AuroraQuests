package gg.auroramc.quests.api.questpool;

import gg.auroramc.aurora.api.config.ConfigLoader;
import gg.auroramc.aurora.api.config.decorators.IgnoreField;
import gg.auroramc.aurora.api.config.premade.ConcreteMatcherConfig;
import gg.auroramc.aurora.api.config.premade.IntervalMatcherConfig;
import gg.auroramc.aurora.api.config.premade.ItemConfig;
import gg.auroramc.quests.config.MainMenuConfig;
import gg.auroramc.quests.config.quest.QuestConfig;
import gg.auroramc.quests.config.quest.StartRequirementConfig;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
public class PoolConfig {
    private String type;
    private String name;
    private Map<String, Integer> difficulties;
    private String resetFrequency;
    private Boolean rerollOnCompletion = false;
    private CompletedQuests completedQuests = new CompletedQuests();
    private PoolMenuItem menuItem;
    private PoolMenu menu;
    private Leveling leveling;
    private StartRequirementConfig unlockRequirements;

    @Setter
    @IgnoreField
    private Map<String, QuestConfig> quests = new HashMap<>();

    @Setter
    @IgnoreField
    private String id;

    @Getter
    public static class PoolMenuItem {
        private Boolean showInMainMenu;
        private Integer page;
        private ItemConfig item;
        private List<String> lockedLore;
    }

    @Getter
    public static class PoolMenu {
        private String title;
        private Integer rows = 6;
        private Map<String, ItemConfig> items;
        private Map<String, ItemConfig> customItems;
        private MainMenuConfig.FillerConfig filler;
        private List<Integer> displayArea;
        private Boolean hasBackButton = true;
        private Boolean hasCloseButton = true;
    }

    @Getter
    public static class CompletedQuests {
        private Boolean display = true;
        private Boolean sortLast = false;
    }

    @Getter
    public static class Leveling {
        private Boolean enabled = false;
        private LevelingMenu menu;
        private List<Integer> requirements;
        private Map<String, IntervalMatcherConfig> levelMatchers;
        private Map<String, ConcreteMatcherConfig> customLevels;
    }

    @Getter
    public static class LevelingMenu {
        private String title;
        private Integer rows = 6;
        private Map<String, ItemConfig> items;
        private Map<String, ItemConfig> customItems;
        private MainMenuConfig.FillerConfig filler;
        private List<Integer> displayArea;
        private Boolean hasBackButton = true;
        private Boolean hasCloseButton = true;
        private Boolean allowItemAmounts = false;
    }

    /**
     * Reads a file. These are read again on every reload, a server's own ones have no bundled copy, so nothing is used
     * as defaults under them.
     */
    public static PoolConfig load(File file) {
        return ConfigLoader.load(PoolConfig.class, file.getPath(), YamlConfiguration.loadConfiguration(file));
    }
}
