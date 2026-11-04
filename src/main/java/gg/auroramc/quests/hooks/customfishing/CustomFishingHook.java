package gg.auroramc.quests.hooks.customfishing;

import gg.auroramc.aurora.api.plugin.Hook;
import gg.auroramc.quests.AuroraQuests;
import org.bukkit.Bukkit;

public class CustomFishingHook implements Hook<AuroraQuests> {
    @Override
    public void hook(AuroraQuests plugin) {
        Bukkit.getPluginManager().registerEvents(new CustomFishingListener(), plugin);
        AuroraQuests.logger().info("Hooked into CustomFishing for FISH objective.");
    }
}
