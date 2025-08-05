package gg.auroramc.quests.hooks.shopguiplus;

import gg.auroramc.aurora.api.AuroraAPI;
import gg.auroramc.quests.AuroraQuests;
import gg.auroramc.quests.api.event.objective.PlayerEarnFromSellEvent;
import gg.auroramc.quests.api.event.objective.PlayerPurchaseItemEvent;
import gg.auroramc.quests.api.event.objective.PlayerSellItemEvent;
import gg.auroramc.quests.api.event.objective.PlayerSpendOnPurchaseEvent;
import gg.auroramc.quests.hooks.Hook;
import net.brcdev.shopgui.event.ShopPostTransactionEvent;
import net.brcdev.shopgui.shop.ShopManager;
import net.brcdev.shopgui.shop.ShopTransactionResult;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class ShopGUIPlusHook implements Hook, Listener {
    @Override
    public void hook(AuroraQuests plugin) {
        AuroraQuests.logger().info("Hooked into ShopGUIPlus for BUY_WORTH, BUY and SELL_WORTH, SELL objectives.");
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPostTransaction(ShopPostTransactionEvent event) {
        var res = event.getResult();

        if (res.getResult() != ShopTransactionResult.ShopTransactionResultType.SUCCESS) return;
        if (res.getPlayer() == null) return;

        var price = res.getPrice();
        var item = res.getShopItem().getItem();
        var id = item != null ? AuroraAPI.getItemManager().resolveId(item) : null;

        if (res.getShopAction() == ShopManager.ShopAction.BUY) {
            if (id != null) {
                Bukkit.getPluginManager().callEvent(new PlayerPurchaseItemEvent(res.getPlayer(), new PlayerPurchaseItemEvent.TransactionItem(id, res.getAmount())));
            }
            Bukkit.getPluginManager().callEvent(new PlayerSpendOnPurchaseEvent(res.getPlayer(), price));

        } else {
            if (id != null) {
                Bukkit.getPluginManager().callEvent(new PlayerSellItemEvent(res.getPlayer(), new PlayerSellItemEvent.TransactionItem(id, res.getAmount())));
            }
            Bukkit.getPluginManager().callEvent(new PlayerEarnFromSellEvent(res.getPlayer(), price));
        }
    }
}
