package gg.auroramc.quests.hooks.znpcs;

import gg.auroramc.quests.AuroraQuests;
import gg.auroramc.quests.hooks.Hook;
import org.bukkit.Bukkit;

public class ZnpcsHook implements Hook {
    @Override
    public void hook(AuroraQuests plugin) {
        Bukkit.getPluginManager().registerEvents(new ZnpcListener(), plugin);
        AuroraQuests.logger().info("Hooked into ZNPCS for INTERACT_NPC objective.");
    }
}