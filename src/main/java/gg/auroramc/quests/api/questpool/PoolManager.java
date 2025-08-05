package gg.auroramc.quests.api.questpool;

import gg.auroramc.aurora.api.reward.*;
import gg.auroramc.aurora.api.util.NamespacedId;
import gg.auroramc.quests.AuroraQuests;
import gg.auroramc.quests.api.event.QuestsLoadedEvent;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.quartz.SchedulerException;
import org.quartz.impl.StdSchedulerFactory;

import java.util.*;

public class PoolManager {
    @Getter
    private final RewardFactory rewardFactory = new RewardFactory();
    @Getter
    private final RewardAutoCorrector rewardAutoCorrector = new RewardAutoCorrector();

    private final Map<String, Pool> registry = new HashMap<>();

    public PoolManager() {
        rewardFactory.registerRewardType(NamespacedId.fromDefault("command"), CommandReward.class);
        rewardFactory.registerRewardType(NamespacedId.fromDefault("money"), MoneyReward.class);
        rewardFactory.registerRewardType(NamespacedId.fromDefault("item"), ItemReward.class);

        try {
            StdSchedulerFactory.getDefaultScheduler().start();
        } catch (SchedulerException e) {
            AuroraQuests.logger().severe("Failed to start scheduler: " + e.getMessage());
        }
    }

    public void reload(List<Pool> pools) {
        for (Pool pool : registry.values()) {
            pool.dispose();
        }
        registry.clear();

        for (Pool pool : pools) {
            registry.put(pool.getId(), pool);
            pool.start();
        }
        Bukkit.getPluginManager().callEvent(new QuestsLoadedEvent());
    }

    public Pool getPool(String id) {
        return registry.get(id);
    }

    public Set<String> getPoolIds() {
        return Collections.unmodifiableSet(registry.keySet());
    }

    public Collection<Pool> getPools() {
        return Collections.unmodifiableCollection(registry.values());
    }
}
