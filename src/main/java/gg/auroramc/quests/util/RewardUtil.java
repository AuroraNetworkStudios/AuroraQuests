package gg.auroramc.quests.util;

import gg.auroramc.aurora.api.message.Placeholder;
import gg.auroramc.aurora.api.message.Text;
import gg.auroramc.aurora.api.reward.Reward;
import gg.auroramc.quests.AuroraQuests;
import gg.auroramc.quests.config.Config;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.List;

public class RewardUtil {
    public static Component fillRewardMessage(Player player, Config.DisplayComponent config, List<String> lines, List<Placeholder<?>> placeholders, Collection<Reward> rewards) {
        var localization = AuroraQuests.getInstance().getLocalizationProvider();
        var text = Component.text();

        for (var line : lines) {
            if (line.equals("component:rewards")) {
                if (!rewards.isEmpty()) {
                    text.append(Text.component(player, localization.fillVariables(player, config.getTitle()), placeholders));
                }
                for (var reward : rewards) {
                    var rewardText = reward.getDisplay(player, placeholders);
                    if (rewardText.isBlank()) continue;
                    text.append(Component.newline());
                    var display = config.getLine().replace("{reward}", rewardText);
                    text.append(Text.component(player, localization.fillVariables(player, display), placeholders));
                }
            } else {
                text.append(Text.component(player,
                        // This might look like a lot of extra work, but there are situations when it is needed.
                        localization.fillVariables(player, Placeholder.execute(localization.fillVariables(player, line), placeholders)),
                        placeholders));
            }

            if (!line.equals(lines.getLast())) text.append(Component.newline());
        }

        return text.build();
    }
}
