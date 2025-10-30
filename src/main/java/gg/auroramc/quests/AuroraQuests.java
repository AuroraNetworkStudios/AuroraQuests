package gg.auroramc.quests;

import gg.auroramc.aurora.Aurora;
import gg.auroramc.aurora.api.AuroraAPI;
import gg.auroramc.aurora.api.AuroraLogger;
import gg.auroramc.aurora.api.command.CommandDispatcher;
import gg.auroramc.aurora.api.events.user.AuroraUserLoadedEvent;
import gg.auroramc.aurora.api.localization.LocalizationProvider;
import gg.auroramc.aurora.api.user.AuroraUser;
import gg.auroramc.quests.api.AuroraQuestsPlugin;
import gg.auroramc.quests.api.data.QuestData;
import gg.auroramc.quests.api.event.BukkitEventBus;
import gg.auroramc.quests.api.event.QuestCompletedEvent;
import gg.auroramc.quests.api.factory.ObjectiveFactory;
import gg.auroramc.quests.api.objective.ObjectiveType;
import gg.auroramc.quests.api.profile.ProfileManager;
import gg.auroramc.quests.api.questpool.Pool;
import gg.auroramc.quests.api.questpool.PoolManager;
import gg.auroramc.quests.command.CommandManager;
import gg.auroramc.quests.config.ConfigManager;
import gg.auroramc.quests.hooks.HookManager;
import gg.auroramc.quests.menu.PoolMenu;
import gg.auroramc.quests.objective.*;
import gg.auroramc.quests.parser.PoolParser;
import gg.auroramc.quests.placeholder.QuestPlaceholderHandler;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import lombok.Getter;
import org.bstats.bukkit.Metrics;
import org.bstats.charts.AdvancedPie;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.quartz.SchedulerException;
import org.quartz.impl.StdSchedulerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Getter
public class AuroraQuests extends AuroraQuestsPlugin implements Listener {
    private boolean loaded = false;

    @Getter
    private static AuroraQuests instance;
    private static AuroraLogger l;

    public static AuroraLogger logger() {
        return l;
    }

    private ConfigManager configManager;
    @Getter
    private LocalizationProvider localizationProvider;
    private CommandManager commandManager;
    private ScheduledTask unlockTask;

    private BukkitEventBus bukkitEventBus;

    @Override
    public void onLoad() {
        instance = this;
        AuroraQuestsPlugin.instance = this;
        configManager = new ConfigManager(this);
        profileManager = new ProfileManager();
        poolManager = new PoolManager();
        bukkitEventBus = new BukkitEventBus();

        registerObjectives();

        l = AuroraAPI.createLogger("AuroraQuests", () -> configManager.getConfig().getDebug());

        configManager.reload();

        for (var pool : configManager.getQuestPools().values()) {
            if (pool.getType().equals("global") && !configManager.getConfig().getLeaderboards().getIncludeGlobal()) {
                continue;
            }
            AuroraAPI.getLeaderboards().registerBoard(
                    "quests_" + pool.getId(),
                    (user) -> (double) user.getData(QuestData.class).getCompletedCount(pool.getId()),
                    (lb) -> AuroraAPI.formatNumber(((Double) lb.getValue()).longValue()),
                    configManager.getConfig().getLeaderboards().getCacheSize(),
                    configManager.getConfig().getLeaderboards().getMinCompleted()
            );
        }

        HookManager.loadHooks(this);
    }

