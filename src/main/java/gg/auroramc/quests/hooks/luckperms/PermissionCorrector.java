package gg.auroramc.quests.hooks.luckperms;

import gg.auroramc.aurora.api.reward.PermissionReward;
import gg.auroramc.aurora.api.reward.RewardCorrector;
import gg.auroramc.quests.AuroraQuests;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.node.Node;
import net.luckperms.api.node.NodeEqualityPredicate;
import net.luckperms.api.util.Tristate;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class PermissionCorrector implements RewardCorrector {
    @Override
    public void correctRewards(Player player) {
        var plugin = AuroraQuests.getInstance();
        var profile = plugin.getProfileManager().getProfile(player);

        List<Node> nodesToAdd = new ArrayList<>();

        for (var pool : profile.getQuestPools()) {
            // Correct global quests
            if (pool.isGlobal()) {
                for (var quest : pool.getQuests()) {
                    if (!quest.isCompleted()) continue;

                    for (var reward : quest.getDefinition().getRewards().values()) {
                        if (reward instanceof PermissionReward permissionReward) {
                            if (permissionReward.getPermissions() == null || permissionReward.getPermissions().isEmpty())
                                continue;
                            var nodes = permissionReward.buildNodes(player, quest.getPlaceholders());
                            nodesToAdd.addAll(nodes);
                        }
                    }
                }
            }

            // Correct quest pool leveling
            if (!pool.hasLeveling()) continue;
            var level = pool.getLevel();

            for (int i = 1; i < level + 1; i++) {
                var matcher = pool.getPool().getMatcherManager().getBestMatcher(i);
                if (matcher == null) continue;
                var placeholders = pool.getLevelPlaceholders(i);
                for (var reward : matcher.computeRewards(i)) {
                    if (reward instanceof PermissionReward permissionReward) {
                        if (permissionReward.getPermissions() == null || permissionReward.getPermissions().isEmpty())
                            continue;

                        var nodes = permissionReward.buildNodes(player, placeholders);

                        nodesToAdd.addAll(nodes);

                    }
                }
            }
        }

        if (nodesToAdd.isEmpty()) return;

        updatePermissionNodes(player, nodesToAdd);
    }

    private void updatePermissionNodes(Player player, List<Node> nodes) {
        var user = LuckPermsProvider.get().getUserManager().getUser(player.getUniqueId());
        if (user == null) {
            AuroraQuests.logger().severe("User " + player.getName() + " is not loaded in LuckPerms, failed to correct permission rewards!");
            return;
        }

        int addedNodes = 0;

        for (var node : nodes) {
            var hasPermission = user.data().contains(node, NodeEqualityPredicate.EXACT);

            if (hasPermission.equals(Tristate.UNDEFINED)) {
                AuroraQuests.logger().debug("Permission " + node.getKey() + " is undefined for player " + player.getName());
                user.data().add(node);
                addedNodes++;
            }
        }

        if (addedNodes > 0) {
            LuckPermsProvider.get().getUserManager().saveUser(user);
            AuroraQuests.logger().debug("Added " + addedNodes + " permission nodes to player " + player.getName());
        }
    }
}
