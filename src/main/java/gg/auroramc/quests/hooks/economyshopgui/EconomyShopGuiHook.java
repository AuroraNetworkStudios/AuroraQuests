package gg.auroramc.quests.hooks.economyshopgui;

import gg.auroramc.quests.AuroraQuests;
import gg.auroramc.quests.hooks.Hook;
import org.bukkit.Bukkit;

public class EconomyShopGuiHook implements Hook {
    @Override
    public void hook(AuroraQuests plugin) {
        Bukkit.getPluginManager().registerEvents(new EconomyShopGUIListener(), plugin);
        AuroraQuests.logger().info("Hooked into EconomyShopGUI for SELL_WORTH, SELL and BUY_WORTH, BUY objectives.");
    }
}
