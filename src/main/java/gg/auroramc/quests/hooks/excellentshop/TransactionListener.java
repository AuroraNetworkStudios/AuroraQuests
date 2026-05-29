package gg.auroramc.quests.hooks.excellentshop;

import gg.auroramc.quests.api.event.objective.PlayerEarnFromSellEvent;
import gg.auroramc.quests.api.event.objective.PlayerSpendOnPurchaseEvent;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import su.nightexpress.excellentshop.api.event.TransactionCompletedEvent;
import su.nightexpress.excellentshop.api.product.TradeType;
import su.nightexpress.excellentshop.api.transaction.ECompletedTransaction;

public class TransactionListener implements Listener {

    @EventHandler(priority = EventPriority.MONITOR)
    public void onTransaction(TransactionCompletedEvent event) {
        if (event.isCancelled() || !event.getTransaction().successful()) return;
        final ECompletedTransaction transaction = event.getTransaction();
        if (transaction.type() == TradeType.BUY) {
            transaction.worth().getBalanceMap().forEach((currency, worth) -> {
                Bukkit.getPluginManager().callEvent(new PlayerSpendOnPurchaseEvent(transaction.player(), worth, currency));
            });
        } else {
            transaction.worth().getBalanceMap().forEach((currency, worth) -> {
                Bukkit.getPluginManager().callEvent(new PlayerEarnFromSellEvent(transaction.player(), worth, currency));
            });
        }
    }
}
