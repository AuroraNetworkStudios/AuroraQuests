package gg.auroramc.quests.hooks.znpcs;

import gg.auroramc.aurora.api.plugin.Hook;
import gg.auroramc.quests.AuroraQuests;
import org.bukkit.Bukkit;

public class ZnpcsHook implements Hook<AuroraQuests> {
    @Override
    public void hook(AuroraQuests plugin) {
        Bukkit.getPluginManager().registerEvents(new ZnpcListener(), plugin);
        AuroraQuests.logger().info("Hooked into ZNPCS for INTERACT_NPC objective.");
    }
}