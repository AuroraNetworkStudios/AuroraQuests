package gg.auroramc.quests.api.factory;

import gg.auroramc.quests.AuroraQuests;
import gg.auroramc.quests.api.profile.Profile;
import gg.auroramc.quests.api.quest.Quest;
import gg.auroramc.quests.api.objective.ObjectiveDefinition;
import gg.auroramc.quests.api.objective.Objective;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class ObjectiveFactory {
    private static final Map<String, Class<? extends Objective>> objectives = new HashMap<>();

    public static void registerObjective(String name, Class<? extends Objective> objective) {
        objectives.put(name.toLowerCase(Locale.ROOT), objective);
    }

    public static Objective createObjective(Quest holder, ObjectiveDefinition definition) {
        var taskData = holder.getData().toTaskDataWrapper(definition.getId());

        var objective = objectives.get(definition.getTask().toLowerCase(Locale.ROOT));
        if (objective == null) {
            AuroraQuests.logger().severe("Task " + definition.getId() + " was tried to use a non existent task type: " + definition.getTask());
            return null;
        }

        try {
            return objective.getDeclaredConstructor(Quest.class, ObjectiveDefinition.class, Profile.TaskDataWrapper.class)
                    .newInstance(holder, definition, taskData);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
