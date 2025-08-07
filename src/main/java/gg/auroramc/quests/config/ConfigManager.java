package gg.auroramc.quests.config;

import com.google.common.collect.Maps;
import gg.auroramc.aurora.Aurora;
import gg.auroramc.aurora.api.AuroraAPI;
import gg.auroramc.aurora.api.localization.LocalizationProvider;
import gg.auroramc.quests.AuroraQuests;
import gg.auroramc.quests.api.questpool.PoolConfig;
import gg.auroramc.quests.config.quest.QuestConfig;
import lombok.Getter;
import lombok.SneakyThrows;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.jar.JarFile;
import java.util.stream.Stream;

@Getter
public class ConfigManager {
    private final AuroraQuests plugin;
    private Config config;
    private final Map<Locale, MessageConfig> messageConfigs = new HashMap<>();
    private MainMenuConfig mainMenuConfig;
    private CommonMenuConfig commonMenuConfig;
    private final Map<String, PoolConfig> questPools = Maps.newConcurrentMap();
    private Locale defaultLocale;

    public ConfigManager(AuroraQuests plugin) {
        this.plugin = plugin;
    }

    public void reload() {
        boolean saveDefaultQuests = !Config.getFile(plugin).exists();

        Config.saveDefault(plugin);
        config = new Config(plugin);
        config.load();

        defaultLocale = Locale.forLanguageTag(config.getLanguage());


        for (var locale : Aurora.getLanguageProvider().getSupportedLocales()) {
            MessageConfig.saveDefault(plugin, locale.getLanguage());
            var messageConfig = new MessageConfig(plugin, locale.getLanguage());
            messageConfig.load();
            messageConfigs.put(locale, messageConfig);
        }

        CommonMenuConfig.saveDefault(plugin);
        commonMenuConfig = new CommonMenuConfig(plugin);
        commonMenuConfig.load();

        MainMenuConfig.saveDefault(plugin);
        mainMenuConfig = new MainMenuConfig(plugin);
        mainMenuConfig.load();

        if (saveDefaultQuests) {
            saveDefaultQuests();
        }

        reloadQuests();
    }

    public MessageConfig getMessageConfig(CommandSender sender) {
        if (config.getPerPlayerLocale()) {
            if (sender instanceof Player player) {
                var locale = Aurora.getLanguageProvider().getPlayerLocale(player);
                return messageConfigs.get(locale);
            } else {
                return messageConfigs.get(Aurora.getLanguageProvider().getFallbackLocale());
            }
        } else {
            return messageConfigs.get(defaultLocale);
        }
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
                var poolConfig = new PoolConfig(poolFile);
                poolConfig.load();
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
                                QuestConfig questConfig = new QuestConfig(path.toFile());
                                questConfig.load();
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
