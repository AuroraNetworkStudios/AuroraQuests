package gg.auroramc.quests.hooks.adyeshach;

import gg.auroramc.quests.AuroraQuests;
import gg.auroramc.quests.hooks.Hook;
import org.bukkit.Bukkit;

public class AdyeshachHook implements Hook {
    @Override
    public void hook(AuroraQuests plugin) {
        Bukkit.getPluginManager().registerEvents(new NpcListener(), AuroraQuests.getInstance());
        AuroraQuests.logger().info("Hooked into Adyeshach for INTERACT_NPC objective.");
    }
}
