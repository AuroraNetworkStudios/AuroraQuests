package gg.auroramc.quests.api.quest;

import gg.auroramc.aurora.api.AuroraAPI;
import gg.auroramc.aurora.api.item.TypeId;
import gg.auroramc.aurora.api.message.Placeholder;
import gg.auroramc.quests.AuroraQuests;
import gg.auroramc.quests.api.data.QuestData;
import gg.auroramc.quests.config.quest.TaskConfig;
import org.bukkit.entity.Player;

import java.util.Map;

public record Task(QuestPool pool, Quest holder, TaskConfig config, String id) {
    public void progress(Player player, double count, Map<String, Object> params) {
        if (!TaskManager.getEvaluator(config.getTask()).evaluate(player, config, params)) return;

        AuroraAPI.getUser(player.getUniqueId()).getData(QuestData.class)
                .progress(pool.getId(), holder.getId(), id, count);
    }

    public void setProgress(Player player, double count, Map<String, Object> params) {
        if (!TaskManager.getEvaluator(config.getTask()).evaluate(player, config, params)) return;

        AuroraAPI.getUser(player.getUniqueId()).getData(QuestData.class)
                .setProgress(pool.getId(), holder.getId(), id, count);
    }

    public void tryTakeItems(Player player) {
        if (!TaskManager.getEvaluator(config.getTask()).evaluate(player, config, Map.of())) {
            return;
        }
        var itemId = config.getArgs().getString("item");
        final var currentAmount = (int) AuroraAPI.getUser(player.getUniqueId()).getData(QuestData.class).getProgression(pool.getId(), holder.getId(), id);
        final var requiredAmount = config.getArgs().getInt("amount", 1);
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

            AuroraAPI.getUser(player.getUniqueId()).getData(QuestData.class)
                    .progress(pool.getId(), holder.getId(), id, remainingAmount - amountNeeded);
        } catch (Exception e) {
            AuroraQuests.logger().severe("Failed to take items");
            e.printStackTrace();
        }
    }

    public String getTaskType() {
        return config.getTask();
    }

    public boolean isCompleted(Player player) {
        if (holder.isCompleted(player)) return true;
        var data = AuroraAPI.getUser(player.getUniqueId()).getData(QuestData.class);
        var count = data.getProgression(pool.getId(), holder.getId(), id);
        return count >= config.getArgs().getInt("amount", 1);
    }

    public String getDisplay(Player player) {
        var gc = AuroraQuests.getInstance().getConfigManager().getCommonMenuConfig().getTaskStatuses();
        var data = AuroraAPI.getUser(player.getUniqueId()).getData(QuestData.class);
        var required = config.getArgs().getInt("amount", 1);
        var current = isCompleted(player) ? required : data.getProgression(pool.getId(), holder.getId(), id);

        return Placeholder.execute(config.getDisplay(),
                Placeholder.of("{status}", isCompleted(player) ? gc.getCompleted() : gc.getNotCompleted()),
                Placeholder.of("{current}", AuroraAPI.formatNumber(Math.min(current, required))),
                Placeholder.of("{required}", AuroraAPI.formatNumber(required))
        );
    }
}
