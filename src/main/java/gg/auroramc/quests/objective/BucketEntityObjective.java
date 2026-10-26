package gg.auroramc.quests.objective;

import gg.auroramc.quests.api.objective.ObjectiveDefinition;
import gg.auroramc.quests.api.objective.TypedObjective;
import gg.auroramc.quests.api.profile.Profile;
import gg.auroramc.quests.api.quest.Quest;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerBucketEntityEvent;
import org.bukkit.event.player.PlayerBucketFishEvent;

public class BucketEntityObjective extends TypedObjective {

    public BucketEntityObjective(Quest quest, ObjectiveDefinition definition, Profile.TaskDataWrapper data) {
        super(quest, definition, data);
    }

    @Override
    @SuppressWarnings("deprecation")
    protected void activate() {
        onEvent(PlayerBucketEntityEvent.class, this::handle, EventPriority.MONITOR);
        // The server still fires this deprecated subclass instead of the base event for fish
        onEvent(PlayerBucketFishEvent.class, this::handle, EventPriority.MONITOR);
    }

    public void handle(PlayerBucketEntityEvent event) {
        var entity = event.getEntity();
        progress(1, meta(entity.getLocation(), entity.getType()));
    }
}
