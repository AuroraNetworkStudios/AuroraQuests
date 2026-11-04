package gg.auroramc.quests.hooks.citizens;

import gg.auroramc.aurora.api.plugin.Hook;
import gg.auroramc.quests.AuroraQuests;
import org.bukkit.Bukkit;

public class CitizensHook implements Hook<AuroraQuests> {
    @Override
    public void hook(AuroraQuests plugin) {
        Bukkit.getPluginManager().registerEvents(new CitizensListener(), plugin);
        AuroraQuests.logger().info("Hooked into Citizens for INTERACT_NPC objective.");
    }
}
