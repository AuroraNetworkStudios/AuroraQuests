package gg.auroramc.quests.hooks.citizens;

import gg.auroramc.quests.AuroraQuests;
import gg.auroramc.quests.hooks.Hook;
import org.bukkit.Bukkit;

public class CitizensHook implements Hook {
    @Override
    public void hook(AuroraQuests plugin) {
        Bukkit.getPluginManager().registerEvents(new CitizensListener(), plugin);
        AuroraQuests.logger().info("Hooked into Citizens for INTERACT_NPC objective.");
    }
}
