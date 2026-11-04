package gg.auroramc.quests.hooks.economyshopgui;

import gg.auroramc.aurora.api.plugin.Hook;
import gg.auroramc.quests.AuroraQuests;
import org.bukkit.Bukkit;

public class EconomyShopGuiHook implements Hook<AuroraQuests> {
    @Override
    public void hook(AuroraQuests plugin) {
        Bukkit.getPluginManager().registerEvents(new EconomyShopGUIListener(), plugin);
        AuroraQuests.logger().info("Hooked into EconomyShopGUI for SELL_WORTH, SELL and BUY_WORTH, BUY objectives.");
    }
}
