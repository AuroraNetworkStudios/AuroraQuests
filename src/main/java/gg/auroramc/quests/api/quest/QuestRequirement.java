package gg.auroramc.quests.api.quest;

import gg.auroramc.aurora.api.item.TypeId;
import gg.auroramc.quests.api.profile.Profile;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@AllArgsConstructor
@Getter
public class QuestRequirement {
    private boolean alwaysShowInMenu;
    private boolean needsManualUnlock;
    private List<String> quests;
    private List<String> permissions;

    public boolean hasRequirements() {
        return (quests != null && !quests.isEmpty()) || (permissions != null && !permissions.isEmpty()) || needsManualUnlock;
    }

    public boolean canStart(Profile profile, String poolId) {
        var data = profile.getData();

        if (needsManualUnlock && data.isPoolUnlocked(poolId)) {
            return false;
        }

        return meetsRequirements(profile, poolId);
    }

    public boolean canStart(Profile.QuestDataWrapper data) {
        if (needsManualUnlock && !data.isUnlocked()) return false;
        return meetsRequirements(data.profile(), data.poolId());
    }

    private boolean meetsRequirements(Profile profile, String poolId) {
        var player = profile.getPlayer();
        var data = profile.getData();

        if (quests != null && !quests.isEmpty()) {
            for (var questId : quests) {
                var typeId = TypeId.fromString(questId);
                var pool = typeId.namespace().equals("minecraft") ? poolId : typeId.namespace();
                if (!data.hasCompletedQuest(pool, typeId.id())) {
                    return false;
                }
            }
        }

        if (permissions != null && !permissions.isEmpty()) {
            for (var perm : permissions) {
                if (!player.hasPermission(perm)) {
                    return false;
                }
            }
        }

        return true;
    }

}
