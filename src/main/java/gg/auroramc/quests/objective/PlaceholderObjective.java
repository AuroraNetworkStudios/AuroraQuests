package gg.auroramc.quests.objective;

import gg.auroramc.aurora.api.menu.Requirement;
import gg.auroramc.quests.AuroraQuests;
import gg.auroramc.quests.api.objective.Objective;
import gg.auroramc.quests.api.objective.ObjectiveDefinition;
import gg.auroramc.quests.api.profile.Profile;
import gg.auroramc.quests.api.quest.Quest;

import java.util.List;

public class PlaceholderObjective extends Objective {
    private volatile String composedRequirement;

    public PlaceholderObjective(Quest quest, ObjectiveDefinition definition, Profile.TaskDataWrapper data) {
        super(quest, definition, data);
    }

    @Override
    protected void activate() {
        if (placeholderToCheck() == null) {
            AuroraQuests.logger().warning("Failed to start placeholder objective with id: " + getId() + ", reason: args.placeholder isn't defined in config");
            return;
        }

        if (value() == null) {
            AuroraQuests.logger().warning("Failed to start placeholder objective with id: " + getId() + ", reason: args.value isn't defined in config");
            return;
        }

        composedRequirement = "[placeholder] " + placeholderToCheck() + " " + comparisonType() + " " + value();

        if (isAsync()) {
            asyncInterval(this::handler, getCheckingInterval(), getCheckingInterval());
        } else {
            syncInterval(this::handler, getCheckingInterval(), getCheckingInterval(), false);
        }
    }

    private void handler() {
        if (Requirement.isMet(data.profile().getPlayer(), composedRequirement, List.of())) {
            progress(1, meta());
        }
    }

    private boolean isAsync() {
        return definition.getArgs().getBoolean("async", false);
    }

    private int getCheckingInterval() {
        return definition.getArgs().getInt("check-interval", 200);
    }

    private String placeholderToCheck() {
        return definition.getArgs().getString("placeholder", null);
    }

    private String comparisonType() {
        return definition.getArgs().getString("comparison", "==");
    }

    private String value() {
        return definition.getArgs().getString("value", null);
    }
}
