package gg.auroramc.quests.api.tracking;

import gg.auroramc.quests.api.event.PlayerTrackedObjectiveChangeEvent;
import gg.auroramc.quests.api.objective.Objective;
import gg.auroramc.quests.api.profile.Profile;
import gg.auroramc.quests.api.questpool.QuestPool;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.Nullable;

import java.util.stream.Stream;

public class QuestTracker {
    private final Profile profile;
    private volatile Objective trackedObjective;
    // true when the plugin picked the objective, not the player; the player's first progress replaces it
    private volatile boolean pickedAutomatically;

    public QuestTracker(Profile profile) {
        this.profile = profile;
    }

    public Objective getTrackedObjective() {
        return trackedObjective;
    }

    public void setTrackedObjective(@Nullable Objective objective) {
        if (objective != null && objective.getQuest().getPool().getProfile() != profile) {
            throw new IllegalArgumentException("Objective belongs to another player");
        }
        changeTrackedObjective(objective, false);
    }

    public void onObjectiveProgress(Objective objective) {
        var current = trackedObjective;
        if (current == null || pickedAutomatically || !isObjectiveActive(current)) {
            changeTrackedObjective(objective, false);
        }
    }

    public void refresh() {
        var current = trackedObjective;
        if (current != null && isObjectiveActive(current)) return;
        var preferredPool = current == null ? null : current.getQuest().getPool();
        changeTrackedObjective(findFirstIncompleteObjective(preferredPool), true);
    }

    // the profile was reloaded, so the tracked objective belongs to a destroyed pool
    public void reset() {
        trackedObjective = null;
        refresh();
    }

    private void changeTrackedObjective(@Nullable Objective newObjective, boolean automatic) {
        var previous = trackedObjective;
        if (previous == newObjective) {
            pickedAutomatically = automatic;
            return;
        }
        var event = new PlayerTrackedObjectiveChangeEvent(profile.getPlayer(), previous, newObjective);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) return;
        trackedObjective = event.getNewObjective();
        pickedAutomatically = automatic;
    }

    // a timed pool re-roll can drop the tracked quest while it is still incomplete
    private static boolean isObjectiveActive(Objective objective) {
        var pool = objective.getQuest().getPool();
        return !objective.isCompleted() && (!pool.isTimedRandom() || pool.isRolledQuest(objective.getQuest()));
    }

    private Objective findFirstIncompleteObjective(@Nullable QuestPool preferredPool) {
        if (preferredPool != null && preferredPool.isUnlocked()) {
            var inPreferredPool = firstIncompleteObjectiveIn(Stream.of(preferredPool));
            if (inPreferredPool != null) return inPreferredPool;
        }
        return firstIncompleteObjectiveIn(profile.getQuestPools().stream());
    }

    private static Objective firstIncompleteObjectiveIn(Stream<QuestPool> pools) {
        return pools.filter(QuestPool::isUnlocked)
                .flatMap(pool -> pool.getActiveQuests().stream())
                .filter(quest -> !quest.isCompleted())
                .flatMap(quest -> quest.getObjectives().stream())
                .filter(objective -> !objective.isCompleted())
                .findFirst().orElse(null);
    }
}
