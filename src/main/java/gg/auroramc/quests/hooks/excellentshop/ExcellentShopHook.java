package gg.auroramc.quests.hooks.excellentshop;

import gg.auroramc.quests.AuroraQuests;
import gg.auroramc.quests.hooks.Hook;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;

public class ExcellentShopHook implements Hook, Listener {
    @Override
    public void hook(AuroraQuests plugin) {
        if (canHook()) {
            Bukkit.getPluginManager().registerEvents(new TransactionListener(), plugin);
            AuroraQuests.logger().info("Hooked into ExcellentShop for BUY_WORTH and SELL_WORTH objectives.");
        } else {
            AuroraQuests.logger().warning("Could not hook into ExcellentShop, because it is outdated.");
        }
    }

    public static boolean canHook() {
        try {
            Class.forName("su.nightexpress.nightcore.bridge.currency.Currency");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }
}
