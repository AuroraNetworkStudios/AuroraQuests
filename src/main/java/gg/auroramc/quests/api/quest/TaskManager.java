package gg.auroramc.quests.api.quest;

import gg.auroramc.quests.config.quest.TaskConfig;

import java.util.Map;

public class TaskManager {
    private static final TaskEvaluator DEFAULT_EVALUATOR = new TypedTaskEvaluator();
    private static Map<String, TaskEvaluator> evaluatorMap;

    public static void registerEvaluator(String taskType, TaskEvaluator evaluator) {
        evaluatorMap.put(taskType, evaluator);
    }

    public static TaskEvaluator getEvaluator(String taskType) {
        return evaluatorMap.getOrDefault(taskType, DEFAULT_EVALUATOR);
    }

    public static boolean evaluate(TaskConfig config, Map<String, Object> params) {
        return getEvaluator(config.getTask()).evaluate(config, params);
    }
}
