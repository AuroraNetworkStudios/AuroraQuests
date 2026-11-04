package gg.auroramc.quests.config;

import com.google.common.collect.Maps;
import gg.auroramc.aurora.api.config.ConfigFile;
import gg.auroramc.aurora.api.plugin.Messages;
import gg.auroramc.quests.AuroraQuests;
import gg.auroramc.quests.api.questpool.PoolConfig;
import gg.auroramc.quests.config.quest.QuestConfig;
import lombok.Getter;
import lombok.SneakyThrows;
import org.bukkit.command.CommandSender;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Map;
import java.util.jar.JarFile;
import java.util.stream.Stream;

public class ConfigManager {
    private final AuroraQuests plugin;
    private ConfigFile<Config> config;
    @Getter
    private Messages<MessageConfig> messages;
    private ConfigFile<MainMenuConfig> mainMenuConfig;
    private ConfigFile<CommonMenuConfig> commonMenuConfig;
    @Getter
    private final Map<String, PoolConfig> questPools = Maps.newConcurrentMap();

    public ConfigManager(AuroraQuests plugin) {
        this.plugin = plugin;
    }

    public void reload() {
        if (config == null) {
            boolean saveDefaultQuests = !new File(plugin.getDataFolder(), "config.yml").exists();
            config = ConfigFile.builder(plugin, "config.yml", Config.class).migrations(Config.migrations()).build();
            commonMenuConfig = ConfigFile.builder(plugin, "menu_common.yml", CommonMenuConfig.class).build();
            mainMenuConfig = ConfigFile.builder(plugin, "menu_main.yml", MainMenuConfig.class).build();
            messages = new Messages<>(plugin, MessageConfig.class, MessageConfig.migrations());
            config.get();
            commonMenuConfig.get();
            mainMenuConfig.get();
            messages.load(getConfig().getLanguage(), getConfig().getPerPlayerLocale());
            if (saveDefaultQuests) {
                saveDefaultQuests();
            }
        } else {
            config.reload();
            commonMenuConfig.reload();
            mainMenuConfig.reload();
            messages.load(getConfig().getLanguage(), getConfig().getPerPlayerLocale());
        }

        reloadQuests();
    }

    public Config getConfig() {
        return config.get();
    }

    public MainMenuConfig getMainMenuConfig() {
        return mainMenuConfig.get();
    }

    public CommonMenuConfig getCommonMenuConfig() {
        return commonMenuConfig.get();
    }

    /**
     * The messages for whoever is told something: their own language if per-player languages are on.
     */
    public MessageConfig getMessageConfig(CommandSender sender) {
        return messages.get(sender);
    }

    @SneakyThrows
    public void saveDefaultQuests() {
        Path jarPath = Path.of(plugin.getClass().getProtectionDomain().getCodeSource().getLocation().toURI());
        Path dataFolder = plugin.getDataFolder().toPath();

        try (JarFile jar = new JarFile(jarPath.toFile())) {
            jar.stream()
                    .filter(entry -> entry.getName().startsWith("quest_pools/") && entry.getName().endsWith(".yml"))

                    .forEach(entry -> {
                        Path outFile = dataFolder.resolve(entry.getName());
                        Path parentDir = outFile.getParent();
                        if (parentDir != null && !Files.exists(parentDir)) {
                            try {
                                Files.createDirectories(parentDir);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                        try (InputStream is = jar.getInputStream(entry)) {
                            Files.copy(is, outFile, StandardCopyOption.REPLACE_EXISTING);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    });
        }
    }

    @SneakyThrows
    public void reloadQuests() {
        questPools.clear();
        Path questsDir = plugin.getDataFolder().toPath().resolve("quest_pools");

        if (!Files.exists(questsDir)) {
            return;
        }

        try (var directories = Files.walk(questsDir, 1)) {
            directories.filter(Files::isDirectory).filter(p -> !p.equals(questsDir)).forEach(dir -> {
                var poolFile = new File(dir.toFile(), "config.yml");
                if (!poolFile.exists()) {
                    return;
                }
                var poolConfig = PoolConfig.load(poolFile);
                poolConfig.setId(dir.toFile().getName());

                var qDir = Paths.get(dir.toFile().getAbsolutePath(), "quests");
                if (!Files.exists(qDir)) {
                    return;
                }

                try (Stream<Path> paths = Files.walk(qDir, 5)) {
                    paths.filter(Files::isRegularFile)
                            .filter(path -> path.toString().endsWith(".yml"))
                            .forEach(path -> {
                                var relativePath = qDir.relativize(path).toString();
                                var questId = relativePath.replace(File.separator, "/").replace(".yml", "");
                                QuestConfig questConfig = QuestConfig.load(path.toFile());
                                questConfig.setPoolConfig(poolConfig);
                                questConfig.setId(questId);
                                if (poolConfig.getQuests().containsKey(questConfig.getId())) {
                                    AuroraQuests.logger().severe("Duplicate quest id: " + questId + " in pool: " + poolConfig.getId() + " skipping... File names most be unique inside the pools quests folder.");
                                } else {
                                    poolConfig.getQuests().put(questConfig.getId(), questConfig);
                                    AuroraQuests.logger().debug("Loaded quest: " + questId + " from pool: " + poolConfig.getId());
                                }
                            });
                } catch (IOException e) {
                    AuroraQuests.logger().severe("Failed to load quests for pool: " + poolConfig.getId() + " error: " + e.getMessage());
                }

                questPools.put(dir.toFile().getName(), poolConfig);
            });
        } catch (IOException e) {
            AuroraQuests.logger().severe("Failed to load quests: " + e.getMessage());
        }
    }
}
