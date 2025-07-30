package gg.auroramc.quests.hooks.znpcsplus;

import gg.auroramc.quests.AuroraQuests;
import gg.auroramc.quests.hooks.Hook;
import org.bukkit.Bukkit;

public class ZnpcPlusHook implements Hook {
    @Override
    public void hook(AuroraQuests plugin) {
        Bukkit.getPluginManager().registerEvents(new ZnpcPlusListener(), plugin);
        AuroraQuests.logger().info("Hooked into zNPCsPlus for INTERACT_NPC objective.");
    }
}
