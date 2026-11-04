package gg.auroramc.quests.hooks.znpcsplus;

import gg.auroramc.aurora.api.plugin.Hook;
import gg.auroramc.quests.AuroraQuests;
import org.bukkit.Bukkit;

public class ZnpcPlusHook implements Hook<AuroraQuests> {
    @Override
    public void hook(AuroraQuests plugin) {
        Bukkit.getPluginManager().registerEvents(new ZnpcPlusListener(), plugin);
        AuroraQuests.logger().info("Hooked into zNPCsPlus for INTERACT_NPC objective.");
    }
}
