package gg.auroramc.quests.hooks.luckperms;

import gg.auroramc.aurora.api.reward.PermissionReward;
import gg.auroramc.aurora.api.util.NamespacedId;
import gg.auroramc.quests.AuroraQuests;
import gg.auroramc.quests.hooks.Hook;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.event.user.UserDataRecalculateEvent;
import org.bukkit.Bukkit;

public class LuckPermsHook implements Hook {
    private AuroraQuests plugin;

    @Override
    public void hook(AuroraQuests plugin) {
        this.plugin = plugin;

        plugin.getPoolManager().getRewardFactory()
                .registerRewardType(NamespacedId.fromDefault("permission"), PermissionReward.class);

        plugin.getPoolManager().getRewardAutoCorrector()
                .registerCorrector(NamespacedId.fromDefault("permission"), new PermissionCorrector());

        var lp = LuckPermsProvider.get();

        lp.getEventBus().subscribe(UserDataRecalculateEvent.class, this::onDataRecalculate);

        AuroraQuests.logger().info("Hooked into LuckPerms for permission rewards and for permission start requirements.");
    }

    // Use synchronized since luckperms events are async and it likes to fire the event multiple times at once
    private synchronized void onDataRecalculate(UserDataRecalculateEvent event) {
        var player = Bukkit.getPlayer(event.getUser().getUniqueId());
        if (player != null) {
            var profile = plugin.getProfileManager().getProfile(player);
            if (profile != null) {
                for (var pool : profile.getQuestPools()) {
                    pool.unlock(false);
                    pool.rollIfNecessary(true);
                    pool.startQuests();
                }
            }
        }
    }
}