    @Override
    public void onEnable() {
        localizationProvider = new LocalizationProvider(Aurora.getLanguageProvider(), configManager.getConfig().getPerPlayerLocale());

        for (var msg : configManager.getMessageConfigs().entrySet()) {
            localizationProvider.setLocaleValues(msg.getKey(), msg.getValue().toFlatMap());
        }

        AuroraAPI.getUserManager().registerUserDataHolder(QuestData.class);
        AuroraAPI.registerPlaceholderHandler(new QuestPlaceholderHandler());
        Bukkit.getPluginManager().registerEvents(this, this);

        commandManager = new CommandManager(this);
        commandManager.reload();

        HookManager.enableHooks(this);

        var pools = new ArrayList<Pool>();

        for (var pool : configManager.getQuestPools().values()) {
            pools.add(PoolParser.parse(pool, poolManager.getRewardFactory()));
        }

        poolManager.reload(pools);
        loaded = true;
        loadPlayers();


        Bukkit.getGlobalRegionScheduler().run(this, (task) -> {
            reloadUnlockTask();
        });

        CommandDispatcher.registerActionHandler("quest-pool", (player, input) -> {
            var split = input.split("---");
            var poolId = split[0].trim();
            var profile = profileManager.getProfile(player);
            if (profile == null) return;
            var pool = profile.getQuestPool(poolId);
            if (pool == null) return;
            if (split.length > 1) {
                new PoolMenu(profile, pool, () -> CommandDispatcher.dispatch(player, split[1].trim())).open();
            } else {
                new PoolMenu(profile, pool).open();
            }
        });

        new Metrics(this, 23779).addCustomChart(new AdvancedPie("objective_type_distribution", () -> {
            var data = new HashMap<String, Integer>();

            for (var pool : poolManager.getPools()) {
                for (var quest : pool.getDefinition().getQuests().values()) {
                    for (var objective : quest.getTasks().values()) {
                        data.merge(objective.getTask(), 1, Integer::sum);
                    }
                }
            }

            return data;
        }));
    }

    public void reload() {
        configManager.reload();

        localizationProvider.clear();
        
        for (var msg : configManager.getMessageConfigs().entrySet()) {
            localizationProvider.setLocaleValues(msg.getKey(), msg.getValue().toFlatMap());
        }

        commandManager.reload();

        var pools = new ArrayList<Pool>();

        for (var pool : configManager.getQuestPools().values()) {
            pools.add(PoolParser.parse(pool, poolManager.getRewardFactory()));
        }

        poolManager.reload(pools);

        reloadUnlockTask();
        profileManager.getProfiles().forEach(p -> p.reload(true));
    }

    @Override
    public void onDisable() {
        commandManager.unregisterCommands();

        try {
            l.info("Shutting down scheduler...");
            StdSchedulerFactory.getDefaultScheduler().shutdown(true);
        } catch (SchedulerException e) {
            l.severe("Failed to shutdown scheduler: " + e.getMessage());
        }

        if (unlockTask != null && !unlockTask.isCancelled()) {
            unlockTask.cancel();
        }
    }

