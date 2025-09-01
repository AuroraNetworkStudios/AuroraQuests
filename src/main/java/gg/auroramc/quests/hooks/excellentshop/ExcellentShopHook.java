package gg.auroramc.quests.hooks.excellentshop;

import gg.auroramc.quests.AuroraQuests;
import gg.auroramc.quests.api.event.objective.PlayerEarnFromSellEvent;
import gg.auroramc.quests.api.event.objective.PlayerSpendOnPurchaseEvent;
import gg.auroramc.quests.hooks.Hook;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import su.nightexpress.nexshop.api.shop.Transaction;
import su.nightexpress.nexshop.api.shop.event.ShopTransactionEvent;
import su.nightexpress.nexshop.api.shop.type.TradeType;

public class ExcellentShopHook implements Hook, Listener {
    private static Boolean hasGetCurrencyMethod = null;

    @Override
    public void hook(AuroraQuests plugin) {
        AuroraQuests.logger().info("Hooked into ExcellentShop for BUY_WORTH and SELL_WORTH objectives.");
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onTransaction(ShopTransactionEvent event) {
        var transaction = event.getTransaction();
        if (transaction.getResult() == Transaction.Result.FAILURE) return;
        var price = transaction.getPrice() * transaction.getAmount();

        String currencyName = null;

        if (hasGetCurrencyMethod(transaction)) {
            currencyName = transaction.getCurrency().getName();
        }

        if (transaction.getTradeType() == TradeType.BUY) {
            Bukkit.getPluginManager().callEvent(new PlayerSpendOnPurchaseEvent(event.getPlayer(), price, currencyName));
        } else {
            Bukkit.getPluginManager().callEvent(new PlayerEarnFromSellEvent(event.getPlayer(), price, currencyName));
        }
    }

    private static boolean hasGetCurrencyMethod(Object obj) {
        if (hasGetCurrencyMethod != null) {
            return hasGetCurrencyMethod;
        }
        try {
            obj.getClass().getMethod("getCurrency");
            hasGetCurrencyMethod = true;
            return true;
        } catch (NoSuchMethodException e) {
            hasGetCurrencyMethod = false;
            return false;
        }
    }
}
