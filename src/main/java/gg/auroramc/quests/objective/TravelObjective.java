package gg.auroramc.quests.objective;

import gg.auroramc.quests.api.objective.Objective;
import gg.auroramc.quests.api.objective.ObjectiveDefinition;
import gg.auroramc.quests.api.profile.Profile;
import gg.auroramc.quests.api.quest.Quest;
import lombok.RequiredArgsConstructor;
import org.bukkit.Statistic;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class TravelObjective extends Objective {
    private final List<StatHandler> handlers = new ArrayList<>();
    private static final Map<String, Statistic> stats = Map.of(
            "walk", Statistic.WALK_ONE_CM,
            "sprint", Statistic.SPRINT_ONE_CM,
            "swim", Statistic.SWIM_ONE_CM,
            "boat", Statistic.BOAT_ONE_CM,
            "horse", Statistic.HORSE_ONE_CM,
            "pig", Statistic.PIG_ONE_CM,
            "strider", Statistic.STRIDER_ONE_CM,
            "walk-on-water", Statistic.WALK_ON_WATER_ONE_CM,
            "walk-under-water", Statistic.WALK_UNDER_WATER_ONE_CM
    );

    public TravelObjective(Quest quest, ObjectiveDefinition definition, Profile.TaskDataWrapper data) {
        super(quest, definition, data);

        boolean all = definition.getArgs().getBoolean("all", false);

        for (var stat : stats.entrySet()) {
            if (definition.getArgs().getBoolean(stat.getKey(), false) || all) {
                handlers.add(new StatHandler(stat.getValue()));
            }
        }
    }

    @Override
    protected void activate() {
        for (var handler : handlers) {
            handler.activate(data);
        }
        syncInterval(this::handler, 100, 100, false);
    }

    private void handler() {
        double sum = 0;
        for (var handler : handlers) {
            sum += handler.handle(data);
        }
        if (sum > 0) {
            progress(sum, meta());
        }
    }

    @RequiredArgsConstructor
    public static final class StatHandler {
        private final Statistic stat;
        private int previousValue;

        public void activate(Profile.TaskDataWrapper data) {
            previousValue = data.profile().getPlayer().getStatistic(stat);
        }

        public double handle(Profile.TaskDataWrapper data) {
            var current = data.profile().getPlayer().getStatistic(stat);
            var diff = current - previousValue;
            if (diff > 0) {
                previousValue = current;
                return diff / 100D;
            }
            return 0;
        }
    }
}
