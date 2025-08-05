package gg.auroramc.quests.objective;

import gg.auroramc.aurora.api.AuroraAPI;
import gg.auroramc.aurora.api.item.TypeId;
import gg.auroramc.quests.AuroraQuests;
import gg.auroramc.quests.api.event.objective.PlayerTakeItemEvent;
import gg.auroramc.quests.api.objective.ObjectiveDefinition;
import gg.auroramc.quests.api.objective.TypedObjective;
import gg.auroramc.quests.api.profile.Profile;
import gg.auroramc.quests.api.quest.Quest;
import org.bukkit.event.EventPriority;

public class TakeItemObjective extends TypedObjective {

    public TakeItemObjective(Quest quest, ObjectiveDefinition definition, Profile.TaskDataWrapper data) {
        super(quest, definition, data);
    }

    @Override
    protected void activate() {
        onEvent(PlayerTakeItemEvent.class, this::handle, EventPriority.MONITOR);
    }

    public void handle(PlayerTakeItemEvent event) {
        if (event.getQuest() != quest) return;

        var args = definition.getArgs();
        var player = data.profile().getPlayer();
        var itemId = args.getString("item");
        final var currentAmount = (int) data.getProgress();
        final var requiredAmount = args.getInt("amount", 1);
        final var remainingAmount = requiredAmount - currentAmount;
        if (itemId == null || remainingAmount <= 0) {
            return;
        }

        final var typeId = TypeId.fromString(itemId);
        try {
            var amountNeeded = remainingAmount;

            for (var invItem : player.getInventory().getContents()) {
                if (invItem == null) continue;

                if (AuroraAPI.getItemManager().resolveId(invItem).equals(typeId)) {
                    var amount = invItem.getAmount();
                    if (amount > amountNeeded) {
                        invItem.setAmount(amount - amountNeeded);
                        amountNeeded = 0;
                        break;
                    } else {
                        amountNeeded -= amount;
                        invItem.setAmount(0);
                    }
                }
            }

            if (remainingAmount - amountNeeded <= 0) return;

            setProgress(currentAmount + (remainingAmount - amountNeeded));
        } catch (Exception e) {
            AuroraQuests.logger().severe("Failed to take items");
            e.printStackTrace();
        }
    }
}
