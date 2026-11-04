package gg.auroramc.quests.hooks.fancynpcs;

import gg.auroramc.aurora.api.plugin.Hook;
import gg.auroramc.quests.AuroraQuests;
import org.bukkit.Bukkit;

public class FancyNPCsHook implements Hook<AuroraQuests> {
    @Override
    public void hook(AuroraQuests plugin) {
        Bukkit.getPluginManager().registerEvents(new NpcListener(), AuroraQuests.getInstance());
        AuroraQuests.logger().info("Hooked into FancyNPCs for INTERACT_NPC objective.");
    }
}
