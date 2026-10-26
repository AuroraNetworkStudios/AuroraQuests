package gg.auroramc.quests.objective;

import gg.auroramc.quests.api.objective.ObjectiveDefinition;
import gg.auroramc.quests.api.objective.TypedObjective;
import gg.auroramc.quests.api.profile.Profile;
import gg.auroramc.quests.api.quest.Quest;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.ProjectileLaunchEvent;

public class LaunchProjectileObjective extends TypedObjective {

    public LaunchProjectileObjective(Quest quest, ObjectiveDefinition definition, Profile.TaskDataWrapper data) {
        super(quest, definition, data);
    }

    @Override
    protected void activate() {
        onEvent(ProjectileLaunchEvent.class, this::handle, EventPriority.MONITOR);
    }

    public void handle(ProjectileLaunchEvent event) {
        var projectile = event.getEntity();
        if (projectile.getShooter() != data.profile().getPlayer()) return;
        progress(1, meta(projectile.getType()));
    }
}