    private void registerObjectives() {
        ObjectiveFactory.registerObjective(ObjectiveType.BLOCK_LOOT, BlockLootObjective.class);
        ObjectiveFactory.registerObjective(ObjectiveType.BLOCK_BREAK, BlockBreakObjective.class);
        ObjectiveFactory.registerObjective(ObjectiveType.BLOCK_SHEAR, BlockShearObjective.class);
        ObjectiveFactory.registerObjective(ObjectiveType.BLOCK_SHEAR_LOOT, BlockShearLootObjective.class);
        ObjectiveFactory.registerObjective(ObjectiveType.BREED, BreedingObjective.class);
        ObjectiveFactory.registerObjective(ObjectiveType.BREW, BrewingObjective.class);
        ObjectiveFactory.registerObjective(ObjectiveType.BUILD, BuildingObjective.class);
        ObjectiveFactory.registerObjective(ObjectiveType.BLOCK_PLACE, BlockPlaceObjective.class);
        ObjectiveFactory.registerObjective(ObjectiveType.RUN_COMMAND, CommandObjective.class);
        ObjectiveFactory.registerObjective(ObjectiveType.CONSUME, ConsumeObjective.class);
        ObjectiveFactory.registerObjective(ObjectiveType.CRAFT, CraftObjective.class);
        ObjectiveFactory.registerObjective(ObjectiveType.ENCHANT, EnchantObjective.class);
        ObjectiveFactory.registerObjective(ObjectiveType.EARN_EXP, ExpEarnObjective.class);
        ObjectiveFactory.registerObjective(ObjectiveType.FARM, FarmingObjective.class);
        ObjectiveFactory.registerObjective(ObjectiveType.FISH, FishingObjective.class);
        ObjectiveFactory.registerObjective(ObjectiveType.MILK, MilkingObjective.class);
        ObjectiveFactory.registerObjective(ObjectiveType.KILL_MOB, MobKillObjective.class);
        ObjectiveFactory.registerObjective(ObjectiveType.KILL_LEVELLED_MOB, LevelledMobKillObjective.class);
        ObjectiveFactory.registerObjective(ObjectiveType.ENTITY_LOOT, EntityLootObjective.class);
        ObjectiveFactory.registerObjective(ObjectiveType.KILL_PLAYER, PlayerKillObjective.class);
        ObjectiveFactory.registerObjective(ObjectiveType.SHEAR, ShearObjective.class);
        ObjectiveFactory.registerObjective(ObjectiveType.SHEAR_LOOT, ShearLootObjective.class);
        ObjectiveFactory.registerObjective(ObjectiveType.SMELT, SmeltObjective.class);
        ObjectiveFactory.registerObjective(ObjectiveType.TAME, TameObjective.class);
        ObjectiveFactory.registerObjective(ObjectiveType.INTERACT_NPC, NpcInteractObjective.class);
        ObjectiveFactory.registerObjective(ObjectiveType.ENTER_REGION, EnterRegionObjective.class);
        ObjectiveFactory.registerObjective(ObjectiveType.COMPLETE_DUNGEON, CompleteDungeonObjective.class);
        ObjectiveFactory.registerObjective(ObjectiveType.SELL_WORTH, SellWorthObjective.class);
        ObjectiveFactory.registerObjective(ObjectiveType.BUY_WORTH, BuyWorthObjective.class);
        ObjectiveFactory.registerObjective(ObjectiveType.SELL, SellObjective.class);
        ObjectiveFactory.registerObjective(ObjectiveType.BUY, BuyObjective.class);
        ObjectiveFactory.registerObjective(ObjectiveType.JOIN_ISLAND, IslandJoinObjective.class);
        ObjectiveFactory.registerObjective(ObjectiveType.REACH_ISLAND_WORTH, IslandWorthObjective.class);
        ObjectiveFactory.registerObjective(ObjectiveType.REACH_ISLAND_LEVEL, IslandLevelObjective.class);
        ObjectiveFactory.registerObjective(ObjectiveType.UPGRADE_ISLAND, IslandUpgradeObjective.class);
        ObjectiveFactory.registerObjective(ObjectiveType.TAKE_ITEM, TakeItemObjective.class);
        ObjectiveFactory.registerObjective(ObjectiveType.PLACEHOLDER, PlaceholderObjective.class);
        ObjectiveFactory.registerObjective(ObjectiveType.ENTER_WORLD, EnterWorldObjective.class);
        ObjectiveFactory.registerObjective(ObjectiveType.DEAL_DAMAGE, DealDamageObjective.class);
        ObjectiveFactory.registerObjective(ObjectiveType.BREAK_ITEM, BreakItemObjective.class);
        ObjectiveFactory.registerObjective(ObjectiveType.TRAVEL, TravelObjective.class);
    }

    private void reloadUnlockTask() {
        var cf = configManager.getConfig().getUnlockTask();

        if (!cf.getEnabled()) {
            if (unlockTask != null && !unlockTask.isCancelled()) {
                unlockTask.cancel();
                unlockTask = null;
            }
            return;
        }

        unlockTask = Bukkit.getAsyncScheduler().runAtFixedRate(this, (task) -> {
            profileManager.getProfiles().forEach(profile -> {
                for (var pool : profile.getQuestPools()) {
                    pool.unlock(false);
                    pool.rollIfNecessary(true);
                    pool.startQuests();
                }
            });
        }, cf.getInterval(), cf.getInterval(), TimeUnit.SECONDS);
    }

    private final Set<Player> toLoad = new HashSet<>();

    @EventHandler
    public void onUserLoaded(AuroraUserLoadedEvent event) {
        if (event.getUser().getPlayer() != null) {
            if (loaded) {
                profileManager.createProfile(event.getUser());
            } else {
                toLoad.add(event.getUser().getPlayer());
            }
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        profileManager.destroyProfile(event.getPlayer().getUniqueId());
        toLoad.remove(event.getPlayer());
    }

    @EventHandler
    public void onQuestComplete(QuestCompletedEvent event) {
        var profile = profileManager.getProfile(event.getPlayer());
        for (var pool : profile.getQuestPools()) {
            pool.unlock(false);
            pool.rollIfNecessary(true);
            pool.startQuests();
        }
    }

    private void loadPlayers() {
        for (var player : toLoad) {
            var user = AuroraUser.get(player.getUniqueId());
            profileManager.createProfile(user);
        }
        toLoad.clear();
    }
}