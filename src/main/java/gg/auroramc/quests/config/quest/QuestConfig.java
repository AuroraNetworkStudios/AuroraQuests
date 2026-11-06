package gg.auroramc.quests.config.quest;

import gg.auroramc.aurora.api.config.ConfigLoader;
import gg.auroramc.aurora.api.config.decorators.IgnoreField;
import gg.auroramc.aurora.api.config.premade.ItemConfig;
import gg.auroramc.quests.api.questpool.PoolConfig;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.List;
import java.util.Map;

@Getter
public class QuestConfig {
    private String name;
    private String difficulty;
    private ItemConfig menuItem;
    private ItemConfig completedMenuItem;
    private ItemConfig lockedMenuItem;
    private List<String> lockedLore;
    private List<String> completedLore;
    private List<String> uncompletedLore;
    private Map<String, TaskConfig> tasks;
    private ConfigurationSection rewards;
    private StartRequirementConfig startRequirements;
    private QuestConfig.LevelUpSound questCompleteSound;
    private QuestConfig.LevelUpMessage questCompleteMessage;

    @Setter
    @IgnoreField
    private String id;

    @Setter
    @IgnoreField
    private PoolConfig poolConfig;

    @Getter
    public static final class LevelUpMessage {
        private Boolean enabled;
        private List<String> message;
        private int delay;
    }

    @Getter
    public static final class LevelUpSound {
        private Boolean enabled;
        private String sound;
        private Float volume;
        private Float pitch;
        private int delay;
    }

    /**
     * Reads a file. These are read again on every reload, a server's own ones have no bundled copy, so nothing is used
     * as defaults under them.
     */
    public static QuestConfig load(File file) {
        return ConfigLoader.load(QuestConfig.class, file.getPath(), YamlConfiguration.loadConfiguration(file));
    }
}
