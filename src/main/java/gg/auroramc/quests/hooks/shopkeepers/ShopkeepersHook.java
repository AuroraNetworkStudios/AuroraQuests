package gg.auroramc.quests.hooks.shopkeepers;

import gg.auroramc.aurora.api.plugin.Hook;
import gg.auroramc.quests.AuroraQuests;
import gg.auroramc.quests.api.factory.ObjectiveFactory;
import gg.auroramc.quests.api.objective.ObjectiveType;
import gg.auroramc.quests.hooks.shopkeepers.objective.InteractShopkeeperObjective;
import gg.auroramc.quests.hooks.shopkeepers.objective.TradeShopkeeperObjective;

public class ShopkeepersHook implements Hook<AuroraQuests> {
    @Override
    public void hook(AuroraQuests plugin) {
        ObjectiveFactory.registerObjective(ObjectiveType.TRADE_SHOPKEEPER, TradeShopkeeperObjective.class);
        ObjectiveFactory.registerObjective(ObjectiveType.INTERACT_SHOPKEEPER, InteractShopkeeperObjective.class);

        AuroraQuests.logger().info("Hooked into Shopkeepers for INTERACT_SHOPKEEPER and TRADE_SHOPKEEPER objectives.");
    }
}
