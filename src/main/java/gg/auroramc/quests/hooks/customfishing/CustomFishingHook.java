package gg.auroramc.quests.hooks.customfishing;

import gg.auroramc.quests.AuroraQuests;
import gg.auroramc.quests.hooks.Hook;
import org.bukkit.Bukkit;

public class CustomFishingHook implements Hook {
    @Override
    public void hook(AuroraQuests plugin) {
        Bukkit.getPluginManager().registerEvents(new CustomFishingListener(), plugin);
        AuroraQuests.logger().info("Hooked into CustomFishing for FISH objective.");
    }
}
